package com.zmansiv.plugin.chat.ui;

import com.mercuryirc.model.Channel;
import com.mercuryirc.model.Entity;
import com.mercuryirc.model.User;
import com.mercuryirc.network.Connection;
import com.zmansiv.plugin.chat.Chat;
import com.zmansiv.plugin.chat.util.Configuration;
import com.zmansiv.plugin.chat.util.HtmlMessageRow;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Tab extends javafx.scene.control.Tab {

	private static final DateFormat TIME_FORMATTER = new SimpleDateFormat(Configuration.TIMESTAMP_FORMAT);
	private final Connection connection;
	private final Entity target;
	private final ObservableList<HtmlMessageRow> messages = FXCollections.observableList(new LinkedList<HtmlMessageRow>());
	private final List<HtmlMessageRow> preloadQueue = Collections.synchronizedList(new LinkedList<HtmlMessageRow>());
	private final WebView webView;
	private boolean loaded = false;

	public Tab(final ChatPane chatPane, final Connection connection, final Entity target) {
		this.connection = connection;
		this.target = target;
		setClosable(true);
		textProperty().bind(target.getNameProperty());
		setTooltip(new Tooltip(connection.getServer().getName()));
		BorderPane container = new BorderPane();
		webView = new WebView();
		webView.setContextMenuEnabled(false);
		final WebEngine webEngine = webView.getEngine();
		webEngine.setOnAlert(new EventHandler<WebEvent<String>>() {
			@Override
			public void handle(WebEvent<String> stringWebEvent) {
				System.out.println(stringWebEvent.getData());
			}
		});
		webEngine.load(Chat.context.resource("html/chat/MessageList.html").toExternalForm());
		webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
			public void changed(ObservableValue observableValue, Worker.State state, Worker.State newState) {
				if (newState.equals(Worker.State.SUCCEEDED)) {
					onLoaded();
				}
			}
		});
		container.setCenter(webView);
		final InputPane inputPane = new InputPane(chatPane, connection, target);
		container.setBottom(inputPane);
		setContent(container);
		final UserList userList = new UserList(chatPane, connection, this);
		if (target instanceof User) {
			userList.getItems().add(connection.getLocalUser());
			userList.getItems().add((User) target);
		}
		final String readStyle = "-fx-background-image: url(\"" +
				Chat.context.resource("images/chat/tab-bg.png").toExternalForm() +
				"\"), url(\"" +
				Chat.context.resource("images/chat/tab-right.png").toExternalForm() +
				"\");" +
				"-fx-background-position: left, right;" +
				"-fx-background-repeat: repeat-x, no-repeat;" +
				"-fx-padding: 0 7px 0 13px;";
		final String unreadStyle = "-fx-background-image: url(\"" +
				Chat.context.resource("images/chat/tab-bg.png").toExternalForm() +
				"\"), url(\"" +
				Chat.context.resource("images/chat/msgblip.png").toExternalForm() +
				"\"), url(\"" +
				Chat.context.resource("images/chat/tab-right.png").toExternalForm() +
				"\");" +
				"-fx-background-position: left, left, right;" +
				"-fx-background-repeat: repeat-x, no-repeat, no-repeat;" +
				"-fx-padding: 0 7px 0 13px;";
		final String selectedStyle = "-fx-background-image: url(\"" +
				Chat.context.resource("images/chat/tab-highlight-bg.png").toExternalForm() +
				"\"), url(\"" +
				Chat.context.resource("images/chat/tab-highlight-right.png").toExternalForm() +
				"\");" +
				"-fx-background-position: left, right;" +
				"-fx-background-repeat: repeat-x, no-repeat;" +
				"-fx-padding: 0 7px 0 13px;";
		setStyle(readStyle);
		setOnClosed(new EventHandler<Event>() {
			public void handle(Event event) {
				if (target instanceof Channel) {
					connection.part((Channel) target);
				} else if (target.equals(connection.getServer())) {
					connection.disconnect();
					for (Tab tab : chatPane.tabPane().tabs()) {
						if (tab.connection().equals(connection)) {
							chatPane.tabPane().getTabs().remove(tab);
						}
					}
					if (chatPane.tabPane().tabs().size() == 0) {
						chatPane.showConnectPane();
					}
				}
			}
		});
		selectedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean
					aBoolean2) {
				if (aBoolean2) {
					setStyle(selectedStyle);
					chatPane.userPane().setCenter(userList);
					chatPane.userPane().setUserCount(userList.getItems().size());
					inputPane.requestFocus();
				} else {
					setStyle(readStyle);
				}
			}
		});
		messages.addListener(new ListChangeListener<HtmlMessageRow>() {

			@Override
			public void onChanged(Change<? extends HtmlMessageRow> change) {
				while (change.next()) {
					if (change.wasAdded()) {
						for (HtmlMessageRow message : change.getAddedSubList()) {
							if (!isSelected() && message.type().alert()) {
								setStyle(unreadStyle);
							}
							if (loaded) {
								webView.getEngine().executeScript(String.format("addRow('%s', '%s', '%s', '%s')", message.source(), message.message(), TIME_FORMATTER.format(message.time()), message.type().style()));
							} else {
								preloadQueue.add(message);
							}
						}
					}
				}
			}
		});
	}

	private void onLoaded() {
		loaded = true;
		synchronized (preloadQueue) {
			for (HtmlMessageRow message : preloadQueue) {
				webView.getEngine().executeScript(String.format("addRow('%s', '%s', '%s', '%s')", message.source(), message.message(), TIME_FORMATTER.format(message.time()), message.type().style()));
			}
		}
		JSObject window = (JSObject) webView.getEngine().executeScript("window");
		window.setMember("chatTab", this);
	}

	//called from javascript
	@SuppressWarnings("unused")
	public void openUrl(String url) {
		try {
			Desktop.getDesktop().browse(new URI(url));
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
		}
	}

	public Connection connection() {
		return connection;
	}

	public Entity target() {
		return target;
	}

	public ObservableList<HtmlMessageRow> messages() {
		return messages;
	}

}