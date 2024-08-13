package org.project.view.bancos;

import org.project.functions.bancos.*;
import org.project.functions.ExportarPlanilha;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Alert;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class ProcessarExtratoContabilContent extends VBox {

    private TextField filePathField;
    private TextField codeField;
    private ComboBox<String> codeTypeComboBox;
    private File selectedFile;
    private TableView<Transaction> tableView;
    private CheckBox useCommaCheckBox;
    private final String userId;

    public ProcessarExtratoContabilContent(Stage primaryStage, String userId) {
        this.userId = userId;

        // Configura o layout principal
        setPadding(new Insets(20));
        setSpacing(15);
        getStyleClass().add("bancos-com-cod-content");

        // VBox pai para centralização vertical
        VBox parentBox = new VBox();
        parentBox.setAlignment(Pos.CENTER);
        VBox.setVgrow(parentBox, Priority.ALWAYS); // Expande verticalmente

        // Título
        Label titleLabel = new Label("Processar Extrato Bancário");
        titleLabel.setFont(new Font("Arial", 20));
        titleLabel.getStyleClass().add("title");
        VBox.setMargin(titleLabel, new Insets(0, 0, 50, 0)); // Adiciona margem inferior

        // Campo para o código
        codeField = new TextField();
        codeField.setPromptText("Código");
        codeField.getStyleClass().add("cod-field");

        // Adiciona o ComboBox ao lado do campo de código
        codeTypeComboBox = new ComboBox<>();
        codeTypeComboBox.getItems().addAll("VIACREDI", "SICREDI", "ITAÚ MODELO ANTIGO", "SICOOB");
        codeTypeComboBox.setValue("Selecionar banco");
        codeTypeComboBox.getStyleClass().add("codigo-combobox");
        codeTypeComboBox.setPrefWidth(150);

        // Campo de texto para o caminho do arquivo
        filePathField = new TextField();
        filePathField.setPromptText("Selecione um arquivo");
        filePathField.getStyleClass().add("text-field");
        filePathField.setPrefWidth(300); // Define uma largura menor para o campo de caminho do arquivo

        Button chooseFileButton = new Button("...");
        chooseFileButton.getStyleClass().add("select-button");
        chooseFileButton.setOnAction(e -> chooseFile(primaryStage));

        Button processButton = new Button("Processar");
        processButton.getStyleClass().add("process-button");
        processButton.setOnAction(e -> processFile(primaryStage));
        VBox.setMargin(processButton, new Insets(15, 0, 50, 0));

        Button exportButton = new Button("Exportar");
        exportButton.getStyleClass().add("export-button");
        exportButton.setOnAction(e -> exportTableData(primaryStage));
        VBox.setMargin(exportButton, new Insets(15, 0, 50, 0));

        // Adiciona o CheckBox para selecionar o uso de vírgulas
        useCommaCheckBox = new CheckBox("Remover vírgulas");
        useCommaCheckBox.getStyleClass().add("comma-checkbox");

        // Criação de HBox para alinhamento do campo de texto e botão do caminho do arquivo
        HBox fileBox = new HBox(10, filePathField, chooseFileButton);
        fileBox.setAlignment(Pos.CENTER);
        fileBox.setMaxWidth(500); // Define uma largura máxima para o alinhamento
        VBox.setMargin(fileBox, new Insets(0, 0, 15, 0));

        // Criação de HBox para o campo de código e o ComboBox
        HBox codeBox = new HBox(10, new Label("Código da conta"), codeField, codeTypeComboBox);
        codeBox.setAlignment(Pos.CENTER);
        codeBox.getStyleClass().add("code-box");
        VBox.setMargin(codeBox, new Insets(0, 0, 15, 0));

        // Adiciona HBox com CheckBox
        HBox checkBoxBox = new HBox(10, useCommaCheckBox);
        checkBoxBox.setAlignment(Pos.CENTER);
        VBox.setMargin(checkBoxBox, new Insets(0, 0, 15, 0));

        // Configuração da tabela
        tableView = new TableView<>();
        tableView.setPrefWidth(792);
        tableView.setMaxWidth(792);

        TableColumn<Transaction, String> dateColumn = new TableColumn<>("Data");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateColumn.setPrefWidth(100);

        TableColumn<Transaction, String> descriptionColumn = new TableColumn<>("Descrição");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionColumn.setPrefWidth(350);

        TableColumn<Transaction, String> valueColumn = new TableColumn<>("Valor");
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        valueColumn.setPrefWidth(140);

        TableColumn<Transaction, String> debitColumn = new TableColumn<>("Débito");
        debitColumn.setCellValueFactory(new PropertyValueFactory<>("debit"));
        debitColumn.setPrefWidth(90);

        TableColumn<Transaction, String> creditColumn = new TableColumn<>("Crédito");
        creditColumn.setCellValueFactory(new PropertyValueFactory<>("credit"));
        creditColumn.setPrefWidth(90);

        tableView.getColumns().addAll(dateColumn, descriptionColumn, valueColumn, debitColumn, creditColumn);

        // Adiciona os controles ao layout do VBox pai
        HBox buttonBox = new HBox(10, processButton, exportButton);
        buttonBox.setAlignment(Pos.CENTER);
        VBox.setMargin(buttonBox, new Insets(15, 0, 50, 0));

        parentBox.getChildren().addAll(titleLabel, codeBox, fileBox, checkBoxBox, buttonBox, tableView);
        getChildren().add(parentBox);
    }

    private void chooseFile(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Escolha um arquivo PDF");
        selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null) {
            filePathField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void processFile(Stage primaryStage) {
        if (selectedFile == null || codeField.getText().isEmpty() || codeTypeComboBox.getValue().equals("Selecionar banco")) {
            showAlert(Alert.AlertType.WARNING, "Atenção", "Por favor, selecione um arquivo PDF e preencha todos os campos.");
            return;
        }

        String bancoSelecionado = codeTypeComboBox.getValue();
        String codigo = codeField.getText();
        boolean useComma = useCommaCheckBox.isSelected();

        try {
            List<String[]> dataRows;
            switch (bancoSelecionado) {
                case "VIACREDI":
                    dataRows = ProcessarViacredi.processarPDF(selectedFile, codigo, useComma);
                    break;
                case "SICREDI":
                    dataRows = ProcessarSicredi.processarPDF(selectedFile, codigo, useComma);
                    break;
                case "ITAÚ MODELO ANTIGO":
                    dataRows = ItauModeloAntigo.processarPDF(selectedFile, codigo, useComma);
                    break;
                case "SICOOB":
                    dataRows = ProcessarSicoob.processarPDF(selectedFile, codigo, useComma);
                    break;
                default:
                    showAlert(Alert.AlertType.INFORMATION, "Atenção", "Processamento para o banco " + bancoSelecionado + " não está disponível ainda.");
                    return;
            }

            tableView.getItems().clear();
            for (String[] row : dataRows) {
                tableView.getItems().add(new Transaction(row[0], row[1], row[2], row[3], row[4]));
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Erro ao processar o arquivo PDF: " + e.getMessage());
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
            String filePath = file.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".csv")) {
                file = new File(filePath + ".csv");
            }

            try {
                ExportarPlanilha.exportExtrato(file, tableView.getItems(), userId);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Arquivo CSV exportado com sucesso.");
            } catch (IOException | GeneralSecurityException e) {
                showAlert(Alert.AlertType.ERROR, "Erro", "Erro ao exportar o arquivo CSV: " + e.getMessage());
            }
        }
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
        private String debit;
        private String credit;

        public Transaction(String date, String description, String value, String debit, String credit) {
            this.date = date;
            this.description = description;
            this.value = value;
            this.debit = debit;
            this.credit = credit;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getDebit() {
            return debit;
        }

        public void setDebit(String debit) {
            this.debit = debit;
        }

        public String getCredit() {
            return credit;
        }

        public void setCredit(String credit) {
            this.credit = credit;
        }
    }
}