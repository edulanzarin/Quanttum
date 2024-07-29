package org.project.view.contents;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class TarefasDiaWindow extends Stage {

    public TarefasDiaWindow(Stage owner, int day) {
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Tarefas do Dia " + day);

        VBox vbox = new VBox();
        vbox.setPadding(new Insets(20));
        vbox.setSpacing(10);

        // Adicione o conteúdo do dia aqui
        Label titleLabel = new Label("Tarefas para o Dia " + day);
        vbox.getChildren().add(titleLabel);

        // Aqui você pode adicionar uma lista de tarefas ou outras informações
        // Exemplo:
        // ListView<String> tasksList = new ListView<>();
        // tasksList.getItems().addAll("Tarefa 1", "Tarefa 2", "Tarefa 3");
        // vbox.getChildren().add(tasksList);

        Scene scene = new Scene(vbox, 300, 200);
        setScene(scene);
    }
}
