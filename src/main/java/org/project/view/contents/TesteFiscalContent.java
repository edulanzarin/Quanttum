package org.project.view.contents;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class TesteFiscalContent extends StackPane {
    public TesteFiscalContent() {
        Label label = new Label("Conteúdo básico para o menu Fiscal - Teste");
        getChildren().add(label);
        setStyle("-fx-padding: 20; -fx-background-color: #e0e0e0;");
    }
}
