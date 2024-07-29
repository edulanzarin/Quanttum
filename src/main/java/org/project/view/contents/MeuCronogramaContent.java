package org.project.view.contents;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MeuCronogramaContent extends VBox {

    private Stage primaryStage;

    public MeuCronogramaContent(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Configura o layout principal
        setPadding(new Insets(20));
        setSpacing(15);
        getStyleClass().add("meu-cronograma-content"); // Adiciona a classe de estilo

        // VBox pai para centralização vertical
        VBox parentBox = new VBox();
        parentBox.setAlignment(Pos.CENTER);
        VBox.setVgrow(parentBox, Priority.ALWAYS); // Expande verticalmente

        // Criação do GridPane para o calendário
        GridPane calendarGrid = new GridPane();
        calendarGrid.setHgap(5); // Espaço horizontal reduzido
        calendarGrid.setVgap(5); // Espaço vertical reduzido
        calendarGrid.getStyleClass().add("calendar-grid"); // Adiciona a classe de estilo
        calendarGrid.setAlignment(Pos.CENTER); // Alinha o GridPane ao centro

        // Adiciona os títulos dos dias da semana
        String[] diasSemana = {"Seg", "Ter", "Qua", "Qui", "Sex", "Sab", "Dom"};
        for (int i = 0; i < diasSemana.length; i++) {
            Label dayLabel = new Label(diasSemana[i]);
            dayLabel.getStyleClass().add("day-label"); // Adiciona a classe de estilo
            dayLabel.setAlignment(Pos.CENTER); // Alinha o texto ao centro
            GridPane.setHalignment(dayLabel, javafx.geometry.HPos.CENTER); // Alinha a label horizontalmente ao centro
            calendarGrid.add(dayLabel, i, 0);
        }

        // Adiciona os botões dos dias do mês (1 a 31)
        for (int day = 1; day <= 31; day++) {
            final int dayFinal = day; // Variável final para a expressão lambda
            Button dayButton = new Button(String.valueOf(day));
            dayButton.getStyleClass().add("day-button"); // Adiciona a classe de estilo
            dayButton.setOnAction(e -> openTarefasDiaWindow(dayFinal)); // Usa a variável final
            dayButton.setPrefSize(30, 30); // Tamanho reduzido dos botões
            dayButton.setAlignment(Pos.CENTER); // Alinha o texto ao centro
            GridPane.setHalignment(dayButton, javafx.geometry.HPos.CENTER); // Alinha o botão horizontalmente ao centro
            calendarGrid.add(dayButton, (day + 5) % 7, (day + 5) / 7 + 1);
        }

        // Adiciona o GridPane ao VBox pai
        parentBox.getChildren().add(calendarGrid);
        getChildren().add(parentBox); // Adiciona o VBox pai ao layout principal
    }

    private void openTarefasDiaWindow(int day) {
        TarefasDiaWindow tarefasWindow = new TarefasDiaWindow(primaryStage, day);
        tarefasWindow.showAndWait();
    }
}
