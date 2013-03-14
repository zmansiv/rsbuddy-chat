package com.zmansiv.plugin.chat.callback;

import com.mercuryirc.model.Entity;
import com.mercuryirc.model.Message;
import com.mercuryirc.model.User;
import com.mercuryirc.network.Connection;
import com.mercuryirc.network.callback.OutputCallback;
import com.zmansiv.plugin.chat.ui.ChatPane;
import com.zmansiv.plugin.chat.util.HtmlMessageRow;
import javafx.application.Platform;

public class OutputCallbackImpl implements OutputCallback {

	private final ChatPane chatPane;

	public OutputCallbackImpl(ChatPane chatPane) {
		this.chatPane = chatPane;
	}

	@Override
	public void onPrivmsg(final Connection connection, final Message message) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				chatPane.tabPane().addTargetedMessage(connection, message, HtmlMessageRow.Type.PRIVMSG);
			}
		});
	}

	@Override
	public void onNotice(final Connection connection, final Message message) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				chatPane.tabPane().addUntargetedMessage(connection, message, HtmlMessageRow.Type.NOTICE);
			}
		});
	}

	@Override
	public void onCtcp(final Connection connection, final Entity target, final String message) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (message.startsWith("ACTION")) {
					Message _message = new Message(connection.getLocalUser(), target, "- " + connection.getLocalUser().getName() + " " + message.substring(7));
					chatPane.tabPane().addUntargetedMessage(connection, _message, HtmlMessageRow.Type.CTCP);
				} else {
					Message _message = new Message(connection.getLocalUser(), target, "Sent to " + target.getName() + ": CTCP " + message);
					chatPane.tabPane().addUntargetedMessage(connection, _message, HtmlMessageRow.Type.CTCP);
				}
			}
		});
	}

	@Override
	public void onQuery(final Connection connection, final User user) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				chatPane.tabPane().createTab(connection, user);
			}
		});
	}

	@Override
	public void onConnectionRequest(final Connection connection, final String network, final String hostname, final int port, final String nick) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Connection conn = chatPane.connect(network, hostname, port, nick);
				chatPane.tabPane().createTab(conn, conn.getServer());
			}
		});
	}

}