package org.project.view.contents;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.project.functions.ConferenciaFiscalAnalitico;

import java.io.File;
import java.util.List;

public class ConferenciaFiscalAnaliticoContent extends VBox {

    private TextField filePathField;
    private File selectedDirectory;
    private TableView<ConferenciaFiscalAnalitico.NaturezaConta> tabela;

    public ConferenciaFiscalAnaliticoContent(Stage primaryStage) {
        setPadding(new Insets(20));
        setSpacing(15);
        getStyleClass().add("conferencia-fiscal-contabil-content");

        VBox parentBox = new VBox();
        parentBox.setAlignment(Pos.CENTER);
        VBox.setVgrow(parentBox, Priority.ALWAYS);

        Label titleLabel = new Label("Conferência Fiscal Contábil");
        titleLabel.setFont(new Font("Arial", 20));
        titleLabel.getStyleClass().add("title");
        VBox.setMargin(titleLabel, new Insets(0, 0, 50, 0));

        filePathField = new TextField();
        filePathField.setPromptText("Selecione uma pasta");
        filePathField.getStyleClass().add("text-field");
        filePathField.setPrefWidth(300);

        Button chooseDirectoryButton = new Button("...");
        chooseDirectoryButton.getStyleClass().add("select-button");
        chooseDirectoryButton.setOnAction(e -> chooseDirectory(primaryStage));

        Button processButton = new Button("Processar");
        processButton.getStyleClass().add("process-button");
        processButton.setOnAction(e -> processDirectory());
        VBox.setMargin(processButton, new Insets(15, 0, 50, 0));

        HBox directoryBox = new HBox(10, filePathField, chooseDirectoryButton);
        directoryBox.setAlignment(Pos.CENTER);
        directoryBox.setMaxWidth(500);

        tabela = new TableView<>();
        tabela.setPrefWidth(750);
        tabela.setMaxWidth(750);
        tabela.getStyleClass().add("table-view");

        TableColumn<ConferenciaFiscalAnalitico.NaturezaConta, String> colunaNatureza = new TableColumn<>("Natureza");
        colunaNatureza.setCellValueFactory(new PropertyValueFactory<>("natureza"));
        colunaNatureza.setPrefWidth(250);

        TableColumn<ConferenciaFiscalAnalitico.NaturezaConta, String> colunaNota = new TableColumn<>("Nota");
        colunaNota.setCellValueFactory(new PropertyValueFactory<>("nota"));
        colunaNota.setPrefWidth(250);

        TableColumn<ConferenciaFiscalAnalitico.NaturezaConta, String> colunaValor = new TableColumn<>("Valor");
        colunaValor.setCellValueFactory(new PropertyValueFactory<>("valor"));
        colunaValor.setPrefWidth(250);

        tabela.getColumns().addAll(colunaNatureza, colunaNota, colunaValor);

        parentBox.getChildren().addAll(titleLabel, directoryBox, processButton, tabela);
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

    private void processDirectory() {
        if (selectedDirectory == null) {
            showAlert(Alert.AlertType.WARNING, "Aviso", "Nenhuma pasta selecionada", "Por favor, selecione uma pasta para processar.");
            return;
        }

        List<ConferenciaFiscalAnalitico.NaturezaConta> naturezas = ConferenciaFiscalAnalitico.processarPasta(selectedDirectory);
        ObservableList<ConferenciaFiscalAnalitico.NaturezaConta> data = FXCollections.observableArrayList(naturezas);

        tabela.setItems(data);

        tabela.setRowFactory(tv -> new TableRow<ConferenciaFiscalAnalitico.NaturezaConta>() {
            @Override
            protected void updateItem(ConferenciaFiscalAnalitico.NaturezaConta item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    String nota = item.getNota();
                }
            }
        });
    }

    private void showAlert(Alert.AlertType alertType, String title, String headerText, String contentText) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }
}
