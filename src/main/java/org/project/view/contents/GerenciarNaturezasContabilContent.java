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

        TableColumn<GerenciarNaturezas.NaturezaConta, String> colunaNatureza = new TableColumn<>("Natureza");
        TableColumn<GerenciarNaturezas.NaturezaConta, String> colunaConta = new TableColumn<>("Conta");

        colunaNatureza.setCellValueFactory(new PropertyValueFactory<>("natureza"));
        colunaNatureza.setPrefWidth(200);

        colunaConta.setCellValueFactory(new PropertyValueFactory<>("conta"));
        colunaConta.setPrefWidth(178);

        tabela.getColumns().addAll(colunaNatureza, colunaConta);
        tabela.getStyleClass().add("tabela");

        // Botão de cadastrar
        Button btnCadastrar = new Button("Cadastrar Natureza");
        btnCadastrar.getStyleClass().add("botao");
        btnCadastrar.setOnAction(e -> mostrarJanelaCadastro());

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

    private void mostrarJanelaCadastro() {
        Stage janelaCadastro = new Stage();
        janelaCadastro.setTitle("Cadastro de Natureza");

        Label lblNatureza = new Label("Natureza:");
        lblNatureza.getStyleClass().add("label");
        TextField txtNatureza = new TextField();
        txtNatureza.getStyleClass().add("text-field");

        Label lblConta = new Label("Conta:");
        lblConta.getStyleClass().add("label");
        TextField txtConta = new TextField();
        txtConta.getStyleClass().add("text-field");

        Button btnCadastrar = new Button("Cadastrar");
        btnCadastrar.getStyleClass().add("botao");
        btnCadastrar.setOnAction(e -> {
            String natureza = txtNatureza.getText();
            String conta = txtConta.getText();

            if (!natureza.isEmpty() && !conta.isEmpty()) {
                tabela.getItems().add(new GerenciarNaturezas.NaturezaConta(natureza, conta));
                janelaCadastro.close();
            } else {
                showAlert(AlertType.WARNING, "Atenção", "Natureza e Conta não podem estar vazios.", janelaCadastro);
            }
        });

        VBox layout = new VBox(10, lblNatureza, txtNatureza, lblConta, txtConta, btnCadastrar);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        Scene cena = new Scene(layout, 300, 200);
        janelaCadastro.initModality(Modality.APPLICATION_MODAL);
        janelaCadastro.initOwner(primaryStage);
        janelaCadastro.setScene(cena);
        janelaCadastro.show();
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