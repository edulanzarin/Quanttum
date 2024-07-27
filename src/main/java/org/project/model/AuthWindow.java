package org.project.model;

import org.project.functions.Authentication;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Platform;

/**
 * A classe AuthWindow representa a janela de autenticação do usuário para a aplicação.
 *
 * Esta janela permite ao usuário inserir seu nome de usuário e clicar no botão de verificação para acessar o restante da aplicação.
 * O código usa JavaFX para criar a interface gráfica.
 */
public class AuthWindow extends Application {

    private Stage primaryStage; // Referência para a janela principal da aplicação
    private Label messageLabel; // Label para exibir mensagens de erro

    /**
     * Método principal que inicializa a aplicação e exibe a janela de autenticação.
     *
     * @param primaryStage O estágio principal da aplicação.
     */
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Verificação de Usuário"); // Define o título da janela

        // Cria um layout vertical com espaçamento de 15 pixels entre os elementos
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(30)); // Define o padding do layout
        layout.setStyle("-fx-background-color: #f1f1f1;"); // Define a cor de fundo do layout

        // Cria um rótulo para o campo de texto
        Label userLabel = new Label("Digite o usuário:");
        userLabel.getStyleClass().add("user-label"); // Adiciona uma classe de estilo para o rótulo

        // Cria um campo de texto para a entrada do nome de usuário
        TextField userField = new TextField();
        userField.getStyleClass().add("user-field"); // Adiciona uma classe de estilo para o campo de texto

        // Cria um botão para a verificação do usuário
        Button verifyButton = new Button("Entrar");
        verifyButton.getStyleClass().add("verify-button"); // Adiciona uma classe de estilo para o botão

        // Cria um label para mensagens de erro
        messageLabel = new Label();
        messageLabel.getStyleClass().add("message-label"); // Adiciona uma classe de estilo para o label de mensagem
        messageLabel.setStyle("-fx-text-fill: red;"); // Define a cor do texto como vermelho

        verifyButton.setOnAction(e -> {
            // Define a ação a ser executada quando o botão é clicado
            if (isUserValid(userField.getText())) {
                primaryStage.close(); // Fecha a janela de autenticação
                showMainWindow(); // Abre a janela principal da aplicação
            } else {
                // Se o usuário não for válido, exibe uma mensagem de erro
                messageLabel.setText("Usuário inválido ou inativo.");
            }
        });

        // Adiciona os elementos ao layout
        layout.getChildren().addAll(userLabel, userField, verifyButton, messageLabel);

        // Cria uma cena com o layout e define seu tamanho
        Scene scene = new Scene(layout, 350, 220);
        // Adiciona o arquivo de estilo CSS à cena
        scene.getStylesheets().add(getClass().getResource("/org/project/styles/auth-styles.css").toExternalForm());

        // Define a cena da janela principal e exibe a janela
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Verifica se o usuário é válido usando a classe Authentication.
     *
     * @param user O nome do usuário a ser verificado.
     * @return true se o usuário for válido, caso contrário, false.
     */
    private boolean isUserValid(String user) {
        Authentication auth = new Authentication(); // Cria uma instância da classe Authentication
        return auth.isUserActive(user); // Verifica se o usuário está ativo
    }

    /**
     * Abre a janela principal da aplicação.
     */
    private void showMainWindow() {
        MainWindow mainWindow = new MainWindow(); // Cria uma nova instância da MainWindow
        try {
            mainWindow.start(new Stage()); // Abre a MainWindow em um novo estágio
        } catch (Exception e) {
            e.printStackTrace(); // Imprime a pilha de erros em caso de exceção
        }
    }

    /**
     * Método principal que inicia a aplicação.
     *
     * @param args Argumentos da linha de comando.
     */
    public static void main(String[] args) {
        launch(args); // Lança a aplicação JavaFX
    }
}
