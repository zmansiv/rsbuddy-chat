package com.mercuryirc.network.callback;

import com.mercuryirc.model.Entity;
import com.mercuryirc.model.Message;
import com.mercuryirc.model.User;
import com.mercuryirc.network.Connection;

public interface OutputCallback {

	public void onPrivmsg(Connection connection, Message message);

	public void onNotice(Connection connection, Message message);

	public void onCtcp(Connection connection, Entity target, String message);

	public void onQuery(Connection connection, User user);

	public void onConnectionRequest(Connection connection, String network, String hostname, int port, String nick);

}