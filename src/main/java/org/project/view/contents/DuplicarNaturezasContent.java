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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DuplicarNaturezasContent {

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
        vbox.getStyleClass().add("vbox-duplicar"); // Adiciona o estilo para o VBox

        // Label para mostrar o código original
        Label lblCodigoOriginal = new Label("Código da Empresa Original: " + codigoOriginal);
        lblCodigoOriginal.getStyleClass().add("label");

        // Campo para código da nova empresa
        TextField txtNovoCodigo = new TextField();
        txtNovoCodigo.setPromptText("Novo Código da Empresa");
        txtNovoCodigo.getStyleClass().add("txtvalor-field"); // Adiciona o estilo para o campo de texto
        txtNovoCodigo.setPrefWidth(300);

        // Botão Duplicar
        Button btnDuplicar = new Button("Duplicar");
        btnDuplicar.getStyleClass().add("botao"); // Adiciona o estilo para o botão
        btnDuplicar.setOnAction(e -> {
            String novoCodigo = txtNovoCodigo.getText();
            if (novoCodigo != null && !novoCodigo.trim().isEmpty()) {
                GerenciarNaturezas gerenciarNaturezas = new GerenciarNaturezas();
                try {
                    // Verifica se a empresa alvo existe
                    if (gerenciarNaturezas.empresaExiste(novoCodigo)) {
                        // Chama o método para duplicar as naturezas
                        duplicarNaturezas(gerenciarNaturezas, codigoOriginal, novoCodigo);
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
        scene.getStylesheets().add("/org/project/styles/content-styles.css"); // Certifique-se de definir o caminho correto para seu arquivo CSS
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private static void duplicarNaturezas(GerenciarNaturezas gerenciarNaturezas, String codigoOriginal, String novoCodigo) {
        // Recupera todas as naturezas da empresa original
        List<GerenciarNaturezas.NaturezaConta> naturezasOriginal = gerenciarNaturezas.verificarCodigoECarregarNaturezas(codigoOriginal);

        if (naturezasOriginal != null) {
            // Recupera todas as naturezas da nova empresa para comparação
            List<GerenciarNaturezas.NaturezaConta> naturezasNovaEmpresa = gerenciarNaturezas.verificarCodigoECarregarNaturezas(novoCodigo);

            // Cria um conjunto para verificar rapidamente a existência
            Set<String> naturezasExistentes = new HashSet<>();
            if (naturezasNovaEmpresa != null) {
                for (GerenciarNaturezas.NaturezaConta natureza : naturezasNovaEmpresa) {
                    naturezasExistentes.add(natureza.getNatureza() + "|" + natureza.getConta());
                }
            }

            // Para cada natureza da empresa original
            for (GerenciarNaturezas.NaturezaConta naturezaOriginal : naturezasOriginal) {
                String chaveNatureza = naturezaOriginal.getNatureza() + "|" + naturezaOriginal.getConta();

                // Verifica se a combinação já existe na nova empresa
                if (!naturezasExistentes.contains(chaveNatureza)) {
                    // Adiciona a natureza na nova empresa
                    gerenciarNaturezas.cadastrarNatureza(novoCodigo, naturezaOriginal.getNatureza(), naturezaOriginal.getConta());
                }
            }
        }
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
