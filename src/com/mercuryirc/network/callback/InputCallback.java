package com.mercuryirc.network.callback;

import com.mercuryirc.model.Channel;
import com.mercuryirc.model.Entity;
import com.mercuryirc.model.Message;
import com.mercuryirc.model.Mode;
import com.mercuryirc.model.User;
import com.mercuryirc.network.Connection;

import java.util.List;
import java.util.Set;

public interface InputCallback {

	/** Called upon successful connection to the IRC network. */
	public void onConnect(Connection connection);

	/** Called whenever someone sends a message to you or a channel you are in. */
	public void onPrivMsg(Connection connection, Message message);

	public void onNotice(Connection connection, Message message);

	public void onCtcp(Connection connection, User source, String ctcp);

	/** Called whenever any user joins a channel you are in. */
	public void onChannelJoin(Connection connection, Channel channel, User user);

	/** Called whenever any user parts a channel you are in. */
	public void onChannelPart(Connection connection, Channel channel, User user, String reason);

	/** Called whenever any user (who shares a common channel with you) quits. */
	public void onUserQuit(Connection connection, User user, String reason);

	/** Called whenever all nicknames in the list have been received. */
	public void onChannelNickList(Connection connection, Channel channel, Set<User> users);

	/** Called whenever the topic changes in a channel you are in. */
	public void onTopicChange(Connection connection, Channel channel, User source, String topic);

	/** Called whenever any user (who shares a common channel with you) changes nicks. */
	public void onUserNickChange(Connection connection, User user, String oldNick);

	public void onUserKick(Connection connection, Channel channel, User user, String reason);

	public void onChannelModeList(Connection connection, Channel channel, Mode.Type type, List<Mode> list);

	public void onError(Connection connection, String error);

	public void onUnknownCommand(Connection connection, String command);

	public void onModeChange(Connection connection, Entity target, Set<Mode> modes, boolean add);

}