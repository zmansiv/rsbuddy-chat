package com.zmansiv.plugin.chat.ui;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.mercuryirc.model.Server;
import com.mercuryirc.model.User;
import com.mercuryirc.network.Connection;
import com.rsbuddy.api.env.Client;
import com.zmansiv.plugin.chat.Chat;
import com.zmansiv.plugin.chat.callback.InputCallbackImpl;
import com.zmansiv.plugin.chat.callback.OutputCallbackImpl;
import com.zmansiv.plugin.chat.util.Configuration;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.LinkedList;
import java.util.List;

public class ChatPane extends BorderPane {

	private final ObservableList<Connection> connections = FXCollections.observableList(new LinkedList<Connection>());

	private final UserPane userPane;
	private final TabPane tabPane;

	public ChatPane() {
		getStylesheets().add(Chat.context.resource("css/chat/Chat.css").toExternalForm());
		getStyleClass().add("main");
		GridPane.setHgrow(this, Priority.ALWAYS);
		GridPane.setVgrow(this, Priority.ALWAYS);
		userPane = new UserPane();
		tabPane = new TabPane(this);
		if (Boolean.parseBoolean(Chat.context.get("autoconnect").toString())) {
			connect();
		} else {
			showConnectPane();
		}
	}

	public void showConnectPane() {
		VBox connectPane = new VBox();
		connectPane.setId("connect");
		Button button = new Button();
		button.setMinSize(75, 25);
		button.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent actionEvent) {
				connect();
			}
		});
		connectPane.getChildren().add(button);
		setLeft(null);
		setCenter(connectPane);
	}

	public void connect() {
		boolean loaded = false;
		String connectionsString = Chat.context.get("connections");
		if (connectionsString != null && !connectionsString.isEmpty()) {
			JsonParser parser = new JsonParser();
			Gson gson = new Gson();
			JsonObject connectionsJson = parser.parse(connectionsString).getAsJsonObject();
			JsonElement obj = connectionsJson.get("connections");
			JsonArray connectionsArray = obj.getAsJsonArray();
			for (JsonElement connectionElement : connectionsArray) {
				JsonObject connection = connectionElement.getAsJsonObject();
				String network = connection.get("network").getAsString();
				String hostname = connection.get("hostname").getAsString();
				int port = connection.get("port").getAsInt();
				String nick = connection.get("nick").getAsString();
				String nspw = null;
				if (connection.has("nickserv_password")) {
					nspw = connection.get("nickserv_password").getAsString();
				}
				JsonArray channels = connection.getAsJsonArray("channels");
				String[] autoChannels = gson.fromJson(channels.toString(), new TypeToken<String[]>() {
				}.getType());
				Connection conn = connect(network, hostname, port, nick);
				conn.getLocalUser().setNickservPassword(nspw);
				conn.getLocalUser().setAutojoinChannels(autoChannels);
				loaded = true;
			}
		}
		if (!loaded) {
			connect(Configuration.DEFAULT_NETWORK, Configuration.DEFAULT_HOSTNAME, Configuration.DEFAULT_PORT, Chat.context.lookup(Client.class).username());
		}
		setLeft(userPane);
		setCenter(tabPane);
	}

	public Connection connect(String network, String hostname, int port, String nick) {
		Server server = new Server(network, hostname, port, false);
		Connection connection = new Connection(server, new User(server, nick, nick, nick), new InputCallbackImpl(this), new OutputCallbackImpl(this));
		connections.add(connection);
		connection.connect();
		return connection;
	}

	public void disconnect() {
		for (Connection connection : connections) {
			connection.disconnect();
		}
	}

	public List<Connection> connections() {
		return connections;
	}

	public UserPane userPane() {
		return userPane;
	}

	public TabPane tabPane() {
		return tabPane;
	}


}