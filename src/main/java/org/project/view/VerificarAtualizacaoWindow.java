package org.project.view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.project.functions.VerificarAtualizacao;

public class VerificarAtualizacaoWindow extends Application {

    private static String actualVersion;
    private static String newVersion;
    private static String downloadLink;

    private Label currentVersionLabel;
    private Label newVersionLabel;
    private Label messageLabel;
    private Button downloadButton;
    private Button finishButton;
    private ProgressBar progressBar;

    private static final String DEFAULT_SAVE_PATH = "C://Quanttum/quanttum.jar";

    public static void setVersions(String actual, String newV, String link) {
        actualVersion = actual;
        newVersion = newV;
        downloadLink = link;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Atualização Disponível");

        // Adicionar o ícone
        String iconPath = "/org/project/images/icon.ico";
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream(iconPath)));

        // Título
        Label titleLabel = new Label("Atualizar Versão");
        titleLabel.setFont(new Font("Arial", 16));
        titleLabel.getStyleClass().add("title");

        // Criação dos componentes
        currentVersionLabel = new Label("Versão Atual: " + actualVersion);
        currentVersionLabel.getStyleClass().add("label");

        newVersionLabel = new Label("Nova Versão: " + newVersion);
        newVersionLabel.getStyleClass().add("label");

        messageLabel = new Label();
        messageLabel.getStyleClass().add("message-label");

        // Botão de download
        downloadButton = new Button("Baixar");
        downloadButton.getStyleClass().add("button");
        downloadButton.setOnAction(e -> startDownload(downloadLink, DEFAULT_SAVE_PATH));

        // Botão de concluir
        finishButton = new Button("Concluir");
        finishButton.getStyleClass().add("button");
        finishButton.setDisable(true);
        finishButton.setOnAction(e -> primaryStage.close());

        // Barra de progresso
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300); // Ajuste o tamanho conforme necessário
        progressBar.getStyleClass().add("progress-bar");

        // Layout
        BorderPane borderPane = new BorderPane();

        VBox topLayout = new VBox(5); // Reduzi o espaçamento
        topLayout.setPadding(new Insets(20));
        topLayout.setAlignment(Pos.CENTER);
        topLayout.getChildren().addAll(titleLabel, currentVersionLabel, newVersionLabel, progressBar, messageLabel);

        HBox bottomLayout = new HBox(10);
        bottomLayout.setPadding(new Insets(10, 0, 0, 0)); // Remover padding superior
        bottomLayout.setAlignment(Pos.CENTER);
        bottomLayout.getChildren().addAll(downloadButton, finishButton);

        borderPane.setTop(topLayout);
        borderPane.setBottom(bottomLayout);

        Scene scene = new Scene(borderPane, 400, 300);
        scene.getStylesheets().add("/org/project/styles/update-styles.css");

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void startDownload(String url, String savePath) {
        new Thread(() -> VerificarAtualizacao.downloadFile(url, savePath, progress -> {
            Platform.runLater(() -> progressBar.setProgress(progress));
            if (progress >= 1.0) {
                Platform.runLater(() -> {
                    messageLabel.setText("Download concluído!");
                    finishButton.setDisable(false); // Habilitar o botão "Concluir"
                });
            }
        })).start();
    }
}
