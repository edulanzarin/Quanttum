package org.project.view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.project.functions.FazerLogin;

public class LoginWindow extends Application {

    private Label messageLabel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Login");

        VBox authContainer = new VBox();
        authContainer.getStyleClass().add("auth-container");

        // Adicionando o ícone
        ImageView iconView = new ImageView(new Image(getClass().getResourceAsStream("/org/project/images/icon.png")));
        iconView.setFitWidth(60);  // Defina a largura desejada do ícone
        iconView.setFitHeight(60); // Defina a altura desejada do ícone

        Label userLabel = new Label("Usuário:");
        userLabel.getStyleClass().add("user-label");
        TextField userField = new TextField();
        userField.getStyleClass().add("user-field");

        Label passwordLabel = new Label("Senha:");
        passwordLabel.getStyleClass().add("password-label");
        PasswordField passwordField = new PasswordField();
        passwordField.getStyleClass().add("password-field");

        Button loginButton = new Button("Login");
        loginButton.getStyleClass().add("verify-button");

        messageLabel = new Label();
        messageLabel.getStyleClass().add("message-label");

        loginButton.setOnAction(e -> handleLogin(userField.getText(), passwordField.getText(), primaryStage));

        // Adicionando os componentes ao authContainer
        authContainer.getChildren().addAll(iconView, userLabel, userField, passwordLabel, passwordField, loginButton, messageLabel);

        Scene scene = new Scene(authContainer, 500, 400);
        String authCssFile = getClass().getResource("/org/project/styles/auth-styles.css").toExternalForm();
        scene.getStylesheets().add(authCssFile);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleLogin(String user, String password, Stage primaryStage) {
        FazerLogin auth = new FazerLogin();
        String userId = auth.isUserActive(user, password);

        if (userId != null) {
            MainWindow mainWindow = new MainWindow(userId);
            try {
                mainWindow.start(new Stage());
                primaryStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            messageLabel.setText("Usuário ou senha inválidos, ou usuário inativo.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
