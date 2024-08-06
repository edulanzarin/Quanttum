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
import org.project.functions.empresas.SupermercadoJK;

import java.io.*;
import java.util.List;

public class SupermercadoJKContabilContent extends VBox {

    private TextField filePathField;
    private File selectedFile;
    private TableView<Transaction> tableView;
    private CheckBox checkBoxRelatorio;
    private CheckBox checkBoxLoterica;

    public SupermercadoJKContabilContent(Stage primaryStage) {
        // Configura o layout principal
        setPadding(new Insets(20));
        setSpacing(15);
        getStyleClass().add("supermercadojk-content");

        // VBox pai para centralização vertical
        VBox parentBox = new VBox();
        parentBox.setAlignment(Pos.CENTER);
        VBox.setVgrow(parentBox, Priority.ALWAYS); // Expande verticalmente

        // Título
        Label titleLabel = new Label("Supermercado JK Contábil");
        titleLabel.setFont(new Font("Arial", 20));
        titleLabel.getStyleClass().add("title");
        VBox.setMargin(titleLabel, new Insets(0, 0, 20, 0)); // Adiciona margem inferior

        // Campo de texto para o caminho do arquivo
        filePathField = new TextField();
        filePathField.setPromptText("Selecione um arquivo");
        filePathField.getStyleClass().add("text-field");
        filePathField.setPrefWidth(300);

        Button chooseFileButton = new Button("...");
        chooseFileButton.getStyleClass().add("select-button");
        chooseFileButton.setOnAction(e -> chooseFile(primaryStage));

        Button processButton = new Button("Processar");
        processButton.getStyleClass().add("process-button");
        processButton.setOnAction(e -> processFile(primaryStage));

        Button exportButton = new Button("Exportar");
        exportButton.getStyleClass().add("export-button");
        exportButton.setOnAction(e -> exportTableData(primaryStage));

        checkBoxRelatorio = new CheckBox("Processar Relatório");
        checkBoxRelatorio.getStyleClass().add("custom-checkbox");
        checkBoxLoterica = new CheckBox("Processar Lotérica");
        checkBoxLoterica.getStyleClass().add("custom-checkbox");

        checkBoxRelatorio.setOnAction(e -> {
            if (checkBoxRelatorio.isSelected()) {
                checkBoxLoterica.setSelected(false);
            }
        });

        checkBoxLoterica.setOnAction(e -> {
            if (checkBoxLoterica.isSelected()) {
                checkBoxRelatorio.setSelected(false);
            }
        });

        HBox checkBoxBox = new HBox(10, checkBoxRelatorio, checkBoxLoterica);
        checkBoxBox.setAlignment(Pos.CENTER);
        VBox.setMargin(checkBoxBox, new Insets(0, 0, 15, 0));

        HBox fileBox = new HBox(10, filePathField, chooseFileButton);
        fileBox.setAlignment(Pos.CENTER);
        fileBox.setMaxWidth(500);
        VBox.setMargin(fileBox, new Insets(0, 0, 15, 0));

        // Configuração da tabela
        tableView = new TableView<>();
        tableView.setPrefWidth(792);
        tableView.setMaxWidth(792);

        TableColumn<Transaction, String> dateColumn = new TableColumn<>("Data");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateColumn.setPrefWidth(165);

        TableColumn<Transaction, String> descriptionColumn = new TableColumn<>("Descrição");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionColumn.setPrefWidth(400);

        TableColumn<Transaction, String> valueColumn = new TableColumn<>("Valor");
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        valueColumn.setPrefWidth(210);

        tableView.getColumns().addAll(dateColumn, descriptionColumn, valueColumn);

        // Adiciona os controles ao layout do VBox pai
        HBox buttonBox = new HBox(10, processButton, exportButton);
        buttonBox.setAlignment(Pos.CENTER);
        VBox.setMargin(buttonBox, new Insets(15, 0, 50, 0));

        parentBox.getChildren().addAll(titleLabel, checkBoxBox, fileBox, buttonBox, tableView);
        getChildren().add(parentBox);
    }

    private void chooseFile(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Escolha um arquivo");

        if (checkBoxRelatorio.isSelected()) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Relatório Bancário", "*.pdf"));
        } else if (checkBoxLoterica.isSelected()) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Relatório Lotérica", "*.pdf"));
        }

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
            List<SupermercadoJK.RegistroSupermercadoJK> registros;
            if (checkBoxRelatorio.isSelected()) {
                registros = SupermercadoJK.processSupermercadoJK(selectedFile);
            } else if (checkBoxLoterica.isSelected()) {
                registros = SupermercadoJK.processCaixaSupermercadoJK(selectedFile);
            } else {
                showAlert(Alert.AlertType.WARNING, "Atenção", "Por favor, selecione um tipo de processamento.");
                return;
            }

            // Limpar a tabela antes de adicionar novos dados
            tableView.getItems().clear();

            // Adicionar os dados processados à tabela
            for (SupermercadoJK.RegistroSupermercadoJK registro : registros) {
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
