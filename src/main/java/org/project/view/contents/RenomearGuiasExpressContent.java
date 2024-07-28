package org.project.view.contents;

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

import org.project.functions.RenomearGuias;

import java.io.File;

public class RenomearGuiasExpressContent extends VBox {

    private TextField folderPathField;
    private File selectedFolder;
    private String userId;

    public RenomearGuiasExpressContent(Stage primaryStage, String userId) {
        this.userId = userId;

        // Configura o layout principal
        setPadding(new Insets(20));
        setSpacing(15);
        getStyleClass().add("nome-das-fiscal-content");

        // VBox pai para centralização vertical
        VBox parentBox = new VBox();
        parentBox.setAlignment(Pos.CENTER);
        VBox.setVgrow(parentBox, Priority.ALWAYS); // Expande verticalmente

        // Título
        Label titleLabel = new Label("Renomear Guias Simples Nacional");
        titleLabel.setFont(new Font("Arial", 20));
        titleLabel.getStyleClass().add("title");
        VBox.setMargin(titleLabel, new Insets(0, 0, 50, 0)); // Adiciona margem inferior

        // Campo de texto para caminho do diretório
        folderPathField = new TextField();
        folderPathField.setPromptText("Selecione a pasta com os PDFs");
        folderPathField.getStyleClass().add("text-field");

        Button chooseFolderButton = new Button("...");
        chooseFolderButton.getStyleClass().add("select-button");
        chooseFolderButton.setOnAction(e -> chooseFolder(primaryStage));

        Button processButton = new Button("Processar");
        processButton.getStyleClass().add("process-button");
        processButton.setOnAction(e -> processPDFFiles(primaryStage));
        VBox.setMargin(processButton, new Insets(15, 0, 0, 0)); // Adiciona margem superior

        // Criação de HBox para alinhamento do campo de texto e botão
        HBox folderBox = new HBox(10, folderPathField, chooseFolderButton);
        folderBox.setAlignment(Pos.CENTER);
        folderBox.setMaxWidth(600); // Define uma largura máxima para o alinhamento
        VBox.setMargin(folderBox, new Insets(0, 0, 15, 0)); // Adiciona margem inferior

        // Adiciona os controles ao layout do VBox pai
        parentBox.getChildren().addAll(titleLabel, folderBox, processButton);
        getChildren().add(parentBox);
    }

    private void chooseFolder(Stage primaryStage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Selecione a pasta com os PDFs");
        selectedFolder = directoryChooser.showDialog(primaryStage);
        if (selectedFolder != null) {
            folderPathField.setText(selectedFolder.getAbsolutePath());
        }
    }

    private void processPDFFiles(Stage primaryStage) {
        if (selectedFolder != null) {
            RenomearGuias nomeDAS = new RenomearGuias();
            int renamedFiles = nomeDAS.processPDFsInFolder(selectedFolder.getAbsolutePath(), userId);
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", renamedFiles + " arquivo(s) foram renomeado(s).", primaryStage);
        } else {
            showAlert(Alert.AlertType.WARNING, "Atenção", "Por favor, selecione uma pasta contendo arquivos PDF.", primaryStage);
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
