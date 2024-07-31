package org.project.view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.project.functions.AtualizarVersao;

import java.nio.file.Paths;

public class AtualizarVersaoWindow extends Application {

    private Label messageLabel;
    private ProgressBar progressBar;
    private Button downloadButton;
    private String defaultDirectory = "C:\\Program Files\\Quanttum"; // Diretório padrão
    private String configFilePath = "org/project/resources/config.properties"; // Caminho para o config.properties

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Verificando Atualização...");

        AtualizarVersao atualizarVersao = new AtualizarVersao(configFilePath);

        // Verifica a versão antes de abrir a janela de atualização
        Task<Void> versionCheckTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                atualizarVersao.fetchUpdateInfoFromSheet(() -> {
                    Platform.runLater(() -> {
                        try {
                            String remoteVersion = atualizarVersao.getRemoteVersion();
                            String downloadUrl = atualizarVersao.getDownloadUrl();
                            atualizarVersao.setRemoteVersion(remoteVersion);
                            atualizarVersao.setDownloadUrl(downloadUrl);

                            if (atualizarVersao.isUpdateAvailable(remoteVersion, atualizarVersao.getLocalVersion())) {
                                showUpdateWindow(primaryStage, atualizarVersao, remoteVersion, downloadUrl);
                            } else {
                                Platform.runLater(() -> {
                                    try {
                                        new LoginWindow().start(new Stage());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    primaryStage.close();
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                });
                return null;
            }
        };

        new Thread(versionCheckTask).start();
    }

    private void showUpdateWindow(Stage primaryStage, AtualizarVersao atualizarVersao, String remoteVersion, String downloadUrl) {
        VBox updateContainer = new VBox();
        updateContainer.getStyleClass().add("update-container");

        Label titleLabel = new Label("Atualização Disponível");
        titleLabel.getStyleClass().add("title-label");

        messageLabel = new Label();
        messageLabel.getStyleClass().add("message-label");

        progressBar = new ProgressBar(0);
        progressBar.getStyleClass().add("progress-bar");

        downloadButton = new Button("Baixar Atualização");
        downloadButton.getStyleClass().add("download-button");

        downloadButton.setOnAction(e -> iniciarAtualizacao(primaryStage, atualizarVersao, remoteVersion, downloadUrl));

        updateContainer.getChildren().addAll(titleLabel, messageLabel, progressBar, downloadButton);

        Scene scene = new Scene(updateContainer, 400, 300);
        String updateCssFile = getClass().getResource("/org/project/styles/auth-styles.css").toExternalForm();
        scene.getStylesheets().add(updateCssFile);

        Platform.runLater(() -> {
            primaryStage.setScene(scene);
            primaryStage.show();
        });
    }

    private void iniciarAtualizacao(Stage primaryStage, AtualizarVersao atualizarVersao, String remoteVersion, String downloadUrl) {
        String savePath = Paths.get(defaultDirectory, "new-version.jar").toString();
        atualizarVersao.setSavePath(savePath);
        atualizarVersao.setRemoteVersion(remoteVersion);
        atualizarVersao.setDownloadUrl(downloadUrl);

        Task<Void> updateTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                atualizarVersao.iniciarAtualizacao(progressBar, downloadButton, messageLabel, primaryStage);
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                Platform.runLater(() -> {
                    messageLabel.setText("Atualização concluída. Abrindo Login Window...");
                    primaryStage.close();
                    try {
                        new LoginWindow().start(new Stage()); // Certifique-se de que LoginWindow é uma classe existente
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            protected void failed() {
                super.failed();
                Platform.runLater(() -> {
                    messageLabel.setText("Falha na atualização.");
                    downloadButton.setDisable(false);
                    progressBar.setVisible(false);
                });
            }

            @Override
            protected void cancelled() {
                super.cancelled();
                Platform.runLater(() -> {
                    messageLabel.setText("Atualização cancelada.");
                    downloadButton.setDisable(false);
                    progressBar.setVisible(false);
                });
            }
        };

        new Thread(updateTask).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
