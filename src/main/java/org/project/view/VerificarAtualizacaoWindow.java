package org.project.view;

import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import org.project.functions.VerificarAtualizacao;

import java.io.File;

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
    private Label loadingIndicator;

    public static void setVersions(String actual, String newV, String link) {
        actualVersion = actual;
        newVersion = newV;
        downloadLink = link;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Verificar Atualização");

        // Adicionar o ícone
        String iconPath = "/org/project/images/icon.ico";
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream(iconPath)));

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
        String quanttumPath = "C:\\Quanttum\\quanttum.jar";
        filePathField.setText(quanttumPath);
        filePathField.getStyleClass().add("text-field");

        Button chooseDirButton = new Button("...");
        chooseDirButton.getStyleClass().add("select-button");
        chooseDirButton.setOnAction(e -> showDirectoryChooser(primaryStage));

        // Botão de download
        downloadButton = new Button("Baixar");
        downloadButton.getStyleClass().add("download-button");
        downloadButton.setOnAction(e -> startDownload());

        // Indicador de carregamento
        loadingIndicator = createLoadingIndicator();
        loadingIndicator.setVisible(false); // Inicialmente invisível

        HBox fileBox = new HBox(10, filePathField, chooseDirButton);
        fileBox.setAlignment(Pos.CENTER); // Alinha horizontalmente no centro

        // Layout horizontal para botões
        HBox buttonBox = new HBox(10, downloadButton);
        buttonBox.setAlignment(Pos.CENTER); // Alinha horizontalmente no centro

        // Layout vertical
        VBox vbox = new VBox(10, titleLabel, currentVersionLabel, newVersionLabel, fileBox, buttonBox, loadingIndicator, messageLabel, changeLabel);
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

        // Exibir o indicador de carregamento e mensagem de progresso
        loadingIndicator.setVisible(true);
        messageLabel.setText("Download em progresso...");

        // Iniciar o download em uma nova thread
        new Thread(() -> {
            // Baixar o arquivo para o caminho especificado
            VerificarAtualizacao.downloadFile(downloadLink, filePath);

            // Atualizar a interface gráfica no FX Application Thread
            Platform.runLater(() -> {
                loadingIndicator.setVisible(false); // Esconder o indicador de carregamento
                messageLabel.setText("Download concluído!");
            });
        }).start();
    }

    private Label createLoadingIndicator() {
        Label indicator = new Label();
        indicator.setText("°");
        indicator.setStyle("-fx-font-size: 20; -fx-text-fill: gray;");

        // Animação de rotação
        RotateTransition rotateTransition = new RotateTransition(Duration.seconds(1), indicator);
        rotateTransition.setFromAngle(0);
        rotateTransition.setToAngle(360);
        rotateTransition.setCycleCount(RotateTransition.INDEFINITE);

        rotateTransition.play();

        return indicator;
    }

}
