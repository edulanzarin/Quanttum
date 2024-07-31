package org.project.view.contents;

import org.project.functions.GerenciarNaturezas;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CadastrarNaturezaContent {

    private static GerenciarNaturezas gerenciarNaturezas = new GerenciarNaturezas(); // Inicialize o GerenciarNaturezas

    public static void showCadastroDialog(Stage owner, TableView<GerenciarNaturezas.NaturezaConta> tabela, String codigo) {
        Stage cadastroStage = new Stage();
        cadastroStage.setTitle("Cadastro de Natureza");

        // Label para mostrar o código da empresa
        Label lblCodigo = new Label("Código da Empresa: " + codigo);
        lblCodigo.getStyleClass().add("label");

        // Labels e campos de texto
        Label lblNatureza = new Label("Natureza:");
        lblNatureza.getStyleClass().add("label");
        TextField txtNatureza = new TextField();
        txtNatureza.getStyleClass().add("text-field");

        Label lblConta = new Label("Conta:");
        lblConta.getStyleClass().add("label");
        TextField txtConta = new TextField();
        txtConta.getStyleClass().add("text-field");

        // Botão de cadastrar
        Button btnCadastrar = new Button("Cadastrar");
        btnCadastrar.getStyleClass().add("botao-edicao"); // Alterado para botao-edicao
        btnCadastrar.setOnAction(e -> {
            String natureza = txtNatureza.getText();
            String conta = txtConta.getText();

            if (!natureza.isEmpty() && !conta.isEmpty()) {
                GerenciarNaturezas.NaturezaConta novaNatureza = new GerenciarNaturezas.NaturezaConta(
                        0, // ID será definido após o cadastro
                        natureza,
                        conta,
                        codigo
                );

                // Atualiza a planilha
                gerenciarNaturezas.cadastrarNatureza(codigo, natureza, conta);

                // Adiciona na tabela e fecha o diálogo
                tabela.getItems().add(novaNatureza);
                cadastroStage.close();
            } else {
                showAlert(Alert.AlertType.WARNING, "Atenção", "Natureza e Conta não podem estar vazios.", cadastroStage);
            }
        });

        // Layout da janela de cadastro
        VBox layout = new VBox(10, lblCodigo, lblNatureza, txtNatureza, lblConta, txtConta, btnCadastrar);
        layout.getStyleClass().add("vbox-edicao"); // Adiciona a classe de estilo para VBox
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 300, 250); // Ajuste o tamanho se necessário
        scene.getStylesheets().add(CadastrarNaturezaContent.class.getResource("/org/project/styles/content-styles.css").toExternalForm()); // Carrega o CSS

        cadastroStage.initModality(Modality.APPLICATION_MODAL);
        cadastroStage.initOwner(owner);
        cadastroStage.setScene(scene);
        cadastroStage.show();
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
