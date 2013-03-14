package com.zmansiv.plugin.chat.ui;

import com.mercuryirc.model.User;
import com.zmansiv.plugin.chat.Chat;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

public class UserPane extends BorderPane {

    private final ListView<User> emptyUserList;
    private final Label title;

    public UserPane() {
        getStylesheets().add(Chat.context.resource("css/chat/UserList.css").toExternalForm());
        emptyUserList = new ListView<>();
        emptyUserList.setId("userpane-list");
        getStyleClass().add("user-box");
        title = new Label();
        title.setId("lebel");
        title.setEffect(new DropShadow(BlurType.ONE_PASS_BOX, Color.BLACK, 1, 1, 1, 1));
        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        ImageView chatIcon = new ImageView(Chat.context.resource("images/chat/logo.png").toExternalForm());
        HBox titleBox = new HBox();
        titleBox.setId("title");
        titleBox.setPrefHeight(28.0);
        titleBox.getChildren().addAll(title, spacer, chatIcon);
        setPrefWidth(150);
        setTop(titleBox);
    }

    public void setUserCount(int count) {
        title.setText("USERS (" + count + ")");
    }

    public void setEmpty() {
        setCenter(emptyUserList);
        setUserCount(0);
    }

}