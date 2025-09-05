package com.herbruns.pbsync.features;

import com.herbruns.pbsync.PBSyncConfig;
import com.herbruns.pbsync.util.ApiHandler;
import com.herbruns.pbsync.util.discord.*;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class PersonalBestInChat
{
    @Inject
    private Client client;

    @Inject
    private PBSyncConfig config;

    public void setUp()
    {
        // nothing
    }

    public void cleanUp()
    {
        // nothing
    }

    public void handleNewPbInChat(String msg)
    {
        msg = Text.removeTags(msg).replace('\u00A0', ' ').trim();

        String regex = "^(.+?) has achieved a new (.+?) personal best: (\\d{1,2}:\\d{2})$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(msg);

        String player = "";
        String activity = "";
        String time = "";

        if (matcher.find()) {
            player = matcher.group(1);
            player = Text.toJagexName(player);
            activity = matcher.group(2);
            time = matcher.group(3);

            // Note: just used for local testing. Commend for release/push
            //String chatDebug = player + ", " + activity + ", " + time;
            //client.addChatMessage(ChatMessageType.GAMEMESSAGE, "PB plugin", chatDebug, null);
        } else {
            return;
        }

        if(!player.equals(client.getLocalPlayer().getName())) {
            return;
        }

        List<String> webhookUrls = new ArrayList<>();
        webhookUrls.add(config.newPbWebhookUrl());

        ApiHandler.Instance().sendWebhookData(webhookUrls, newPersonalBestMessage(msg, player, activity, time))
            .exceptionally(ex -> {
                log.error("Failed to send webhook data", ex);
                return null;
            });
    }

    private Webhook newPersonalBestMessage(String msg, String playerName, String activity, String time)
    {
        Embed embed = Discord.createEmbeddedMessage(msg, playerName, activity, time);
        Webhook webhookData = new Webhook();
        webhookData.setEmbeds(new Embed[]{embed});
        return webhookData;
    }
}
