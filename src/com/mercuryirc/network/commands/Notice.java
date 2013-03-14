package com.mercuryirc.network.commands;

import com.mercuryirc.misc.IrcUtils;
import com.mercuryirc.model.Entity;
import com.mercuryirc.model.Message;
import com.mercuryirc.model.Server;
import com.mercuryirc.model.User;
import com.mercuryirc.network.Connection;

public class Notice implements Connection.CommandHandler {

	public boolean applies(Connection connection, String command, String line) {
		return command.equals("NOTICE");
	}

	public void process(Connection connection, String line, String[] parts) {
		String from = IrcUtils.parseSource(parts[0]);
		String cmd = parts[1];
		String to = parts[2];

		String text = line.substring(line.indexOf(':', 1) + 1);

		Server server = connection.getServer();
		User source = server.getUser(from);
		Entity target = to.startsWith("#") ? server.getChannel(to) : server.getUser(to);
		if(text.length() > 0 && text.charAt(0) == '\u0001') {
			String ctcp = text.substring(1, text.indexOf('\u0001', 1));
			connection.getInputCallback().onCtcp(connection, source, ctcp);
		} else {
			Message message = new Message(source, target, text);
			connection.getInputCallback().onNotice(connection, message);
		}
	}

}