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
import org.project.functions.ProcessarReinfExpress;
import com.itextpdf.text.DocumentException;

import java.io.File;
import java.io.IOException;

public class ArquivosReinfExpressContent extends VBox {

    private TextField planilhaPathField;
    private File planilhaFile;
    private String userId;

    public ArquivosReinfExpressContent(Stage primaryStage, String userId) {
        this.userId = userId;

        // Configura o layout principal
        setPadding(new Insets(20));
        setSpacing(15);
        getStyleClass().add("arquivos-reinf-express-content");

        // VBox pai para centralização vertical
        VBox parentBox = new VBox();
        parentBox.setAlignment(Pos.CENTER);
        VBox.setVgrow(parentBox, Priority.ALWAYS);

        // Título
        Label titleLabel = new Label("Gerar Arquivos Reinf");
        titleLabel.setFont(new Font("Arial", 20));
        titleLabel.getStyleClass().add("title");
        VBox.setMargin(titleLabel, new Insets(0, 0, 50, 0)); // Adiciona margem inferior

        // Campo de texto para caminho do arquivo da planilha
        planilhaPathField = new TextField();
        planilhaPathField.setPromptText("Selecione a planilha");
        planilhaPathField.getStyleClass().add("text-field");

        Button choosePlanilhaButton = new Button("...");
        choosePlanilhaButton.getStyleClass().add("select-button");
        choosePlanilhaButton.setOnAction(e -> choosePlanilha(primaryStage));

        Button processButton = new Button("Processar");
        processButton.getStyleClass().add("process-button");
        processButton.setOnAction(e -> processPlanilhaFile(primaryStage));
        VBox.setMargin(processButton, new Insets(15, 0, 0, 0)); // Adiciona margem superior

        // Criação de HBox para alinhamento do campo de texto e botão
        HBox planilhaBox = new HBox(10, planilhaPathField, choosePlanilhaButton);
        planilhaBox.setAlignment(Pos.CENTER);
        planilhaBox.setMaxWidth(600); // Define uma largura máxima para o alinhamento
        VBox.setMargin(planilhaBox, new Insets(0, 0, 15, 0)); // Adiciona margem inferior

        // Adiciona os controles ao layout do VBox pai
        parentBox.getChildren().addAll(titleLabel, planilhaBox, processButton);
        getChildren().add(parentBox);
    }

    private void choosePlanilha(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecione a planilha");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls"));
        planilhaFile = fileChooser.showOpenDialog(primaryStage);
        if (planilhaFile != null) {
            planilhaPathField.setText(planilhaFile.getAbsolutePath());
        }
    }

    private void processPlanilhaFile(Stage primaryStage) {
        if (planilhaFile != null) {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Escolha o diretório de saída");
            File outputDirectory = directoryChooser.showDialog(primaryStage);

            if (outputDirectory != null) {
                try {
                    ProcessarReinfExpress.processarPlanilha(planilhaFile.getAbsolutePath(), outputDirectory.getAbsolutePath());
                    showAlert(Alert.AlertType.INFORMATION, "Sucesso", "A planilha foi processada com sucesso.", primaryStage);
                } catch (IOException | DocumentException e) {
                    showAlert(Alert.AlertType.ERROR, "Erro", "Ocorreu um erro ao processar a planilha.", primaryStage);
                    e.printStackTrace();
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "Atenção", "Por favor, selecione um diretório de saída.", primaryStage);
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Atenção", "Por favor, selecione uma planilha.", primaryStage);
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
