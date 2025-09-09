/*
    Written to work directly with the Discord objects from MasterKenth source code
 */

package com.herbruns.pbsync.util.discord;

import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Discord {
    private Gson gson;

    public Discord(Gson gson) { this.gson = gson;}

    public Embed createEmbeddedMessage(String msg, String playerName, String activityName, String time) {
        Author author = new Author();
        author.setName("New Personal Best!");

        Embed embed = new Embed();
        embed.setAuthor(author);

        Field player = new Field();
        player.setName("Player");
        player.setValue(playerName);
        player.setInline(false);

        Field activity = new Field();
        activity.setName("Activity");
        activity.setValue(activityName);
        activity.setInline(false);

        Field timeVal = new Field();
        timeVal.setName("Time");
        timeVal.setValue(time);
        timeVal.setInline(false);

        embed.setFields(new Field[]{player, activity, timeVal});
        embed.setDescription(msg);
        return embed;
    }

    public String createJsonBlobMessage(String playerName, Map<String, String> activityTimes) {
        Gson newGson = gson.newBuilder().create();

        JsonObject root = new JsonObject();
        root.addProperty("player", playerName);

        // Convert the map into a JsonObject
        JsonObject activities = new JsonObject();
        for (Map.Entry<String, String> entry : activityTimes.entrySet())
        {
            activities.addProperty(entry.getKey(), entry.getValue());
        }
        root.add("activities", activities);

        // Serialize to JSON string
        return newGson.toJson(root);
    }
}
