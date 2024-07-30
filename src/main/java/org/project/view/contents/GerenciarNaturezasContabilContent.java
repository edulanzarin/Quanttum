package org.project.view.contents;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.project.functions.GerenciarNaturezas;

import java.io.IOException;
import java.util.List;

public class GerenciarNaturezasContabilContent extends VBox {

    private TableView<GerenciarNaturezas.NaturezaConta> tabela;
    private TextField txtValor;
    private Stage primaryStage;
    private GerenciarNaturezas gerenciarNaturezas;

    public GerenciarNaturezasContabilContent(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.gerenciarNaturezas = new GerenciarNaturezas();

        configureLayout();
        configureTable();
        configureButtons();
    }

    private void configureLayout() {
        setPadding(new Insets(20));
        setSpacing(15);
        getStyleClass().add("cadastrar-natureza-contabil-content");

        VBox parentBox = new VBox();
        parentBox.setAlignment(Pos.CENTER);
        VBox.setVgrow(parentBox, javafx.scene.layout.Priority.ALWAYS);

        txtValor = new TextField();
        txtValor.setPromptText("Código da Empresa");
        txtValor.getStyleClass().add("txtvalor-field");
        txtValor.setPrefWidth(300);

        HBox txtValorBox = new HBox();
        txtValorBox.setAlignment(Pos.CENTER);
        txtValorBox.setSpacing(10);
        txtValorBox.getChildren().addAll(txtValor, createButton("Carregar", e -> carregarDados()));

        tabela = new TableView<>();
        tabela.setPrefWidth(400);
        tabela.getStyleClass().add("table-view");

        parentBox.getChildren().addAll(txtValorBox, tabela, createButton("Cadastrar Natureza", e -> mostrarJanelaCadastro()));
        getChildren().add(parentBox);
    }

    private void configureTable() {
        TableColumn<GerenciarNaturezas.NaturezaConta, String> colId = createColumn("ID", "id", 0);
        TableColumn<GerenciarNaturezas.NaturezaConta, String> colunaNatureza = createColumn("Natureza", "natureza", 200);
        TableColumn<GerenciarNaturezas.NaturezaConta, String> colunaConta = createColumn("Conta", "conta", 178);

        tabela.getColumns().addAll(colId, colunaNatureza, colunaConta);
        tabela.setRowFactory(tv -> createTableRow());
    }

    private TableColumn<GerenciarNaturezas.NaturezaConta, String> createColumn(String title, String property, double width) {
        TableColumn<GerenciarNaturezas.NaturezaConta, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setPrefWidth(width);
        return column;
    }

    private TableRow<GerenciarNaturezas.NaturezaConta> createTableRow() {
        TableRow<GerenciarNaturezas.NaturezaConta> row = new TableRow<>();
        ContextMenu contextMenu = new ContextMenu();

        MenuItem editItem = new MenuItem("Editar");
        editItem.setOnAction(e -> editarNatureza(row.getItem()));

        MenuItem deleteItem = new MenuItem("Excluir");
        deleteItem.setOnAction(e -> excluirNatureza(row.getItem()));

        contextMenu.getItems().addAll(editItem, deleteItem);

        row.contextMenuProperty().bind(
                javafx.beans.binding.Bindings.when(row.emptyProperty())
                        .then((ContextMenu) null)
                        .otherwise(contextMenu)
        );

        return row;
    }

    private void configureButtons() {
        // Configura o botão de cadastro de natureza
        Button btnCadastrar = createButton("Cadastrar Natureza", e -> mostrarJanelaCadastro());

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setSpacing(10);
        buttonBox.getChildren().add(btnCadastrar);

        VBox.setMargin(buttonBox, new Insets(20, 0, 0, 0));
    }

    private Button createButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> eventHandler) {
        Button button = new Button(text);
        button.getStyleClass().add("botao");
        button.setOnAction(eventHandler);
        return button;
    }

    private void carregarDados() {
        String codigo = txtValor.getText().trim();

        if (codigo.isEmpty()) {
            showAlert(AlertType.WARNING, "Atenção", "O código da empresa não pode estar vazio.", primaryStage);
            return;
        }

        List<GerenciarNaturezas.NaturezaConta> naturezas = gerenciarNaturezas.verificarCodigoECarregarNaturezas(codigo);

        if (naturezas != null) {
            tabela.getItems().clear();
            tabela.getItems().addAll(naturezas);
        } else {
            showAlert(AlertType.INFORMATION, "Informação", "Código da empresa não encontrado.", primaryStage);
        }
    }

    private void mostrarJanelaCadastro() {
        Stage janelaCadastro = new Stage();
        janelaCadastro.setTitle("Cadastro de Natureza");

        TextField txtNatureza = new TextField();
        TextField txtConta = new TextField();

        Button btnCadastrar = createButton("Cadastrar", e -> {
            String natureza = txtNatureza.getText().trim();
            String conta = txtConta.getText().trim();

            if (!natureza.isEmpty() && !conta.isEmpty()) {
                tabela.getItems().add(new GerenciarNaturezas.NaturezaConta(natureza, natureza, conta));
                janelaCadastro.close();
            } else {
                showAlert(AlertType.WARNING, "Atenção", "Natureza e Conta não podem estar vazios.", janelaCadastro);
            }
        });

        VBox layout = new VBox(10, new Label("Natureza:"), txtNatureza, new Label("Conta:"), txtConta, btnCadastrar);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        janelaCadastro.initModality(Modality.APPLICATION_MODAL);
        janelaCadastro.initOwner(primaryStage);
        janelaCadastro.setScene(new Scene(layout, 300, 200));
        janelaCadastro.show();
    }

    private void editarNatureza(GerenciarNaturezas.NaturezaConta naturezaConta) {
        Stage janelaEdicao = new Stage();
        janelaEdicao.setTitle("Editar Natureza");

        TextField txtConta = new TextField(naturezaConta.getConta());

        Button btnAtualizar = createButton("Atualizar", e -> {
            String novaConta = txtConta.getText().trim();
            if (!novaConta.isEmpty()) {
                try {
                    atualizarNaturezaNaPlanilha(naturezaConta.getId(), novaConta);
                    GerenciarNaturezas.NaturezaConta updatedNatureza = new GerenciarNaturezas.NaturezaConta(naturezaConta.getId(), naturezaConta.getNatureza(), novaConta);
                    tabela.getItems().set(tabela.getSelectionModel().getSelectedIndex(), updatedNatureza);
                    janelaEdicao.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    showAlert(AlertType.ERROR, "Erro", "Erro ao atualizar natureza.", janelaEdicao);
                }
            } else {
                showAlert(AlertType.WARNING, "Atenção", "Conta não pode estar vazia.", janelaEdicao);
            }
        });

        VBox layout = new VBox(10, new Label("Conta:"), txtConta, btnAtualizar);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        janelaEdicao.initModality(Modality.APPLICATION_MODAL);
        janelaEdicao.initOwner(primaryStage);
        janelaEdicao.setScene(new Scene(layout, 300, 150));
        janelaEdicao.show();
    }

    private void excluirNatureza(GerenciarNaturezas.NaturezaConta naturezaConta) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText("Você tem certeza que deseja excluir a natureza?");
        alert.setContentText("Esta ação não pode ser desfeita.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                gerenciarNaturezas.marcarNaturezaComoInativa(naturezaConta.getId());
                tabela.getItems().remove(naturezaConta);
            } catch (IOException ex) {
                ex.printStackTrace();
                showAlert(AlertType.ERROR, "Erro", "Erro ao excluir natureza.", primaryStage);
            }
        }
    }

    private void atualizarNaturezaNaPlanilha(String id, String novaConta) throws IOException {
        gerenciarNaturezas.atualizarNatureza(id, novaConta);
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
