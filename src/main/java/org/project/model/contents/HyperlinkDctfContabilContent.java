package org.project.model.contents;

import org.project.functions.HyperlinkDctfContabil;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.text.Font;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Priority;

import java.io.File;

public class HyperlinkDctfContabilContent extends VBox {

    private TextField filePathField;
    private TextField folderPathField;
    private File selectedFile;
    private File selectedFolder;

    public HyperlinkDctfContabilContent(Stage primaryStage) {
        // Configura o layout principal
        setPadding(new Insets(20));
        setSpacing(15);
        getStyleClass().add("hyperlink-dctf-contabil-content");

        // VBox pai para centralização vertical
        VBox parentBox = new VBox();
        parentBox.setAlignment(Pos.CENTER);
        VBox.setVgrow(parentBox, Priority.ALWAYS); // Expande verticalmente

        // Título
        Label titleLabel = new Label("Hyperlink DCTF");
        titleLabel.setFont(new Font("Arial", 20));
        titleLabel.getStyleClass().add("title");
        VBox.setMargin(titleLabel, new Insets(0, 0, 50, 0)); // Adiciona margem inferior

        // Labels e campos de texto
        filePathField = new TextField();
        filePathField.setPromptText("Selecione a planilha Excel");
        filePathField.getStyleClass().add("text-field");

        Button selectFileButton = new Button("...");
        selectFileButton.getStyleClass().add("select-button");
        selectFileButton.setOnAction(e -> selectFile(primaryStage));

        folderPathField = new TextField();
        folderPathField.setPromptText("Selecione a pasta com arquivos");
        folderPathField.getStyleClass().add("text-field");

        Button selectFolderButton = new Button("...");
        selectFolderButton.getStyleClass().add("select-button");
        selectFolderButton.setOnAction(e -> selectFolder(primaryStage));

        Button executeButton = new Button("Executar");
        executeButton.getStyleClass().add("execute-button");
        executeButton.setOnAction(e -> executeComparison(primaryStage));
        VBox.setMargin(executeButton, new Insets(15, 0, 0, 0)); // Adiciona margem superior

        // Criação de HBoxes para alinhamento dos campos de texto e botões
        HBox fileBox = new HBox(10, filePathField, selectFileButton);
        fileBox.setAlignment(Pos.CENTER);
        fileBox.setMaxWidth(600); // Define uma largura máxima para o alinhamento
        VBox.setMargin(fileBox, new Insets(0, 0, 15, 0)); // Adiciona margem inferior

        HBox folderBox = new HBox(10, folderPathField, selectFolderButton);
        folderBox.setAlignment(Pos.CENTER);
        folderBox.setMaxWidth(600); // Define uma largura máxima para o alinhamento
        VBox.setMargin(folderBox, new Insets(0, 0, 15, 0)); // Adiciona margem inferior

        // Adiciona os controles ao layout do VBox pai
        parentBox.getChildren().addAll(titleLabel, fileBox, folderBox, executeButton);
        getChildren().add(parentBox);
    }

    private void selectFile(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos Excel", "*.xlsx", "*.xls"));
        selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null) {
            filePathField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void selectFolder(Stage primaryStage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        selectedFolder = directoryChooser.showDialog(primaryStage);
        if (selectedFolder != null) {
            folderPathField.setText(selectedFolder.getAbsolutePath());
        }
    }

    private void executeComparison(Stage primaryStage) {
        if (selectedFile == null || selectedFolder == null) {
            showAlert(AlertType.WARNING, "Atenção", "Por favor, selecione a planilha e a pasta.", primaryStage);
            return;
        }

        // Use the constructor with both file and folder
        HyperlinkDctfContabil processor = new HyperlinkDctfContabil(selectedFile, selectedFolder);
        processor.processFiles(primaryStage);
    }

    private void showAlert(AlertType alertType, String title, String message, Stage primaryStage) {
        Alert alert = new Alert(alertType);
        alert.initOwner(primaryStage);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
