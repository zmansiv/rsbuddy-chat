package com.zmansiv.plugin.chat;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mercuryirc.model.Channel;
import com.mercuryirc.model.Entity;
import com.mercuryirc.network.Connection;
import com.rsbuddy.api.gui.Location;
import com.rsbuddy.plugin.PluginContext;
import com.rsbuddy.plugin.WidgetPluginBase;
import com.zmansiv.plugin.chat.ui.ChatPane;
import com.zmansiv.plugin.chat.ui.Tab;
import javafx.scene.Node;

import java.util.EnumSet;

public class Chat extends WidgetPluginBase {

    public static PluginContext context;
    private ChatPane chatPane = null;

    public Chat() {
        super("Chat", "images/chat/buttons/chat.png", "images/chat/buttons/chat_hover.png", "images/chat/buttons/chat_pressed.png");
    }

    @Override
    protected void init() {
        context = context();
        chatPane = new ChatPane();
    }

    @Override
    protected void dispose() {
        serializeConnections();
        chatPane.disconnect();
    }

    private void serializeConnections() {
        Gson gson = new Gson();
        JsonObject json = new JsonObject();
        JsonArray connections = new JsonArray();
        for (Connection conn : chatPane.connections()) {
            JsonObject connection = new JsonObject();
            connection.addProperty("network", conn.getServer().getName());
            connection.addProperty("hostname", conn.getServer().getHost());
            connection.addProperty("port", conn.getServer().getPort());
            connection.addProperty("nick", conn.getLocalUser().getName());
            String ns = conn.getLocalUser().getNickservPassword();
            if (ns != null) {
                connection.addProperty("nickserv_password", ns);
            }
            JsonArray channels = new JsonArray();
            for (Tab tab : chatPane.tabPane().tabs()) {
                if (tab.connection().equals(conn)) {
                    Entity target = tab.target();
                    if (target instanceof Channel) {
                        channels.add(new JsonPrimitive(target.getName()));
                    }
                }
            }
            connection.add("channels", channels);
            connections.add(connection);
        }
        json.add("connections", connections);
        context.set("connections", gson.toJson(json));
	}

    @Override
    public EnumSet<Location> supportedLocations() {
        return EnumSet.of(Location.BOTTOM);
    }

	@Override
	public Location defaultLocation() {
		return Location.BOTTOM;
	}

	@Override
    public Node content(Location loc) {
        return chatPane;
    }

}