package org.project.view.contents;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.project.functions.GerenciarNaturezas;

import java.util.List;

public class GerenciarNaturezasContabilContent extends VBox {

    private TableView<GerenciarNaturezas.NaturezaConta> tabela;
    private TextField txtValor;
    private Stage primaryStage;
    private GerenciarNaturezas cadastrarNatureza;

    public GerenciarNaturezasContabilContent(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Configura o layout principal
        setPadding(new Insets(20));
        setSpacing(15);
        getStyleClass().add("cadastrar-natureza-contabil-content");

        // VBox pai para centralização vertical
        VBox parentBox = new VBox();
        parentBox.setAlignment(Pos.CENTER);
        VBox.setVgrow(parentBox, javafx.scene.layout.Priority.ALWAYS);

        txtValor = new TextField();
        txtValor.setPromptText("Código da Empresa");
        txtValor.getStyleClass().add("txtvalor-field");
        txtValor.setPrefWidth(300); // Define a largura do campo de texto
        txtValor.setMaxWidth(300);

        // Botão Carregar
        Button btnCarregar = new Button("Carregar");
        btnCarregar.getStyleClass().add("botao");
        btnCarregar.setOnAction(e -> carregarDados());

        // Criação do campo de texto e botão Carregar
        HBox txtValorBox = new HBox();
        txtValorBox.setAlignment(Pos.CENTER);
        txtValorBox.setSpacing(10); // Espaçamento entre o campo de texto e o botão
        txtValorBox.getChildren().addAll(txtValor, btnCarregar);

        // Criação da tabela
        tabela = new TableView<>();
        tabela.setPrefWidth(400); // Define a largura preferencial da tabela
        tabela.setMaxWidth(400);  // Define a largura máxima da tabela
        tabela.getStyleClass().add("table-view"); // Aplicar a classe correta

        // Coluna ID (oculta)
        TableColumn<GerenciarNaturezas.NaturezaConta, Integer> colunaId = new TableColumn<>("ID");
        colunaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colunaId.setVisible(false); // Oculta a coluna ID

        // Coluna Natureza
        TableColumn<GerenciarNaturezas.NaturezaConta, String> colunaNatureza = new TableColumn<>("Natureza");
        colunaNatureza.setCellValueFactory(new PropertyValueFactory<>("natureza"));
        colunaNatureza.setPrefWidth(200);

        // Coluna Conta
        TableColumn<GerenciarNaturezas.NaturezaConta, String> colunaConta = new TableColumn<>("Conta");
        colunaConta.setCellValueFactory(new PropertyValueFactory<>("conta"));
        colunaConta.setPrefWidth(178);

        tabela.getColumns().addAll(colunaId, colunaNatureza, colunaConta);
        tabela.getStyleClass().add("tabela");

        // Adiciona evento de clique nas linhas da tabela
        tabela.setRowFactory(tv -> {
            TableRow<GerenciarNaturezas.NaturezaConta> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.SECONDARY) {
                    GerenciarNaturezas.NaturezaConta clickedRow = row.getItem();
                    mostrarContextMenu(event.getScreenX(), event.getScreenY(), clickedRow);
                }
            });
            return row;
        });

        // Botão de cadastrar
        Button btnCadastrar = new Button("Cadastrar Natureza");
        btnCadastrar.getStyleClass().add("botao");
        btnCadastrar.setOnAction(e -> {
            String codigo = txtValor.getText();
            if (codigo != null && !codigo.trim().isEmpty()) {
                CadastrarNaturezaContent.showCadastroDialog(primaryStage, tabela, codigo);
            } else {
                showAlert(AlertType.WARNING, "Atenção", "O código da empresa deve ser fornecido.", primaryStage);
            }
        });

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setSpacing(10);
        buttonBox.getChildren().add(btnCadastrar);

        VBox.setMargin(buttonBox, new Insets(20, 0, 0, 0)); // Margem superior do botão
        VBox.setMargin(txtValorBox, new Insets(0, 0, 15, 0));
        parentBox.getChildren().addAll(txtValorBox, tabela, buttonBox);
        getChildren().add(parentBox);

        // Inicialização do CadastrarNatureza
        cadastrarNatureza = new GerenciarNaturezas();
    }

    private void carregarDados() {
        String codigo = txtValor.getText();

        if (codigo == null || codigo.trim().isEmpty()) {
            showAlert(AlertType.WARNING, "Atenção", "O código da empresa não pode estar vazio.", primaryStage);
            return;
        }

        List<GerenciarNaturezas.NaturezaConta> naturezas = cadastrarNatureza.verificarCodigoECarregarNaturezas(codigo);

        if (naturezas != null) {
            tabela.getItems().clear();
            tabela.getItems().addAll(naturezas);
        } else {
            showAlert(AlertType.INFORMATION, "Informação", "Código da empresa não encontrado.", primaryStage);
        }
    }

    private void mostrarContextMenu(double x, double y, GerenciarNaturezas.NaturezaConta item) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem editarItem = new MenuItem("Editar Conta");
        editarItem.setOnAction(e -> {
            System.out.println("Editar - ID: " + item.getId()); // Exibe o ID no console
            mostrarJanelaEdicao(item);
        });

        MenuItem excluirItem = new MenuItem("Excluir Natureza");
        excluirItem.setOnAction(e -> {
            System.out.println("Excluir - ID: " + item.getId()); // Exibe o ID no console
            tabela.getItems().remove(item);
        });

        contextMenu.getItems().addAll(editarItem, excluirItem);
        contextMenu.show(primaryStage, x, y);
    }

    private void mostrarJanelaEdicao(GerenciarNaturezas.NaturezaConta item) {
        EditarContaContent.showEditDialog(primaryStage, item, () -> {
            tabela.refresh();
        });
    }

    private void showAlert(AlertType type, String title, String message, Stage owner) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(owner);
        alert.showAndWait();
    }
}
