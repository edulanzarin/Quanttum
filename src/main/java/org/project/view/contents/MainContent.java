package org.project.view.contents;

import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.input.KeyCode;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;

public class MainContent extends StackPane {

    private VBox mainContent;
    private List<Integer> searchResults;
    private int currentIndex;

    public MainContent() {
        this.getStyleClass().add("main-content");

        mainContent = new VBox();
        mainContent.getStyleClass().add("main-content");

        Label welcomeLabel = new Label("Sistema Quanttum");
        welcomeLabel.getStyleClass().add("welcome-title");

        ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/org/project/images/icon.png")));
        logo.setFitWidth(100);
        logo.setFitHeight(100);

        Button readmeButton = new Button("Abrir Manual");
        readmeButton.getStyleClass().add("readme-button");
        readmeButton.setOnAction(e -> showReadmeContent());

        mainContent.getChildren().addAll(logo, welcomeLabel, readmeButton);
        this.getChildren().add(mainContent);
    }

    private void showReadmeContent() {
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("readme.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            if (inputStream == null) {
                throw new FileNotFoundException("Resource not found: readme.txt");
            }

            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            textArea.setText(content.toString());
        } catch (IOException e) {
            textArea.setText("Não foi possível carregar o conteúdo do manual.");
            e.printStackTrace();  // Adiciona o trace para ajudar na depuração
        }

        TextField searchField = new TextField();
        searchField.setPromptText("Pesquisar...");
        searchField.getStyleClass().add("search-field");
        searchField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                searchResults = highlightText(textArea, searchField.getText());
                currentIndex = -1;
            }
        });

        Button upButton = new Button("↑");
        upButton.getStyleClass().add("search-nav-button");
        upButton.setOnAction(e -> navigateSearchResults(textArea, -1));

        Button downButton = new Button("↓");
        downButton.getStyleClass().add("search-nav-button");
        downButton.setOnAction(e -> navigateSearchResults(textArea, 1));

        HBox searchBox = new HBox(5, searchField, upButton, downButton);

        ScrollPane scrollPane = new ScrollPane(textArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPrefSize(580, 790);

        Button closeButton = new Button("Fechar");
        closeButton.getStyleClass().add("close-button");
        closeButton.setOnAction(e -> this.getChildren().removeIf(node -> node instanceof VBox && node != mainContent));

        VBox readmeContent = new VBox(searchBox, scrollPane, closeButton);
        readmeContent.setSpacing(10);
        readmeContent.setPadding(new Insets(10));
        readmeContent.getStyleClass().add("readme-content");
        readmeContent.setMaxWidth(600);
        readmeContent.setMaxHeight(800);
        readmeContent.setAlignment(Pos.CENTER);

        this.getChildren().add(readmeContent);
        StackPane.setAlignment(readmeContent, Pos.CENTER);
    }

    private List<Integer> highlightText(TextArea textArea, String searchText) {
        String content = textArea.getText();
        textArea.setStyle("-fx-highlight-fill: yellow; -fx-highlight-text-fill: black;");
        List<Integer> results = new ArrayList<>();

        Pattern pattern = Pattern.compile(searchText, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            results.add(matcher.start());
        }

        if (!results.isEmpty()) {
            textArea.selectRange(results.get(0), results.get(0) + searchText.length());
        }

        return results;
    }

    private void navigateSearchResults(TextArea textArea, int direction) {
        if (searchResults == null || searchResults.isEmpty()) {
            return;
        }

        currentIndex += direction;

        if (currentIndex < 0) {
            currentIndex = searchResults.size() - 1;
        } else if (currentIndex >= searchResults.size()) {
            currentIndex = 0;
        }

        int start = searchResults.get(currentIndex);
        int end = start + textArea.getSelectedText().length();

        textArea.selectRange(start, end);
    }
}
