package org.project.view;

import javafx.application.Application;
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

public class VerificarAtualizacaoWindow extends Application {

    private static String actualVersion;
    private static String newVersion;
    private static String downloadLink;

    private Label currentVersionLabel;
    private Label newVersionLabel;
    private ProgressBar progressBar;
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

        progressBar = new ProgressBar(0);
        progressBar.getStyleClass().add("progress-bar");

        messageLabel = new Label();
        messageLabel.getStyleClass().add("message-label");

        changeLabel = new Label();
        changeLabel.getStyleClass().add("change-label");

        filePathField = new TextField();
        filePathField.setPromptText("Selecione um diretório");
        filePathField.setPrefWidth(300);
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
        VBox vbox = new VBox(10, titleLabel, currentVersionLabel, newVersionLabel, fileBox, downloadButton, progressBar, messageLabel, changeLabel);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER); // Alinha verticalmente no centro
        vbox.getStyleClass().add("update-container");

        // Scene e estilos
        Scene scene = new Scene(vbox, 400, 300);
        scene.getStylesheets().add(getClass().getResource("/org/project/styles/auth-styles.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showDirectoryChooser(Window owner) {
        directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Escolher Diretório de Download");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Downloads"));

        File directory = directoryChooser.showDialog(owner);
        if (directory != null) {
            filePathField.setText(directory.getPath() + File.separator + "quanttum.jar");
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
            progressBar.setProgress(1.0);  // Defina como concluído
            messageLabel.setText("Download concluído!");
        }).start();

        // Atualizar a barra de progresso e mensagens conforme necessário
        messageLabel.setText("Download em progresso...");
    }
}
