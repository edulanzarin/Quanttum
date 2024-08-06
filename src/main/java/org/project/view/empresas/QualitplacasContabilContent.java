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
import org.project.functions.empresas.QualitplacasContabil;
import org.project.functions.empresas.QualitplacasContabil.RegistroQualitplacas;

import java.io.*;
import java.util.List;

public class QualitplacasContabilContent extends VBox {

    private TextField filePathField;
    private File selectedFile;
    private TableView<Transaction> tableView;
    private CheckBox removeCommasCheckBox;

    public QualitplacasContabilContent(Stage primaryStage) {
        // Configura o layout principal
        setPadding(new Insets(20));
        setSpacing(15);
        getStyleClass().add("qualitplacas-content");

        // VBox pai para centralização vertical
        VBox parentBox = new VBox();
        parentBox.setAlignment(Pos.CENTER);
        VBox.setVgrow(parentBox, Priority.ALWAYS); // Expande verticalmente

        // Título
        Label titleLabel = new Label("Qualitplacas Contábil");
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

        // Adiciona o CheckBox para remover vírgulas
        removeCommasCheckBox = new CheckBox("Remover vírgulas");
        removeCommasCheckBox.getStyleClass().add("comma-checkbox");

        // Criação de HBox para alinhamento do campo de texto e botão do caminho do arquivo
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
        dateColumn.setPrefWidth(100);

        TableColumn<Transaction, String> supplierColumn = new TableColumn<>("Fornecedor");
        supplierColumn.setCellValueFactory(new PropertyValueFactory<>("supplier"));
        supplierColumn.setPrefWidth(310);

        TableColumn<Transaction, String> noteColumn = new TableColumn<>("Nota");
        noteColumn.setCellValueFactory(new PropertyValueFactory<>("note"));
        noteColumn.setPrefWidth(140);

        TableColumn<Transaction, String> valueColumn = new TableColumn<>("Valor");
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        valueColumn.setPrefWidth(110);

        TableColumn<Transaction, String> discountColumn = new TableColumn<>("Desconto");
        discountColumn.setCellValueFactory(new PropertyValueFactory<>("discount"));
        discountColumn.setPrefWidth(110);

        tableView.getColumns().addAll(dateColumn, supplierColumn, noteColumn, valueColumn, discountColumn);

        // Adiciona os controles ao layout do VBox pai
        HBox buttonBox = new HBox(10, processButton, exportButton);
        buttonBox.setAlignment(Pos.CENTER);
        VBox.setMargin(buttonBox, new Insets(15, 0, 50, 0));

        HBox checkBoxBox = new HBox(10, removeCommasCheckBox);
        checkBoxBox.setAlignment(Pos.CENTER);
        VBox.setMargin(checkBoxBox, new Insets(0, 0, 15, 0));

        parentBox.getChildren().addAll(titleLabel, fileBox, checkBoxBox, buttonBox, tableView);
        getChildren().add(parentBox);
    }

    private void chooseFile(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Escolha um arquivo");
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

        boolean removeCommas = removeCommasCheckBox.isSelected();

        try {
            // Processar o arquivo e obter os dados
            List<RegistroQualitplacas> registros = QualitplacasContabil.processQualitplacas(selectedFile, removeCommas);

            // Limpar a tabela antes de adicionar novos dados
            tableView.getItems().clear();

            // Adicionar os dados processados à tabela
            for (RegistroQualitplacas registro : registros) {
                Transaction transaction = new Transaction(registro.getData(), registro.getFornecedor(), registro.getNota(), registro.getValor(), registro.getDesconto());
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
                writer.println("Data;Fornecedor;Nota;Valor;Desconto");

                // Escreve os dados
                for (Transaction transaction : transactions) {
                    writer.printf("%s;%s;%s;%s;%s%n",
                            escapeSpecialCharacters(transaction.getDate()),
                            escapeSpecialCharacters(transaction.getSupplier()),
                            escapeSpecialCharacters(transaction.getNote()),
                            escapeSpecialCharacters(transaction.getValue()),
                            escapeSpecialCharacters(transaction.getDiscount()));
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
        private String supplier;
        private String note;
        private String value;
        private String discount;

        public Transaction(String date, String supplier, String note, String value, String discount) {
            this.date = date;
            this.supplier = supplier;
            this.note = note;
            this.value = value;
            this.discount = discount;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getSupplier() {
            return supplier;
        }

        public String getNote() {
            return note;
        }

        public String getValue() {
            return value;
        }

        public String getDiscount() {
            return discount;
        }
    }
}
