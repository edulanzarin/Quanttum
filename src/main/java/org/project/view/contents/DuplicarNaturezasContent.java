package org.project.view.contents;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.project.functions.GerenciarNaturezas;

import java.io.IOException;

public class DuplicarNaturezasContent {

    private static GerenciarNaturezas gerenciarNaturezas = new GerenciarNaturezas(); // Cria a instância

    public static void showDuplicarDialog(Stage ownerStage, String codigoOriginal) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(ownerStage);
        dialog.setTitle("Duplicar Naturezas");

        // Layout
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(20));
        vbox.setSpacing(15);
        vbox.setAlignment(Pos.CENTER);
        vbox.getStyleClass().add("vbox-duplicar");

        // Label para mostrar o código original
        Label lblCodigoOriginal = new Label("Código da Empresa Original: " + codigoOriginal);
        lblCodigoOriginal.getStyleClass().add("label");

        // Campo para código da nova empresa
        TextField txtNovoCodigo = new TextField();
        txtNovoCodigo.setPromptText("Novo Código da Empresa");
        txtNovoCodigo.getStyleClass().add("txtvalor-field");
        txtNovoCodigo.setPrefWidth(300);

        // Botão Duplicar
        Button btnDuplicar = new Button("Duplicar");
        btnDuplicar.getStyleClass().add("botao");
        btnDuplicar.setOnAction(e -> {
            String novoCodigo = txtNovoCodigo.getText();
            if (novoCodigo != null && !novoCodigo.trim().isEmpty()) {
                try {
                    // Verifica se a empresa alvo existe
                    if (gerenciarNaturezas.empresaExiste(novoCodigo)) { // Usa a instância para chamar o método
                        // Chama o método para duplicar as naturezas
                        gerenciarNaturezas.duplicarNaturezas(codigoOriginal, novoCodigo); // Usa a instância para chamar o método
                        dialog.close();
                    } else {
                        showAlert(Alert.AlertType.WARNING, "Atenção", "Empresa não cadastrada.", dialog);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Erro", "Erro ao verificar a existência da empresa.", dialog);
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "Atenção", "O novo código da empresa deve ser fornecido.", dialog);
            }
        });

        vbox.getChildren().addAll(lblCodigoOriginal, txtNovoCodigo, btnDuplicar);

        Scene scene = new Scene(vbox, 400, 200);
        scene.getStylesheets().add("/org/project/styles/content-styles.css");
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private static void showAlert(Alert.AlertType type, String title, String message, Stage owner) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(owner);
        alert.showAndWait();
    }
}

