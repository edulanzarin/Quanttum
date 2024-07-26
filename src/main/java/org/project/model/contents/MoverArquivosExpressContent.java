package org.project.model.contents;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.scene.control.Separator;
import org.project.functions.MoverArquivosExpress;

import java.io.File;

public class MoverArquivosExpressContent extends VBox {

    private Label rootPathLabel;
    private Label destPathLabel;
    private File rootDirectory;
    private File destDirectory;

    public MoverArquivosExpressContent() {
        // Configura o layout principal
        this.setSpacing(15);
        this.setPadding(new Insets(20));
        this.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");
        this.setAlignment(Pos.CENTER); // Centraliza verticalmente

        // Título
        Label titleLabel = new Label("Mover Arquivos");
        titleLabel.setFont(new Font("Arial", 20));
        titleLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");

        // Cria a label para mostrar o caminho da pasta raiz
        rootPathLabel = new Label("Caminho da pasta raiz: Nenhuma");
        rootPathLabel.setStyle("-fx-text-fill: #555; -fx-padding: 5;");

        // Cria a label para mostrar o caminho da pasta de destino
        destPathLabel = new Label("Caminho da pasta de destino: Nenhuma");
        destPathLabel.setStyle("-fx-text-fill: #555; -fx-padding: 5;");

        // Cria o botão para escolher a pasta raiz
        Button chooseRootButton = new Button("Escolher Pasta Raiz");
        chooseRootButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #007bff; -fx-padding: 8; -fx-border-color: #007bff; -fx-border-radius: 3; -fx-border-width: 1;");
        chooseRootButton.setMinWidth(150); // Define a largura mínima do botão
        chooseRootButton.setMaxWidth(150); // Define a largura máxima do botão
        chooseRootButton.setOnAction(e -> chooseRootDirectory());

        // Cria o botão para escolher a pasta de destino
        Button chooseDestButton = new Button("Escolher Pasta de Destino");
        chooseDestButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #007bff; -fx-padding: 8; -fx-border-color: #007bff; -fx-border-radius: 3; -fx-border-width: 1;");
        chooseDestButton.setMinWidth(150); // Define a largura mínima do botão
        chooseDestButton.setMaxWidth(150); // Define a largura máxima do botão
        chooseDestButton.setOnAction(e -> chooseDestDirectory());

        // Cria o botão para processar
        Button processButton = new Button("Processar");
        processButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #28a745; -fx-padding: 8; -fx-border-color: #28a745; -fx-border-radius: 3; -fx-border-width: 1;");
        processButton.setMinWidth(150); // Define a largura mínima do botão
        processButton.setMaxWidth(150); // Define a largura máxima do botão
        processButton.setOnAction(e -> processDirectories());

        // Adiciona os controles ao layout
        VBox buttonBox = new VBox(10, chooseRootButton, chooseDestButton, processButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Adiciona os elementos ao VBox principal
        this.getChildren().addAll(titleLabel, new Separator(), rootPathLabel, destPathLabel, buttonBox);
    }

    private void chooseRootDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        rootDirectory = directoryChooser.showDialog(this.getScene().getWindow());
        if (rootDirectory != null) {
            rootPathLabel.setText("Caminho da pasta raiz: " + rootDirectory.getAbsolutePath());
        }
    }

    private void chooseDestDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        destDirectory = directoryChooser.showDialog(this.getScene().getWindow());
        if (destDirectory != null) {
            destPathLabel.setText("Caminho da pasta de destino: " + destDirectory.getAbsolutePath());
        }
    }

    private void processDirectories() {
        if (rootDirectory != null && destDirectory != null) {
            MoverArquivosExpress mover = new MoverArquivosExpress(rootDirectory, destDirectory);
            mover.copiarArquivos(); // Atualize para chamar o método correto
        } else {
            System.out.println("Pastas inválidas.");
        }
    }
}
