package com.herbruns.pbsync.util;

import com.google.gson.Gson;
import com.herbruns.pbsync.util.discord.Webhook;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
public class ApiHandler
{
    private OkHttpClient httpClient;
    public ApiHandler(OkHttpClient httpClient) { this.httpClient = httpClient; }

    public CompletableFuture<Void> sendWebhookData(List<String> webhookUrls, Webhook webhookData)
    {
        Gson gson = new Gson();
        String jsonStr = gson.toJson(webhookData);

        List<CompletableFuture<Void>> sends = webhookUrls.stream()
                .map(url ->
                        this.postRaw(url, jsonStr, "application/json")
                                .exceptionally(ex -> {
                                    log.error("Failed to send webhook to {}: {}", url, ex.getMessage(), ex);
                                    return null;
                                })
                                .thenAccept(_v -> log.info("Webhook sent to {}", url))
                )
                .collect(Collectors.toList());

        // Return a future that completes when all sends are done
        return CompletableFuture.allOf(sends.toArray(new CompletableFuture[0]));
    }

    public CompletableFuture<ResponseBody> postRaw(String url, String data, String type)
    {
        Request request = new Request.Builder().url(url).post(RequestBody.create(MediaType.parse(type), data)).build();

        return callRequest(request);
    }

    // Note: potentially implement screenshotting cability in the future
    /*
    public CompletableFuture<Void> postFormImage(String url, byte[] imageBytes, String type)
    {
        MultipartBody.Builder requestBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file", "image.png", RequestBody.create(MediaType.parse(type), imageBytes));

        Request request = new Request.Builder().url(url).post(requestBuilder.build()).build();

        return callRequest(request).thenAccept(rb ->
        {
        });
    }*/

    private CompletableFuture<ResponseBody> callRequest(Request request)
    {
        CompletableFuture<ResponseBody> future = new CompletableFuture<>();

        httpClient.newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
                try (ResponseBody responseBody = response.body())
                {
                    if (!response.isSuccessful())
                    {
                        future.completeExceptionally(new IOException("Unexpected code " + response));
                    }
                    else
                    {
                        future.complete(responseBody);
                    }
                }
                response.close();
            }
        });

        return future;
    }
}