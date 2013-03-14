package com.zmansiv.plugin.chat.callback;

import com.mercuryirc.model.Channel;
import com.mercuryirc.model.Entity;
import com.mercuryirc.model.Message;
import com.mercuryirc.model.Mode;
import com.mercuryirc.model.User;
import com.mercuryirc.network.Connection;
import com.mercuryirc.network.callback.InputCallback;
import com.rsbuddy.api.env.Client;
import com.zmansiv.plugin.chat.Chat;
import com.zmansiv.plugin.chat.ui.ChatPane;
import com.zmansiv.plugin.chat.util.Configuration;
import com.zmansiv.plugin.chat.util.HtmlMessageRow;
import javafx.application.Platform;

import java.util.List;
import java.util.Set;

public class InputCallbackImpl implements InputCallback {

	private final ChatPane chatPane;

	public InputCallbackImpl(ChatPane chatPane) {
		this.chatPane = chatPane;
	}

	@Override
	public void onConnect(final Connection connection) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Client client = Chat.context.lookup(Client.class);
				if (connection.getServer().getName().equals(Configuration.DEFAULT_NETWORK)) {
					Message message = new Message(connection.getLocalUser(), connection.getServer().getUser("NickServ"), "IDENTIFY " + client.username() + " " + client.session());
					connection.privmsg(message, true);
					connection.join(Configuration.DEFAULT_CHANNEL);
					if (client.dev()) {
						connection.join(Configuration.DEFAULT_DEV_CHANNEL);
					}
				} else {
					if (connection.getLocalUser().getNickservPassword() != null) {
						Message message = new Message(connection.getLocalUser(), connection.getServer().getUser("NickServ"), "IDENTIFY " + client.username() + " " + connection.getLocalUser().getNickservPassword());
						connection.privmsg(message, true);
					}
				}
				if (connection.getLocalUser().getAutojoinChannels() != null) {
					for (String channel : connection.getLocalUser().getAutojoinChannels()) {
						connection.join(channel);
					}
				}
			}
		});
	}

	@Override
	public void onPrivMsg(final Connection connection, final Message message) {
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
				Message _message = new Message(message.getSource(), null, message.getMessage());
				chatPane.tabPane().addUntargetedMessage(connection, _message, HtmlMessageRow.Type.NOTICE);
			}
		});
	}

	@Override
	public void onCtcp(final Connection connection, final User source, final String message) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (message.startsWith("ACTION")) {
					Message _message = new Message(source, null, "- " + source.getName() + " " + message.substring(7));
					chatPane.tabPane().addUntargetedMessage(connection, _message, HtmlMessageRow.Type.CTCP);
				} else {
					Message _message = new Message(source, null, "Received from " + source.getName() + ": CTCP " + message);
					chatPane.tabPane().addUntargetedMessage(connection, _message, HtmlMessageRow.Type.CTCP);
				}
			}
		});
	}

	@Override
	public void onChannelJoin(final Connection connection, final Channel channel, final User user) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Message message = new Message(user, channel, "has joined " + channel.getName());
				chatPane.tabPane().addTargetedMessage(connection, message, HtmlMessageRow.Type.JOIN);
			}
		});
	}

	@Override
	public void onChannelPart(final Connection connection, final Channel channel, final User user, final String reason) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Message message = new Message(user, channel, "has left " + channel.getName());
				chatPane.tabPane().addTargetedMessage(connection, message, HtmlMessageRow.Type.PART);
				if (user.equals(connection.getLocalUser())) {
					chatPane.tabPane().closeTab(channel);
					chatPane.userPane().setEmpty();
					chatPane.userPane().setUserCount(0);
				}
			}
		});
	}

	@Override
	public void onUserQuit(final Connection connection, final User user, final String reason) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Message message = new Message(user, null, "has quit (" + reason + ")");
				chatPane.tabPane().addUserStatusMessage(user, message, HtmlMessageRow.Type.PART);
			}
		});
	}

	@Override
	public void onChannelNickList(final Connection connection, Channel channel, Set<User> users) {
	}

	@Override
	public void onTopicChange(final Connection connection, final Channel channel, final User source, final String topic) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Message message = new Message(source, channel, "has set the topic: " + topic);
				chatPane.tabPane().addTargetedMessage(connection, message, HtmlMessageRow.Type.EVENT);
			}
		});
	}

	@Override
	public void onUserNickChange(final Connection connection, final User user, final String oldNick) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Message message = new Message(null, null, oldNick + " is now known as " + user.getName());
				chatPane.tabPane().addUserStatusMessage(user, message, HtmlMessageRow.Type.EVENT);
			}
		});
	}

	@Override
	public void onUserKick(final Connection connection, final Channel channel, final User user, final String reason) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Message message = new Message(user, channel, "has been kicked from " + channel.getName());
				chatPane.tabPane().addTargetedMessage(connection, message, HtmlMessageRow.Type.PART);
			}
		});
	}

	@Override
	public void onChannelModeList(final Connection connection, final Channel channel, final Mode.Type type, final List<Mode> list) {
		Message message = new Message(null, null, "Channel " + type.toString() + " list:");
		chatPane.tabPane().addTargetedMessage(connection, message, HtmlMessageRow.Type.EVENT);
		for (Mode mode : list) {
			Message message2 = new Message(null, null, ((User) mode.getTarget()).getHost() + " by " + mode.getSource().getHost());
			chatPane.tabPane().addTargetedMessage(connection, message2, HtmlMessageRow.Type.EVENT);
		}

	}

	@Override
	public void onError(final Connection connection, final String error) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Message message = new Message(null, null, error);
				chatPane.tabPane().addUntargetedMessage(connection, message, HtmlMessageRow.Type.ERROR);
			}
		});
	}

	@Override
	public void onUnknownCommand(final Connection connection, final String command) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Message message = new Message(null, null, "Unknown command: " + command);
				chatPane.tabPane().addUntargetedMessage(connection, message, HtmlMessageRow.Type.ERROR);
			}
		});
	}

	@Override
	public void onModeChange(final Connection connection, final Entity target, final Set<Mode> modes, final boolean add) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				for (Mode mode : modes) {
					Message message = new Message(mode.getSource(), target, (add ? "" : "un") + "sets " + mode.getType().toString() + " on " + mode.getTarget().getName() + (mode.getTarget().equals(target) ? "" : " in " + target.getName()));
					if (target instanceof Channel) {
						chatPane.tabPane().addTargetedMessage(connection, message, HtmlMessageRow.Type.EVENT);
					} else {
						chatPane.tabPane().addUntargetedMessage(connection, message, HtmlMessageRow.Type.EVENT);
					}
				}
			}
		});
	}

}