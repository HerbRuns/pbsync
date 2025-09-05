package com.herbruns.pbsync.features;

import com.herbruns.pbsync.PBSyncConfig;
import com.herbruns.pbsync.util.ApiHandler;
import com.herbruns.pbsync.util.BossNames;
import com.herbruns.pbsync.util.Utility;
import com.herbruns.pbsync.util.discord.Discord;
import com.herbruns.pbsync.util.discord.Webhook;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.config.ConfigManager;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
public class PersonalBestSync
{
    @Inject
    private ConfigManager configManager;
    @Inject
    private ChatMessageManager chatMessageManager;
    @Inject
    private Client client;
    @Inject
    private ClientThread clientThread;
    @Inject
    private ScheduledExecutorService executor;
    @Inject
    private PBSyncConfig config;

    private boolean isSyncing = false;
    private Map<String, String> activityTimes = new HashMap<>();
    private BossNames bossNames;

    public void setUp()
    {
        bossNames = new BossNames();
        activityTimes.clear();
    }

    public void cleanUp()
    {
        activityTimes.clear();
    }

    public void handlePbSyncCommand()
    {
        if (isSyncing) {
            return;
        }

        client.addChatMessage(
            net.runelite.api.ChatMessageType.GAMEMESSAGE,
            "",
            "[PBSync] Syncing, please wait...",
            null
        );

        try {
            gatherAllPersonalBestData();
        } catch (IOException e) {
            // uh oh
            log.error("Error while trying to gather all personal best data");
            log.error(e.getMessage());
        }
    }

    public void gatherAllPersonalBestData() throws IOException {
        isSyncing = true;

        String player = client.getLocalPlayer().getName();
        activityTimes.clear();

        executor.execute(() ->
        {
            try {
                // Background thread: we do a bunch of look-ups here
                for (String name : bossNames.getAllNames()) {
                    double pbVal = getPb(name);
                    if (pbVal == 0) {
                        continue;
                    }

                    String timeStrSeconds = Utility.secondsToTimeString(pbVal);
                    activityTimes.put(name, timeStrSeconds);
                }

                // Note: create a single item List to use multi-webhook url sending in ApiHandler
                List<String> webhookUrls = new ArrayList<>();
                webhookUrls.add(config.pbSyncWebhookUrl());

                ApiHandler.Instance().sendWebhookData(webhookUrls, personalBestSyncMessage(player, activityTimes))
                    .exceptionally(ex -> {
                        log.error("Failed to send webhook data", ex);
                        return null;
                    });

                // Switch back to client thread to write a chat message
                clientThread.invoke(() -> {
                    client.addChatMessage(
                        net.runelite.api.ChatMessageType.GAMEMESSAGE,
                        "",
                        "[PBSync] Finished, thanks!",
                        null
                    );
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

            isSyncing = false;
        });
    }

    private Webhook personalBestSyncMessage(String playerName, Map<String, String> activityTimes)
    {
        String msg = Discord.createJsonBlobMessage(playerName, activityTimes);
        Webhook webhookData = new Webhook();
        webhookData.setContent(msg);
        return webhookData;
    }

    // Note: lifted this from Runelite's chat commands approach of getting PBs
    // Couldn't inject the module to handle getPb but could utilise the same functionality below
    // Thanks devs! :D
    private double getPb(String boss)
    {
        Double personalBest = configManager.getRSProfileConfiguration("personalbest", boss.toLowerCase(), double.class);
        return personalBest == null ? 0 : personalBest;
    }
}
