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

public class CadastrarNaturezaContabilContent extends VBox {

    private TableView<NaturezaConta> tabela;
    private TextField txtValor;
    private Stage primaryStage;

    public CadastrarNaturezaContabilContent(Stage primaryStage) {
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
        txtValor.setPromptText("Número da Empresa");
        txtValor.getStyleClass().add("txtvalor-field");
        txtValor.setPrefWidth(300); // Define a largura do campo de texto
        txtValor.setMaxWidth(300);

        // Criação do campo de texto para valor
        HBox txtValorBox = new HBox();
        txtValorBox.setAlignment(Pos.CENTER);
        txtValorBox.setSpacing(10); // Espaçamento entre os botões
        txtValorBox.getChildren().add(txtValor);

        // Criação da tabela
        tabela = new TableView<>();
        tabela.setPrefWidth(792); // Define a largura preferencial da tabela
        tabela.setMaxWidth(792);  // Define a largura máxima da tabela

        TableColumn<NaturezaConta, String> colunaNatureza = new TableColumn<>("Natureza");
        TableColumn<NaturezaConta, String> colunaConta = new TableColumn<>("Conta");

        colunaNatureza.setCellValueFactory(new PropertyValueFactory<>("natureza"));
        colunaNatureza.setPrefWidth(400);

        colunaConta.setCellValueFactory(new PropertyValueFactory<>("conta"));
        colunaConta.setPrefWidth(380);

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
        VBox.setMargin(txtValorBox, new Insets(0, 0, 20, 0));
        parentBox.getChildren().addAll(txtValorBox, tabela, buttonBox);
        getChildren().add(parentBox);
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
                tabela.getItems().add(new NaturezaConta(natureza, conta));
                janelaCadastro.close();
            } else {
                showAlert(AlertType.WARNING, "Atenção", "Preencha todos os campos.", janelaCadastro);
            }
        });

        VBox vboxCadastro = new VBox(10, lblNatureza, txtNatureza, lblConta, txtConta, btnCadastrar);
        vboxCadastro.setPadding(new Insets(10));
        vboxCadastro.getStyleClass().add("vbox-cadastro");

        Scene sceneCadastro = new Scene(vboxCadastro, 300, 200);
        janelaCadastro.initModality(Modality.APPLICATION_MODAL);
        janelaCadastro.initOwner(primaryStage);
        janelaCadastro.setScene(sceneCadastro);
        janelaCadastro.show();
    }

    private void showAlert(AlertType alertType, String title, String message, Stage owner) {
        Alert alert = new Alert(alertType);
        alert.initOwner(owner);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class NaturezaConta {
        private final String natureza;
        private final String conta;

        public NaturezaConta(String natureza, String conta) {
            this.natureza = natureza;
            this.conta = conta;
        }

        public String getNatureza() {
            return natureza;
        }

        public String getConta() {
            return conta;
        }
    }
}