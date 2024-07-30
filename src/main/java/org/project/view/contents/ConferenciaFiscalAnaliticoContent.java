package org.project.view.contents;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.project.functions.ConferenciaFiscalAnalitico;

import java.io.File;

public class ConferenciaFiscalAnaliticoContent extends VBox {

    private TextField filePathField;
    private File selectedDirectory;

    public ConferenciaFiscalAnaliticoContent(Stage primaryStage) {
        // Configura o layout principal
        setPadding(new Insets(20));
        setSpacing(15);
        getStyleClass().add("conferencia-fiscal-contabil-content");

        // VBox pai para centralização vertical
        VBox parentBox = new VBox();
        parentBox.setAlignment(Pos.CENTER);
        VBox.setVgrow(parentBox, Priority.ALWAYS);

        // Título
        Label titleLabel = new Label("Conferência Fiscal Contábil");
        titleLabel.setFont(new Font("Arial", 20));
        titleLabel.getStyleClass().add("title");
        VBox.setMargin(titleLabel, new Insets(0, 0, 50, 0));

        // Campo de texto para o caminho da pasta
        filePathField = new TextField();
        filePathField.setPromptText("Selecione uma pasta");
        filePathField.getStyleClass().add("text-field");
        filePathField.setPrefWidth(300);

        // Botão para escolher pasta
        Button chooseDirectoryButton = new Button("...");
        chooseDirectoryButton.getStyleClass().add("select-button");
        chooseDirectoryButton.setOnAction(e -> chooseDirectory(primaryStage));

        // Botão para processar a pasta
        Button processButton = new Button("Processar");
        processButton.getStyleClass().add("process-button");
        processButton.setOnAction(e -> processDirectory(primaryStage));
        VBox.setMargin(processButton, new Insets(15, 0, 50, 0));

        // Criação de HBox para alinhamento do campo de texto e botão do caminho da pasta
        HBox directoryBox = new HBox(10, filePathField, chooseDirectoryButton);
        directoryBox.setAlignment(Pos.CENTER);
        directoryBox.setMaxWidth(500);
        VBox.setMargin(directoryBox, new Insets(0, 0, 15, 0));

        // Adiciona os controles ao layout do VBox pai
        parentBox.getChildren().addAll(titleLabel, directoryBox, processButton);
        getChildren().add(parentBox);
    }

    private void chooseDirectory(Stage primaryStage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Escolha uma pasta");
        selectedDirectory = directoryChooser.showDialog(primaryStage);
        if (selectedDirectory != null) {
            filePathField.setText(selectedDirectory.getAbsolutePath());
        }
    }

    private void processDirectory(Stage primaryStage) {
        if (selectedDirectory == null) {
            showAlert(Alert.AlertType.WARNING, "Atenção", "Por favor, selecione uma pasta.");
            return;
        }

        // Chama a função para processar a pasta selecionada
        ConferenciaFiscalAnalitico.processarPasta(selectedDirectory);

        // Mostra mensagem de sucesso
        showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Pasta processada com sucesso.");
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
