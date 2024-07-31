package org.project.view.contents;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.project.functions.GerenciarNaturezas;

public class EditarContaContent extends VBox {

    private TextField txtConta;
    private Button btnSalvar;
    private Stage dialogStage;
    private GerenciarNaturezas.NaturezaConta naturezaConta;
    private Runnable onSave;

    public EditarContaContent(Stage dialogStage, GerenciarNaturezas.NaturezaConta naturezaConta, Runnable onSave) {
        this.dialogStage = dialogStage;
        this.naturezaConta = naturezaConta;
        this.onSave = onSave;

        setPadding(new Insets(20));
        setSpacing(10);
        setAlignment(Pos.CENTER);
        getStyleClass().add("vbox-edicao");

        Label lblConta = new Label("Conta:");
        lblConta.getStyleClass().add("label");

        txtConta = new TextField(naturezaConta.getConta());
        txtConta.getStyleClass().add("text-field");

        btnSalvar = new Button("Salvar");
        btnSalvar.getStyleClass().add("botao-edicao");
        btnSalvar.setOnAction(e -> salvar());

        getChildren().addAll(lblConta, txtConta, btnSalvar);
    }

    private void salvar() {
        if (!txtConta.getText().isEmpty()) {
            naturezaConta.setConta(txtConta.getText());
            GerenciarNaturezas gerenciarNaturezas = new GerenciarNaturezas();

            // Ajuste para o intervalo correto: a coluna C deve ser usada
            int rowIndex = naturezaConta.getId() + 1; // Ajuste para a linha correta, considerando que o índice começa em 0
            String range = "naturezas!C" + rowIndex; // Coluna C é para Conta

            try {
                gerenciarNaturezas.atualizarConta(GerenciarNaturezas.SHEET_ID, range, txtConta.getText());
                if (onSave != null) {
                    onSave.run();
                }
                dialogStage.close();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível atualizar a conta.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Atenção", "Conta não pode estar vazia.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(dialogStage);
        alert.showAndWait();
    }

    public static void showEditDialog(Stage owner, GerenciarNaturezas.NaturezaConta naturezaConta, Runnable onSave) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Editar Conta");

        EditarContaContent content = new EditarContaContent(dialogStage, naturezaConta, onSave);
        Scene scene = new Scene(content, 300, 150);
        scene.getStylesheets().add(content.getClass().getResource("/org/project/styles/content-styles.css").toExternalForm());

        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(owner);
        dialogStage.setScene(scene);
        dialogStage.show();
    }
}
