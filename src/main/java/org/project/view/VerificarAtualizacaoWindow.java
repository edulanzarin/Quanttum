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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class VerificarAtualizacaoWindow extends Application {

    private static String actualVersion;
    private static String newVersion;
    private static String downloadLink;

    private Label currentVersionLabel;
    private Label newVersionLabel;
    private Label messageLabel;
    private Label changeLabel;
    private TextField filePathField;
    private Button downloadButton;
    private Button updateButton;

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
        filePathField.setText("C:\\Program Files\\Quanttum\\quanttum.jar"); // Define o caminho padrão para o diretório especificado
        filePathField.getStyleClass().add("text-field");

        Button chooseDirButton = new Button("...");
        chooseDirButton.getStyleClass().add("select-button");
        chooseDirButton.setOnAction(e -> showDirectoryChooser(primaryStage));

        // Botão de download
        downloadButton = new Button("Baixar");
        downloadButton.getStyleClass().add("download-button");
        downloadButton.setOnAction(e -> startDownload());

        HBox fileBox = new HBox(10, filePathField, chooseDirButton);
        fileBox.setAlignment(Pos.CENTER); // Alinha horizontalmente no centro

        // Layout horizontal para botões
        HBox buttonBox = new HBox(10, downloadButton);
        buttonBox.setAlignment(Pos.CENTER); // Alinha horizontalmente no centro

        // Layout vertical
        VBox vbox = new VBox(10, titleLabel, currentVersionLabel, newVersionLabel, fileBox, buttonBox, messageLabel, changeLabel);
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
        DirectoryChooser directoryChooser = new DirectoryChooser();
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
            });
        }).start();

        // Atualizar a mensagem
        Platform.runLater(() -> messageLabel.setText("Download em progresso..."));
    }

    private void executeUpdateScript() {
        try {
            String batFilePath = "C:\\Program Files\\Quanttum\\move.bat";
            File batFile = new File(batFilePath);

            if (batFile.exists()) {
                ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", batFilePath);
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();

                try (InputStream inputStream = process.getInputStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                }

                process.waitFor();

                messageLabel.setText("Atualização concluída!");
            } else {
                messageLabel.setText("Arquivo move.bat não encontrado na pasta especificada.");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            messageLabel.setText("Erro ao executar o arquivo move.bat.");
        }
    }
}
