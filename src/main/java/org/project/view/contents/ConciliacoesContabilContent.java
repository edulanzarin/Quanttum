package org.project.view.contents;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.text.Font;

public class ConciliacoesContabilContent extends VBox {

    public ConciliacoesContabilContent() {
        // Configura o layout principal
        this.setSpacing(15);
        this.setPadding(new Insets(20));
        this.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");
        this.setAlignment(Pos.CENTER); // Centraliza verticalmente

        // Cria a label para exibir a frase
        Label phraseLabel = new Label("Bem-vindo ao módulo Conciliações.");
        phraseLabel.setFont(new Font("Arial", 20));
        phraseLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");

        // Adiciona o label ao VBox
        this.getChildren().add(phraseLabel);
    }
}
