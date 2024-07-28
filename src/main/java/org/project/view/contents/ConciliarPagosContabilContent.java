package org.project.view.contents;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.project.functions.ConciliarPagos;
import org.project.functions.ExportarPlanilha;

import java.io.File;
import java.util.List;

public class ConciliarPagosContabilContent extends VBox {

    private TextField filePathField1;
    private TextField filePathField2;
    private File selectedFile1;
    private File selectedFile2;
    private List<List<String>> reconciledData;

    public ConciliarPagosContabilContent(Stage primaryStage) {
        // Configura o layout principal
        setPadding(new Insets(20));
        setSpacing(15);
        getStyleClass().add("bancos-com-cod-content");

        // VBox pai para centralização vertical
        VBox parentBox = new VBox();
        parentBox.setAlignment(Pos.CENTER);
        VBox.setVgrow(parentBox, Priority.ALWAYS); // Expande verticalmente

        // Título
        Label titleLabel = new Label("Conciliar Pagamentos");
        titleLabel.setFont(new Font("Arial", 20));
        titleLabel.getStyleClass().add("title");
        VBox.setMargin(titleLabel, new Insets(0, 0, 50, 0)); // Adiciona margem inferior

        // Campo de texto para o caminho do primeiro arquivo
        filePathField1 = new TextField();
        filePathField1.setPromptText("Selecione o arquivo de Pagos");
        filePathField1.getStyleClass().add("text-field");
        filePathField1.setPrefWidth(300); // Define uma largura menor para o campo de caminho do arquivo

        Button chooseFileButton1 = new Button("...");
        chooseFileButton1.getStyleClass().add("select-button");
        chooseFileButton1.setOnAction(e -> chooseFile(primaryStage, filePathField1, 1));

        // Campo de texto para o caminho do segundo arquivo
        filePathField2 = new TextField();
        filePathField2.setPromptText("Selecione o arquivo do Banco");
        filePathField2.getStyleClass().add("text-field");
        filePathField2.setPrefWidth(300); // Define uma largura menor para o campo de caminho do arquivo

        Button chooseFileButton2 = new Button("...");
        chooseFileButton2.getStyleClass().add("select-button");
        chooseFileButton2.setOnAction(e -> chooseFile(primaryStage, filePathField2, 2));

        // Botão para processar a tabela
        Button processButton = new Button("Processar");
        processButton.getStyleClass().add("process-button");
        processButton.setOnAction(e -> processFiles());

        // Botão para exportar a tabela
        Button exportButton = new Button("Exportar");
        exportButton.getStyleClass().add("export-button");
        exportButton.setOnAction(e -> exportTableData(primaryStage));

        // Adiciona os controles ao layout do VBox pai
        HBox fileBox1 = new HBox(10, filePathField1, chooseFileButton1);
        fileBox1.setAlignment(Pos.CENTER);
        fileBox1.setMaxWidth(500);
        fileBox1.getStyleClass().add("file-box");

        HBox fileBox2 = new HBox(10, filePathField2, chooseFileButton2);
        fileBox2.setAlignment(Pos.CENTER);
        fileBox2.setMaxWidth(500);
        fileBox2.getStyleClass().add("file-box");

        HBox buttonBox = new HBox(10, processButton, exportButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getStyleClass().add("button-box");

        parentBox.getChildren().addAll(titleLabel, fileBox1, fileBox2, buttonBox);
        getChildren().add(parentBox);
    }

    private void chooseFile(Stage primaryStage, TextField filePathField, int fileIndex) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Escolha um arquivo");
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null) {
            filePathField.setText(selectedFile.getAbsolutePath());
            if (fileIndex == 1) {
                selectedFile1 = selectedFile;
            } else if (fileIndex == 2) {
                selectedFile2 = selectedFile;
            }
        }
    }

    private void processFiles() {
        if (selectedFile1 != null && selectedFile2 != null) {
            reconciledData = ConciliarPagos.conciliar(selectedFile1, selectedFile2);
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Os arquivos foram processados com sucesso.");
        } else {
            showAlert(Alert.AlertType.WARNING, "Atenção", "Por favor, selecione ambos os arquivos antes de processar.");
        }
    }

    private void exportTableData(Stage primaryStage) {
        if (reconciledData != null && !reconciledData.isEmpty()) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Salvar Arquivo CSV");
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showSaveDialog(primaryStage);

            if (file != null) {
                ExportarPlanilha.exportConciliacao(reconciledData, file);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Os dados foram exportados com sucesso.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Atenção", "Por favor, processe os arquivos antes de exportar.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
