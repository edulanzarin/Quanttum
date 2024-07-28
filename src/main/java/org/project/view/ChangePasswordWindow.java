package org.project.view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.project.functions.ChangePassword;

public class ChangePasswordWindow extends Application {

    private String userId;
    private Label messageLabel;

    public ChangePasswordWindow(String userId) {
        this.userId = userId;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Alterar Senha");

        VBox passwordContainer = new VBox();
        passwordContainer.getStyleClass().add("auth-container");

        Label newPasswordLabel = new Label("Nova Senha:");
        newPasswordLabel.getStyleClass().add("password-label");
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.getStyleClass().add("password-field");

        Button changePasswordButton = new Button("Alterar Senha");
        changePasswordButton.getStyleClass().add("verify-button");

        messageLabel = new Label();
        messageLabel.getStyleClass().add("change-label");

        changePasswordButton.setOnAction(e -> handleChangePassword(newPasswordField.getText()));

        passwordContainer.getChildren().addAll(newPasswordLabel, newPasswordField, changePasswordButton, messageLabel);

        Scene scene = new Scene(passwordContainer, 350, 220);
        String authCssFile = getClass().getResource("/org/project/styles/auth-styles.css").toExternalForm();
        scene.getStylesheets().add(authCssFile);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleChangePassword(String newPassword) {
        System.out.println("User ID: " + userId);

        ChangePassword changePassword = new ChangePassword();
        boolean isPasswordUpdated = changePassword.updatePassword(userId, newPassword);

        if (isPasswordUpdated) {
            messageLabel.setText("Senha alterada com sucesso.");
            messageLabel.setStyle("-fx-text-fill: green;"); // Define a cor do texto como verde para sucesso
        } else {
            messageLabel.setText("Falha ao alterar a senha.");
        }
    }
}
