package org.project.view.contents;

import org.project.functions.GerenciarNaturezas;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
                // Verifica se a natureza já existe para a empresa
                boolean naturezaExiste = verificarNaturezaExistente(codigo, natureza);

                if (!naturezaExiste) {
                    // Natureza não existe, então faz o cadastro
                    List<GerenciarNaturezas.NaturezaConta> naturezasParaAdicionar = new ArrayList<>();
                    String id = UUID.randomUUID().toString(); // Gera um UUID aleatório
                    GerenciarNaturezas.NaturezaConta novaNatureza = new GerenciarNaturezas.NaturezaConta(
                            id, // ID gerado aleatoriamente
                            natureza,
                            conta,
                            codigo
                    );
                    naturezasParaAdicionar.add(novaNatureza);

                    // Atualiza a planilha com todas as naturezas de uma vez
                    gerenciarNaturezas.cadastrarNaturezas(naturezasParaAdicionar, codigo);

                    // Adiciona na tabela e fecha o diálogo
                    tabela.getItems().add(novaNatureza);
                    cadastroStage.close();
                } else {
                    showAlert(Alert.AlertType.WARNING, "Atenção", "A natureza já existe para a empresa.", cadastroStage);
                }
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

    private static boolean verificarNaturezaExistente(String codigo, String natureza) {
        List<GerenciarNaturezas.NaturezaConta> naturezas = gerenciarNaturezas.verificarCodigoECarregarNaturezas(codigo);
        if (naturezas != null) {
            for (GerenciarNaturezas.NaturezaConta nc : naturezas) {
                if (nc.getNatureza().equals(natureza)) {
                    return true;
                }
            }
        }
        return false;
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
