package org.project.view.contents;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.project.functions.MeuCronograma;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;

public class CadastrarTarefaContent {

    public CadastrarTarefaContent(Stage primaryStage, String userId, Runnable onTarefaAdicionada) {
        // Criação do stage para a janela de cadastro de tarefas
        Stage cadastroStage = new Stage();
        cadastroStage.setTitle("Cadastrar Tarefa");

        // Layout do formulário
        VBox formBox = new VBox();
        formBox.getStyleClass().add("cadastrar-tarefa-form"); // Aplica o estilo
        formBox.setPadding(new Insets(20));
        formBox.setSpacing(10);
        formBox.setAlignment(Pos.CENTER);

        // Campo para título da tarefa
        Label titleLabel = new Label("Título:");
        titleLabel.getStyleClass().add("cadastrar-tarefa-label"); // Aplica o estilo
        TextField titleField = new TextField();
        titleField.getStyleClass().add("cadastrar-tarefa-text-field"); // Aplica o estilo
        titleField.setPromptText("Digite o título da tarefa");

        // Campo para descrição da tarefa
        Label descriptionLabel = new Label("Descrição:");
        descriptionLabel.getStyleClass().add("cadastrar-tarefa-label"); // Aplica o estilo
        TextArea descriptionArea = new TextArea();
        descriptionArea.getStyleClass().add("cadastrar-tarefa-text-area"); // Aplica o estilo
        descriptionArea.setPromptText("Digite a descrição da tarefa");

        // Campo para data da tarefa
        Label dateLabel = new Label("Data:");
        dateLabel.getStyleClass().add("cadastrar-tarefa-label"); // Aplica o estilo
        DatePicker datePicker = new DatePicker();
        datePicker.getStyleClass().add("cadastrar-tarefa-date-picker"); // Aplica o estilo

        // Botão de confirmar
        Button confirmButton = new Button("Confirmar");
        confirmButton.getStyleClass().add("cadastrar-tarefa-button"); // Aplica o estilo
        confirmButton.setOnAction(e -> {
            String titulo = titleField.getText();
            String descricao = descriptionArea.getText();
            LocalDate data = datePicker.getValue(); // Obtém a data selecionada

            try {
                // Adiciona a tarefa à planilha
                MeuCronograma.addTarefa(titulo, descricao, data, userId);
                onTarefaAdicionada.run(); // Notifica que a tarefa foi adicionada
            } catch (IOException | GeneralSecurityException ex) {
                ex.printStackTrace();
                // Tratar exceções adequadamente, por exemplo, mostrar uma mensagem de erro ao usuário
            }
            cadastroStage.close(); // Fecha a janela após confirmar
        });

        formBox.getChildren().addAll(titleLabel, titleField, descriptionLabel, descriptionArea, dateLabel, datePicker, confirmButton);

        Scene scene = new Scene(formBox, 300, 300); // Ajusta o tamanho da janela para incluir o novo campo
        scene.getStylesheets().add(getClass().getResource("/org/project/styles/content-styles.css").toExternalForm()); // Aplica o CSS
        cadastroStage.setScene(scene);
        cadastroStage.show();
    }
}
