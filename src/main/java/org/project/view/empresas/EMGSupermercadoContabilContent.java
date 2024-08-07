package org.project.view.empresas;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Alert;
import org.project.functions.empresas.EMGSupermercadoContabil;

import java.io.*;
import java.util.List;

public class EMGSupermercadoContabilContent extends VBox {

    private TextField filePathField;
    private TableView<Transaction> tableView;
    private File selectedFile;

    public EMGSupermercadoContabilContent(Stage primaryStage) {
        // Configura o layout principal
        setPadding(new Insets(20));
        setSpacing(15);
        getStyleClass().add("supermercadojk-content");

        // VBox pai para centralização vertical
        VBox parentBox = new VBox();
        parentBox.setAlignment(Pos.CENTER);
        VBox.setVgrow(parentBox, Priority.ALWAYS); // Expande verticalmente

        // Título
        Label titleLabel = new Label("EMG Supermercado Contábil");
        titleLabel.setFont(new Font("Arial", 20));
        titleLabel.getStyleClass().add("title");
        VBox.setMargin(titleLabel, new Insets(0, 0, 20, 0)); // Adiciona margem inferior

        // Campo de texto para o caminho do arquivo
        filePathField = new TextField();
        filePathField.setPromptText("Caminho do arquivo");
        filePathField.getStyleClass().add("text-field");
        filePathField.setPrefWidth(300);
        filePathField.setDisable(true); // Desabilitado para edição

        Button chooseFileButton = new Button("...");
        chooseFileButton.getStyleClass().add("select-button");
        chooseFileButton.setOnAction(e -> chooseFile(primaryStage));

        Button processButton = new Button("Processar");
        processButton.getStyleClass().add("process-button");
        processButton.setOnAction(e -> processFile(primaryStage));

        Button exportButton = new Button("Exportar");
        exportButton.getStyleClass().add("export-button");
        exportButton.setOnAction(e -> exportTableData(primaryStage));

        // Configuração da tabela
        tableView = new TableView<>();
        tableView.setPrefWidth(792);
        tableView.setMaxWidth(792);

        TableColumn<Transaction, String> dateColumn = new TableColumn<>("Data");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateColumn.setPrefWidth(165);

        TableColumn<Transaction, String> descriptionColumn = new TableColumn<>("Descrição");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionColumn.setPrefWidth(410);

        TableColumn<Transaction, String> valueColumn = new TableColumn<>("Valor");
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        valueColumn.setPrefWidth(210);

        tableView.getColumns().addAll(dateColumn, descriptionColumn, valueColumn);

        // Adiciona os controles ao layout do VBox pai
        HBox fileBox = new HBox(10, filePathField, chooseFileButton);
        fileBox.setAlignment(Pos.CENTER);
        VBox.setMargin(fileBox, new Insets(0, 0, 15, 0));

        HBox buttonBox = new HBox(10, processButton, exportButton);
        buttonBox.setAlignment(Pos.CENTER);
        VBox.setMargin(buttonBox, new Insets(15, 0, 50, 0));

        parentBox.getChildren().addAll(titleLabel, fileBox, buttonBox, tableView);
        getChildren().add(parentBox);
    }

    private void chooseFile(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Escolher Arquivo PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos PDF", "*.pdf"));
        selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            filePathField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void processFile(Stage primaryStage) {
        if (selectedFile == null) {
            showAlert(Alert.AlertType.WARNING, "Atenção", "Por favor, selecione um arquivo.");
            return;
        }

        try {
            // Processar o arquivo e obter os dados
            List<EMGSupermercadoContabil.RegistroEMGSupermercado> registros = EMGSupermercadoContabil.processEMGSupermercado(selectedFile);

            // Limpar a tabela antes de adicionar novos dados
            tableView.getItems().clear();

            // Adicionar os dados processados à tabela
            for (EMGSupermercadoContabil.RegistroEMGSupermercado registro : registros) {
                Transaction transaction = new Transaction(registro.getData(), registro.getDescricao(), registro.getValor());
                tableView.getItems().add(transaction);
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Erro ao processar o arquivo: " + e.getMessage());
        }
    }

    private void exportTableData(Stage primaryStage) {
        if (tableView.getItems().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Atenção", "Não há dados para exportar.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar Arquivo CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(primaryStage);

        if (file != null) {
            // Garante que a extensão do arquivo seja .csv
            String filePath = file.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".csv")) {
                file = new File(filePath + ".csv");
            }

            // Coleta os dados da tabela para exportar
            List<Transaction> transactions = tableView.getItems();

            try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)))) {
                // Escreve o cabeçalho
                writer.println("Data;Descrição;Valor");

                // Escreve os dados
                for (Transaction transaction : transactions) {
                    writer.printf("%s;%s;%s%n",
                            escapeSpecialCharacters(transaction.getDate()),
                            escapeSpecialCharacters(transaction.getDescription()),
                            escapeSpecialCharacters(transaction.getValue()));
                }
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erro", "Erro ao salvar o arquivo: " + e.getMessage());
            }
        }
    }

    private String escapeSpecialCharacters(String value) {
        // Escapa aspas e ponto e vírgula para garantir que o CSV seja corretamente formatado
        if (value.contains("\"") || value.contains(";")) {
            value = "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class Transaction {
        private String date;
        private String description;
        private String value;

        public Transaction(String date, String description, String value) {
            this.date = date;
            this.description = description;
            this.value = value;
        }

        public String getDate() {
            return date;
        }

        public String getDescription() {
            return description;
        }

        public String getValue() {
            return value;
        }
    }
}
