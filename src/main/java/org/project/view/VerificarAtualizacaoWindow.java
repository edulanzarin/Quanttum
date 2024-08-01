package org.project.view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.project.functions.VerificarAtualizacao;

import java.io.File;
import java.io.IOException;

public class VerificarAtualizacaoWindow extends Application {

    private static String actualVersion;
    private static String newVersion;
    private static String downloadLink;

    private Label currentVersionLabel;
    private Label newVersionLabel;
    private Label messageLabel;
    private Label changeLabel;
    private DirectoryChooser directoryChooser;
    private TextField filePathField;
    private Button downloadButton;

    public static void setVersions(String actual, String newV, String link) {
        actualVersion = actual;
        newVersion = newV;
        downloadLink = link;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Verificar Atualização");

        // Título
        Label titleLabel = new Label("Atualizar Versão");
        titleLabel.setFont(new Font("Arial", 20));
        titleLabel.getStyleClass().add("title");

        // Criação dos componentes
        currentVersionLabel = new Label("Versão Atual: " + actualVersion);
        currentVersionLabel.getStyleClass().add("label");

        newVersionLabel = new Label("Nova Versão: " + newVersion);
        newVersionLabel.getStyleClass().add("label");

        messageLabel = new Label();
        messageLabel.getStyleClass().add("message-label");

        changeLabel = new Label();
        changeLabel.getStyleClass().add("change-label");

        filePathField = new TextField();
        filePathField.setPromptText("Selecione um diretório");
        filePathField.setPrefWidth(300);
        filePathField.setText("C:\\Program Files\\Quanttum\\quanttum.jar"); // Define o caminho padrão
        filePathField.getStyleClass().add("text-field");

        Button chooseDirButton = new Button("...");
        chooseDirButton.getStyleClass().add("select-button");
        chooseDirButton.setOnAction(e -> showDirectoryChooser(primaryStage));

        // Botão de download
        downloadButton = new Button("Baixar");
        downloadButton.getStyleClass().add("download-button");
        downloadButton.setOnAction(e -> startDownload());

        // Layout horizontal para campo de texto e botão
        HBox fileBox = new HBox(10, filePathField, chooseDirButton);
        fileBox.setAlignment(Pos.CENTER); // Alinha horizontalmente no centro

        // Layout vertical
        VBox vbox = new VBox(10, titleLabel, currentVersionLabel, newVersionLabel, fileBox, downloadButton, messageLabel, changeLabel);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER); // Alinha verticalmente no centro
        vbox.getStyleClass().add("update-container");

        // Scene
        Scene scene = new Scene(vbox, 400, 300);
        scene.getStylesheets().add(getClass().getResource("/org/project/styles/auth-styles.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showDirectoryChooser(Window window) {
        directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Selecione o Diretório de Download");
        File selectedDirectory = directoryChooser.showDialog(window);
        if (selectedDirectory != null) {
            filePathField.setText(selectedDirectory.getAbsolutePath() + "\\quanttum.jar");
        }
    }

    private void startDownload() {
        String filePath = filePathField.getText();
        if (filePath == null || filePath.isEmpty()) {
            messageLabel.setText("Por favor, selecione um diretório de download.");
            return;
        }

        // Iniciar o download
        new Thread(() -> {
            VerificarAtualizacao.downloadFile(downloadLink, filePath);

            // Atualizar a interface gráfica no FX Application Thread
            Platform.runLater(() -> {
                messageLabel.setText("Download concluído!");

                // Atualizar a versão no arquivo JSON
                try {
                    VerificarAtualizacao.writeVersionToFile(newVersion);

                    // Substituir o JAR atual e reiniciar a aplicação
                    File downloadedJar = new File(filePath);
                    File targetJar = new File("C:\\Program Files\\Quanttum\\quanttum.jar");
                    VerificarAtualizacao.replaceCurrentJar(downloadedJar, targetJar);
                    VerificarAtualizacao.restartApplication();
                } catch (IOException e) {
                    e.printStackTrace();
                    messageLabel.setText("Erro ao substituir o arquivo JAR ou reiniciar a aplicação.");
                }
            });
        }).start();

        // Atualizar a mensagem
        Platform.runLater(() -> messageLabel.setText("Download em progresso..."));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
