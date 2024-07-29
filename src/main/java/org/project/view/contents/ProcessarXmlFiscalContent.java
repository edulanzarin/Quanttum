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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.layout.Priority;
import org.project.functions.ProcessarXmlFiscal;

import java.io.File;

public class ProcessarXmlFiscalContent extends VBox {

    private TextField xmlFolderPathField;
    private TextField camposDesejadosField;
    private File xmlFolder;
    private String userId;

    public ProcessarXmlFiscalContent(Stage primaryStage, String userId) {
        this.userId = userId;

        // Configura o layout principal
        setPadding(new Insets(20));
        setSpacing(15);
        getStyleClass().add("processar-xml-fiscal-content");

        // VBox pai para centralização vertical
        VBox parentBox = new VBox();
        parentBox.setAlignment(Pos.CENTER);
        VBox.setVgrow(parentBox, Priority.ALWAYS); // Expande verticalmente

        // Título
        Label titleLabel = new Label("Gerar Planilha");
        titleLabel.setFont(new Font("Arial", 20));
        titleLabel.getStyleClass().add("title");
        VBox.setMargin(titleLabel, new Insets(0, 0, 50, 0)); // Adiciona margem inferior

        // Campo de texto para caminho do diretório XML
        xmlFolderPathField = new TextField();
        xmlFolderPathField.setPromptText("Selecione a pasta com os XMLs");
        xmlFolderPathField.getStyleClass().add("text-field");

        Button chooseXmlFolderButton = new Button("...");
        chooseXmlFolderButton.getStyleClass().add("select-button");
        chooseXmlFolderButton.setOnAction(e -> chooseXmlFolder(primaryStage));

        // Campo de texto para os campos desejados
        camposDesejadosField = new TextField();
        camposDesejadosField.setPromptText("Digite os campos desejados separados por ';'");
        camposDesejadosField.getStyleClass().add("dados-field");
        camposDesejadosField.setPrefWidth(450); // Define a largura máxima

        Button processButton = new Button("Processar");
        processButton.getStyleClass().add("process-button");
        processButton.setOnAction(e -> processXmlFiles(primaryStage));
        VBox.setMargin(processButton, new Insets(15, 0, 0, 0)); // Adiciona margem superior

        // Criação de HBox para alinhamento do campo de texto e botão
        HBox xmlFolderBox = new HBox(10, xmlFolderPathField, chooseXmlFolderButton);
        xmlFolderBox.setAlignment(Pos.CENTER);
        xmlFolderBox.setMaxWidth(600); // Define uma largura máxima para o alinhamento
        VBox.setMargin(xmlFolderBox, new Insets(0, 0, 15, 0)); // Adiciona margem inferior

        // Criação de HBox para alinhamento do campo de texto dos campos desejados
        HBox camposBox = new HBox(camposDesejadosField);
        camposBox.setAlignment(Pos.CENTER);
        camposBox.setMaxWidth(600); // Define uma largura máxima para o alinhamento
        VBox.setMargin(camposBox, new Insets(0, 0, 15, 0)); // Adiciona margem inferior

        // Adiciona os controles ao layout do VBox pai
        parentBox.getChildren().addAll(titleLabel, xmlFolderBox, camposBox, processButton);
        getChildren().add(parentBox);
    }

    private void chooseXmlFolder(Stage primaryStage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Selecione a pasta com os XMLs");
        xmlFolder = directoryChooser.showDialog(primaryStage);
        if (xmlFolder != null) {
            xmlFolderPathField.setText(xmlFolder.getAbsolutePath());
        }
    }

    private void processXmlFiles(Stage primaryStage) {
        if (xmlFolder != null) {
            String camposDesejados = camposDesejadosField.getText();
            if (camposDesejados.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Atenção", "Por favor, informe os campos desejados.", primaryStage);
                return;
            }

            // Abrir diálogo para escolher onde salvar o arquivo CSV
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            // Define um nome padrão para o arquivo
            File saveFile = fileChooser.showSaveDialog(primaryStage);
            if (saveFile != null) {
                String caminhoCsv = saveFile.getAbsolutePath();
                // Adiciona extensão .csv caso o usuário não o faça
                if (!caminhoCsv.toLowerCase().endsWith(".csv")) {
                    caminhoCsv += ".csv";
                }
                ProcessarXmlFiscal.processarPasta(camposDesejados, xmlFolder.getAbsolutePath(), caminhoCsv, userId);

                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Os arquivos XML foram processados com sucesso.", primaryStage);
            } else {
                showAlert(Alert.AlertType.WARNING, "Atenção", "Por favor, selecione um local para salvar o arquivo CSV.", primaryStage);
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Atenção", "Por favor, selecione uma pasta contendo arquivos XML.", primaryStage);
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
