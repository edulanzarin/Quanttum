package org.project.model.contents;

import org.project.functions.MoverArquivosExpress;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.layout.Priority;

import java.io.File;

public class MoverArquivosExpressContent extends VBox {

    private TextField rootPathField;
    private TextField destPathField;
    private File rootDirectory;
    private File destDirectory;

    public MoverArquivosExpressContent(Stage primaryStage) {
        // Configura o layout principal
        setPadding(new Insets(20));
        setSpacing(15);
        getStyleClass().add("mover-arquivos-express-content");

        // VBox pai para centralização vertical
        VBox parentBox = new VBox();
        parentBox.setAlignment(Pos.CENTER);
        VBox.setVgrow(parentBox, Priority.ALWAYS); // Expande verticalmente

        // Título
        Label titleLabel = new Label("Mover Arquivos");
        titleLabel.setFont(new Font("Arial", 20));
        titleLabel.getStyleClass().add("title");
        VBox.setMargin(titleLabel, new Insets(0, 0, 50, 0)); // Adiciona margem inferior

        // Campos de texto
        rootPathField = new TextField();
        rootPathField.setPromptText("Selecione a pasta raiz");
        rootPathField.getStyleClass().add("text-field");

        Button chooseRootButton = new Button("...");
        chooseRootButton.getStyleClass().add("select-button");
        chooseRootButton.setOnAction(e -> chooseRootDirectory(primaryStage));

        destPathField = new TextField();
        destPathField.setPromptText("Selecione a pasta de destino");
        destPathField.getStyleClass().add("text-field");

        Button chooseDestButton = new Button("...");
        chooseDestButton.getStyleClass().add("select-button");
        chooseDestButton.setOnAction(e -> chooseDestDirectory(primaryStage));

        Button processButton = new Button("Processar");
        processButton.getStyleClass().add("process-button");
        processButton.setOnAction(e -> processDirectories(primaryStage));
        VBox.setMargin(processButton, new Insets(15, 0, 0, 0)); // Adiciona margem superior

        // Criação de HBoxes para alinhamento dos campos de texto e botões
        HBox rootBox = new HBox(10, rootPathField, chooseRootButton);
        rootBox.setAlignment(Pos.CENTER);
        rootBox.setMaxWidth(600); // Define uma largura máxima para o alinhamento
        VBox.setMargin(rootBox, new Insets(0, 0, 15, 0)); // Adiciona margem inferior

        HBox destBox = new HBox(10, destPathField, chooseDestButton);
        destBox.setAlignment(Pos.CENTER);
        destBox.setMaxWidth(600); // Define uma largura máxima para o alinhamento
        VBox.setMargin(destBox, new Insets(0, 0, 15, 0)); // Adiciona margem inferior

        // Adiciona os controles ao layout do VBox pai
        parentBox.getChildren().addAll(titleLabel, rootBox, destBox, processButton);
        getChildren().add(parentBox);
    }

    private void chooseRootDirectory(Stage primaryStage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        rootDirectory = directoryChooser.showDialog(primaryStage);
        if (rootDirectory != null) {
            rootPathField.setText(rootDirectory.getAbsolutePath());
        }
    }

    private void chooseDestDirectory(Stage primaryStage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        destDirectory = directoryChooser.showDialog(primaryStage);
        if (destDirectory != null) {
            destPathField.setText(destDirectory.getAbsolutePath());
        }
    }

    private void processDirectories(Stage primaryStage) {
        if (rootDirectory != null && destDirectory != null) {
            MoverArquivosExpress mover = new MoverArquivosExpress(rootDirectory, destDirectory);
            mover.copiarArquivos(); // Atualize para chamar o método correto
        } else {
            showAlert(Alert.AlertType.WARNING, "Atenção", "Por favor, selecione as pastas.", primaryStage);
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message, Stage primaryStage) {
        Alert alert = new Alert(alertType);
        alert.initOwner(primaryStage);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
