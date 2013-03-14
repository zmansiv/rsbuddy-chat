package com.zmansiv.plugin.chat.ui;

import com.mercuryirc.model.Channel;
import com.mercuryirc.model.Entity;
import com.mercuryirc.model.Message;
import com.mercuryirc.model.User;
import com.mercuryirc.network.Connection;
import com.zmansiv.plugin.chat.util.HtmlMessageRow;

import java.util.LinkedList;
import java.util.List;

public class TabPane extends javafx.scene.control.TabPane {

	private final ChatPane chatPane;

	public TabPane(ChatPane parent) {
		this.chatPane = parent;
		setId("messages");
		setTabClosingPolicy(TabClosingPolicy.ALL_TABS);
	}

	public void addUserStatusMessage(User source, Message message, HtmlMessageRow.Type type) {
		for (Tab tab : tabs()) {
			if (tab.target().equals(source) || (tab.target() instanceof Channel && ((Channel) tab.target()).getUsers().contains(source))) {
				tab.messages().add(new HtmlMessageRow(message, type));
			}
		}
	}

	public void addTargetedMessage(Connection connection, Message message, HtmlMessageRow.Type type) {
		Entity target = message.getTarget();
		if (!(target instanceof Channel)) {
			target = message.getSource();
		}
		Tab tab = null;
		for (Tab _tab : tabs()) {
			if (_tab.target().equals(target)) {
				tab = _tab;
				break;
			}
		}
		if (tab == null) {
			if (type == HtmlMessageRow.Type.PART && message.getSource().equals(connection.getLocalUser())) {
				return;
			}
			tab = createTab(connection, message.getTarget());
		}
		tab.messages().add(new HtmlMessageRow(message, type));
	}

	public void addUntargetedMessage(Connection connection, Message message, HtmlMessageRow.Type type) {
		Tab tab = (Tab) getSelectionModel().getSelectedItem();
		if (tab == null || !tab.connection().equals(connection)) {
			tab = null;
			for (Tab _tab : tabs()) {
				if (_tab.connection().equals(connection)) {
					tab = _tab;
					break;
				}
			}
			if (tab == null) {
				tab = createTab(connection, connection.getServer());
			}
		}
		tab.messages().add(new HtmlMessageRow(message, type));
	}

	public Tab createTab(Connection connection, Entity entity) {
		for (Tab tab : tabs()) {
			if (tab.target().equals(entity)) {
				return tab;
			}
		}
		final Tab tab = new Tab(chatPane, connection, entity);
		getTabs().add(tab);
		if (entity instanceof Channel) {
			getSelectionModel().select(tab);
		}
		return tab;
	}

	public void closeTab(final Entity entity) {
		for (Tab tab : tabs()) {
			if (tab.target().equals(entity)) {
				getTabs().remove(tab);
				return;
			}
		}
	}

	public List<Tab> tabs() {
		List<Tab> tabs = new LinkedList<>();
		for (javafx.scene.control.Tab tab : getTabs()) {
			tabs.add((Tab) tab);
		}
		return tabs;
	}

}