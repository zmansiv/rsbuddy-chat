package com.zmansiv.plugin.chat.ui;

import com.mercuryirc.model.Channel;
import com.mercuryirc.model.Mode;
import com.mercuryirc.model.User;
import com.mercuryirc.network.Connection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenuBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItemBuilder;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

import java.util.Comparator;
import java.util.Iterator;

public class UserList extends ListView<User> {

	private final ChatPane chatPane;
	private final Connection connection;
	private final Channel channel;

	public UserList(final ChatPane chatPane, Connection connection, final Tab tab) {
		this.chatPane = chatPane;
		this.connection = connection;
		if (tab.target() instanceof Channel) {
			this.channel = (Channel) tab.target();
		} else {
			this.channel = null;
		}
		setId("userpane-list");
		setCellFactory(new Callback<ListView<User>, ListCell<User>>() {
			public ListCell<User> call(ListView<User> tabListView) {
				return new UserCell();
			}
		});
		if (channel != null) {
			final ObservableSet<User> users = channel.getUsersProperty().get();
			Iterator<User> it = users.iterator();
			while (it.hasNext()) {
				final User user = it.next();
				getItems().add(user);
			}
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					FXCollections.sort(getItems(), USER_COMPARATOR);
				}
			});
			if (tab.isSelected()) {
				chatPane.userPane().setUserCount(users.size());
			}
			users.addListener((new SetChangeListener<User>() {
				@Override
				public void onChanged(final Change<? extends User> change) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							if (change.wasAdded()) {
								getItems().add(change.getElementAdded());
							} else {
								getItems().remove(change.getElementRemoved());
							}
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									FXCollections.sort(getItems(), USER_COMPARATOR);
								}
							});
							if (tab.isSelected()) {
								chatPane.userPane().setUserCount(getItems().size());
							}
						}
					});
				}
			}));
		}
	}

	private final Comparator<User> USER_COMPARATOR = new Comparator<User>() {
		@Override
		public int compare(User o1, User o2) {
			int rankC = User.RANK_COMPARATOR.compare(o1.getChannelRank(channel), o2.getChannelRank(channel));
			return rankC == 0 ? o1.getName().compareTo(o2.getName()) : rankC  * -1; //multiply by -1 because list ordering logic is flipped from treeset ordering log (which RANK_COMPARATOR was written for)
		}
	};

	private class UserCell extends ListCell<User> {

		private Mode.Type rank = null;

		public UserCell() {
			super();
			if (channel != null) {
				setContextMenu(ContextMenuBuilder.create().items(MenuItemBuilder.create().text("Kick").onAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent actionEvent) {
						connection.kick(channel, getItem());
					}
				}).build()).build());
			}
		}

		@Override
		protected void updateItem(final User user, boolean empty) {
			super.updateItem(user, empty);
			if (user != null) {
				final Label label = new Label();
				label.textProperty().bind(user.getNameProperty());
				label.getStyleClass().add("user-label");
				label.setMaxWidth(130);
				setRankId(user, label);
				user.getChannelRanksProperty(channel).addListener(new SetChangeListener<Mode.Type>() {
					@Override
					public void onChanged(Change<? extends Mode.Type> change) {
						setRankId(user, label);
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								FXCollections.sort(getItems(), USER_COMPARATOR);
							}
						});
					}
				});
				label.setOnMouseClicked(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent mouseEvent) {
						if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() > 1) {
							chatPane.tabPane().createTab(connection, user);
						}
					}
				});
				setGraphic(label);
			}
		}

		private void setRankId(User user, Label label) {
			String id = null;
			if (channel != null && (rank = user.getChannelRank(channel)) != null) {
				switch (rank) {
					case VOICE:
						id = "voice-label";
						break;
					case HALFOP:
						id = "halfop-label";
						break;
					case OP:
						id = "op-label";
						break;
					case PROTECT:
						id = "protect-label";
						break;
					case OWNER:
						id = "owner-label";
						break;
				}
			} else {
				id = "regular-label";
			}
			label.setId(id);
		}

	}



}