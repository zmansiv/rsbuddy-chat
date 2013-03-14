package com.zmansiv.plugin.chat.ui;

import com.mercuryirc.model.Channel;
import com.mercuryirc.model.Entity;
import com.mercuryirc.model.User;
import com.mercuryirc.network.Connection;
import com.zmansiv.plugin.chat.Chat;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.LinkedList;
import java.util.List;

public class InputPane extends HBox {

	private final ChatPane chatPane;
	private final Connection connection;
	private final Entity target;
	private final InputField messageField;
	private String lastMessage = null;

	public InputPane(ChatPane chatPane, Connection connection, Entity target) {
		super(5.0);
		this.chatPane = chatPane;
		this.connection = connection;
		this.target = target;
		setPrefHeight(41);
		setAlignment(Pos.CENTER_LEFT);
		getStylesheets().add(Chat.context.resource("css/chat/Input.css").toExternalForm());
		setId("input-box");
		messageField = new InputField("Message");
		HBox.setHgrow(messageField, Priority.SOMETIMES);
		getChildren().addAll(messageField, createSendButton());
	}

	private Button createSendButton() {
		Button sendButton = new Button();
		sendButton.setId("send");
		sendButton.setOnAction(messageSendEventHandler);
		sendButton.setMinSize(63, 32);
		sendButton.setPrefSize(63, 32);
		return sendButton;
	}

	private final class InputField extends TextField {

		private List<String> autocompletionOptions = null;
		private int autocompletionStart = -1, autocompletionOption = -1;

		public InputField(final String promptText) {
			setPromptText(promptText);
			lengthProperty().add(200);
			setMinHeight(32);
			setOnAction(messageSendEventHandler);
			setOnKeyPressed(new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent keyEvent) {
					switch (keyEvent.getCode()) {
						case TAB:
							if (keyEvent.isControlDown()) {
								SingleSelectionModel<javafx.scene.control.Tab> model = chatPane.tabPane().getSelectionModel();
								if (keyEvent.isShiftDown()) {
									if (model.getSelectedIndex() == 0) {
										model.selectLast();
									} else {
										model.selectPrevious();
									}
								} else {
									if (model.getSelectedIndex() == chatPane.tabPane().getTabs().size() - 1) {
										model.selectFirst();
									} else {
										model.selectNext();
									}
								}
								resetAutocompletions();
							} else {
								autocomplete();
							}
							break;
						case UP:
							if (lastMessage != null) {
								setText(lastMessage);
							}
						default:
							resetAutocompletions();
					}
					keyEvent.consume();
				}
			});
		}

		private void autocomplete() {
			String text = getText();
			for (int i = getCaretPosition() - 1; i >= 0; i--) {
				if (text.charAt(i) == ' ') {
					break;
				}
				autocompletionStart = i;
			}
			int _caret = getCaretPosition();
			String beginning = text.substring(autocompletionStart, getCaretPosition()).toLowerCase();
			String targetName = target.getName();
			if (targetName.toLowerCase().startsWith(beginning)) {
				deleteText(autocompletionStart, getCaretPosition());
				insertText(autocompletionStart, target.getName() + " ");
				_caret = autocompletionStart + targetName.length() + 1;
			} else {
				if (target instanceof Channel) {
					if (autocompletionOptions == null) {
						autocompletionOptions = new LinkedList<>();
						for (User user : ((Channel) target).getUsers()) {
							final String nick = user.getName();
							if (nick.toLowerCase().startsWith(beginning)) {
								autocompletionOptions.add(nick);
							}
						}
					}
					if (autocompletionOptions.size() > 0) {
						int i = autocompletionOptions.size() > autocompletionOption + 1 ? ++autocompletionOption : (autocompletionOption = 0);
						String nick = autocompletionOptions.get(i);
						deleteText(autocompletionStart, getCaretPosition());
						insertText(autocompletionStart, nick + " ");
						_caret = autocompletionStart + nick.length() + 1;
					}
				}
			}
			final int caret = _caret;
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					requestFocus();
					positionCaret(caret);
				}
			});
		}

		private void resetAutocompletions() {
			autocompletionOptions = null;
			autocompletionStart = -1;
			autocompletionOption = -1;
		}

	}

	private final EventHandler<ActionEvent> messageSendEventHandler = new EventHandler<ActionEvent>() {
		public void handle(ActionEvent actionEvent) {
			String input = messageField.getText();
			if (!input.isEmpty()) {
				lastMessage = input;
				connection.process(target, input);
			}
			messageField.setText("");
		}
	};

}