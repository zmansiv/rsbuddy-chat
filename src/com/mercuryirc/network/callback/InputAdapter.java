package com.mercuryirc.network.callback;

import com.mercuryirc.model.Channel;
import com.mercuryirc.model.Entity;
import com.mercuryirc.model.Message;
import com.mercuryirc.model.Mode;
import com.mercuryirc.model.User;
import com.mercuryirc.network.Connection;

import java.util.List;
import java.util.Set;

/**
 * Contains empty implementations of InputCallback methods
 * so that clients can pick and choose which methods they would like
 * to implement without adhering to the whole InputCallback interface.
 */
public class InputAdapter implements InputCallback {

	public void onConnect(Connection connection) { }

	public void onPrivMsg(Connection connection, Message message) { }

	public void onNotice(Connection connection, Message message) { }

	public void onCtcp(Connection connection, User source, String ctcp) { }

	public void onChannelJoin(Connection connection, Channel channel, User user) { }

	public void onChannelPart(Connection connection, Channel channel, User user, String reason) { }

	public void onUserQuit(Connection connection, User user, String reason) { }

	public void onChannelNickList(Connection connection, Channel channel, Set<User> users) {}

	public void onTopicChange(Connection connection, Channel channel, User source, String topic) { }

	public void onUserNickChange(Connection connection, User user, String oldNick) { }

	public void onUserKick(Connection connection, Channel channel, User user, String reason) { }

	public void onChannelModeList(Connection connection, Channel channel, Mode.Type type, List<Mode> list) { }

	public void onError(Connection connection, String error) { }

	public void onUnknownCommand(Connection connection, String command) { }

	public void onModeChange(Connection connection, Entity target, Set<Mode> modes, boolean add) { }

}