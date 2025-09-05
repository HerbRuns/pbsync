package com.herbruns.pbsync;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("pbsync")
public interface PBSyncConfig extends Config
{
    @ConfigSection(
            position = 1,
            name = "Webhook Options",
            description = "Manage how the plugin sends drops to your discord server"
    )
    String webhookOptionsSection = "webhookOptionsSection";

    @ConfigItem(
            keyName = "pbSyncWebhookUrl",
            name = "Sync webhook URL",
            description = "The Discord webhook for sync channel. This will be pretty noisey and busy!",
            section = webhookOptionsSection,
            position = 1
    )
    default String pbSyncWebhookUrl()
    {
        return "";
    }

    @ConfigItem(
            keyName = "newPbWebhookUrl",
            name = "New PB webhook URL",
            description = "The Discord webhook for the new PBs channel. These messages are embedded and slightly easier to read",
            section = webhookOptionsSection,
            position = 2
    )
    default String newPbWebhookUrl()
    {
        return "";
    }
}
