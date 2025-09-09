package com.herbruns.pbsync;

import com.google.gson.Gson;
import com.google.inject.Injector;
import com.google.inject.Provides;
import javax.inject.Inject;

import com.herbruns.pbsync.features.PersonalBestInChat;
import com.herbruns.pbsync.features.PersonalBestSync;
import com.herbruns.pbsync.util.ApiHandler;
import com.herbruns.pbsync.util.discord.Discord;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import okhttp3.OkHttpClient;

import java.util.concurrent.ExecutionException;

@Slf4j
@PluginDescriptor(
	name = "PBSync"
)
public class PBSyncPlugin extends Plugin
{
    @Inject
    private Injector injector;
	@Inject
	private PBSyncConfig config;

    @Inject
    private OkHttpClient httpClient;
    @Inject
    private Gson gson;

    private ApiHandler apihandler;
    private Discord discord;
    private PersonalBestInChat personalBestInChat;
    private PersonalBestSync personalBestSync;

	@Override
	protected void startUp() throws Exception
    {
        apihandler = new ApiHandler(httpClient);
        discord = new Discord(gson);

        if (personalBestSync == null)
        {
            personalBestSync = injector.getInstance(PersonalBestSync.class);
        }
        personalBestSync.setUp(discord, apihandler);

        if (personalBestInChat == null)
        {
            personalBestInChat = injector.getInstance(PersonalBestInChat.class);
        }
        personalBestInChat.setUp(discord, apihandler);
	}

	@Override
	protected void shutDown() throws Exception
	{
        personalBestSync.cleanUp();
	}

    @Subscribe
    public void onChatMessage(ChatMessage event) throws ExecutionException, InterruptedException
    {
        // pb message in clan chat: NEW PB
        if (event.getType() == ChatMessageType.CLAN_MESSAGE)
        {
            String msg = event.getMessage();
            personalBestInChat.handleNewPbInChat(msg);
        }
        else if (event.getType() == ChatMessageType.PUBLICCHAT
            || event.getType() == ChatMessageType.CLAN_CHAT)
        {
            // The message string
            String msg = event.getMessage().trim();
            if (msg.equalsIgnoreCase("!pbsync"))
            {
                // Remove the original message
                event.getMessageNode().setRuneLiteFormatMessage(null);
                event.getMessageNode().setValue("");

                personalBestSync.handlePbSyncCommand();
            }
        }
        // anything else, we don't care about (right now)
    }

	@Provides
    PBSyncConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PBSyncConfig.class);
	}
}
