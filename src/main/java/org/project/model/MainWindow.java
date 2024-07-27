package org.project.model;

import org.project.model.contents.*;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainWindow extends Application {

    private StackPane contentPanel;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Quanttum");

        BorderPane mainLayout = new BorderPane();
        VBox sidebar = createSidebar();
        mainLayout.setLeft(sidebar);

        contentPanel = new StackPane();
        contentPanel.setStyle("-fx-background-color: #f8f9fa;");
        mainLayout.setCenter(contentPanel);

        mainLayout.setBottom(createFooter());

        Scene scene = new Scene(mainLayout, 800, 600);

        String cssFile = getClass().getResource("/org/project/styles/styles.css") != null ?
                getClass().getResource("/org/project/styles/styles.css").toExternalForm() : null;

        if (cssFile != null) {
            scene.getStylesheets().add(cssFile);
        } else {
            System.err.println("Arquivo CSS não encontrado!");
        }

        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPadding(new Insets(10));
        sidebar.setMinWidth(250);
        sidebar.getStyleClass().add("sidebar");

        TitledPane contabilMenu = createMenu("Contábil", "Bancos", "Empresas", "Conciliações", "Hyperlink");
        TitledPane fiscalMenu = createMenu("Fiscal", "Teste");
        TitledPane expressMenu = createMenu("Express", "Mover Arquivos");

        sidebar.getChildren().addAll(contabilMenu, fiscalMenu, expressMenu);
        return sidebar;
    }

    private TitledPane createMenu(String menuTitle, String... items) {
        VBox content = new VBox();
        content.setSpacing(5);

        for (String item : items) {
            if (isDropdownItem(item)) {
                content.getChildren().add(createDropdownMenu(item));
            } else {
                Button menuItem = new Button(item);
                menuItem.getStyleClass().add("menu-item");
                menuItem.setMaxWidth(Double.MAX_VALUE);
                menuItem.setOnAction(e -> handleMenuItemAction(item));
                content.getChildren().add(menuItem);
            }
        }

        TitledPane titledPane = new TitledPane(menuTitle, content);
        titledPane.setExpanded(false);
        titledPane.getStyleClass().add("titled-pane");
        return titledPane;
    }

    private BorderPane createFooter() {
        BorderPane footer = new BorderPane();
        footer.setPadding(new Insets(10));
        footer.getStyleClass().add("footer");

        Label footerLabel = new Label("© 2024 Quanttum | Versão 1.0");
        footerLabel.getStyleClass().add("footer-label");

        footer.setCenter(footerLabel);
        return footer;
    }

    private boolean isDropdownItem(String item) {
        return item.equals("Bancos") || item.equals("Empresas") || item.equals("Conciliações") || item.equals("Hyperlink");
    }

    private VBox createDropdownMenu(String title) {
        VBox dropdown = new VBox();
        dropdown.getStyleClass().add("dropdown");

        Button menuItem = new Button(title);
        menuItem.getStyleClass().add("menu-item");
        menuItem.setMaxWidth(Double.MAX_VALUE);

        VBox dropdownContent = new VBox();
        dropdownContent.getStyleClass().add("dropdown-content");
        dropdownContent.setVisible(false);
        dropdownContent.setManaged(false);

        String[] subItems = getDropdownItems(title);
        for (String item : subItems) {
            Button subMenuItem = new Button(item);
            subMenuItem.getStyleClass().add("menu-item");
            subMenuItem.setMaxWidth(Double.MAX_VALUE);
            subMenuItem.setOnAction(e -> handleMenuItemAction(item));
            dropdownContent.getChildren().add(subMenuItem);
        }

        menuItem.setOnAction(e -> toggleDropdown(dropdownContent));
        dropdown.getChildren().addAll(menuItem, dropdownContent);

        return dropdown;
    }

    private String[] getDropdownItems(String title) {
        switch (title) {
            case "Bancos": return new String[]{"Com código conta", "Sem código conta"};
            case "Empresas": return new String[]{"Lojão", "Capital Six", "Qualitplacas"};
            case "Conciliações": return new String[]{"Gerar Excel"};
            case "Hyperlink": return new String[]{"DCTF"};
            default: return new String[]{};
        }
    }

    private void toggleDropdown(VBox dropdownContent) {
        boolean isVisible = dropdownContent.isVisible();
        dropdownContent.setVisible(!isVisible);
        dropdownContent.setManaged(!isVisible);
    }

    private void handleMenuItemAction(String item) {
        switch (item) {
            case "Mover Arquivos":
                showContent(new MoverArquivosExpressContent(primaryStage), "Mover Arquivos - Express");
                break;
            case "Com código conta":
                showContent(new BancosComCodContent(), "Bancos - Contábil");
                break;
            case "Sem código conta":
                showContent(new BancosSemCodContent(), "Bancos - Contábil");
                break;
            case "Lojão":
            case "Capital Six":
            case "Qualitplacas":
                showContent(new EmpresasContabilContent(), "Empresas - Contábil");
                break;
            case "Gerar Excel":
                showContent(new ConciliacoesContabilContent(), "Conciliações - Contábil");
                break;
            case "DCTF":
                showContent(new HyperlinkDctfContabilContent(primaryStage), "Hyperlink - Contábil");
                break;
            case "Teste":
                showContent(new TesteFiscalContent(), "Teste - Fiscal");
                break;
            default:
                // Handle other menu items if necessary
                break;
        }
    }

    private void showContent(Object contentInstance, String title) {
        if (contentInstance instanceof MoverArquivosExpressContent) {
            contentPanel.getChildren().setAll((MoverArquivosExpressContent) contentInstance);
        } else if (contentInstance instanceof BancosComCodContent) {
            contentPanel.getChildren().setAll((BancosComCodContent) contentInstance);
        } else if (contentInstance instanceof BancosSemCodContent) {
            contentPanel.getChildren().setAll((BancosSemCodContent) contentInstance);
        } else if (contentInstance instanceof EmpresasContabilContent) {
            contentPanel.getChildren().setAll((EmpresasContabilContent) contentInstance);
        } else if (contentInstance instanceof ConciliacoesContabilContent) {
            contentPanel.getChildren().setAll((ConciliacoesContabilContent) contentInstance);
        } else if (contentInstance instanceof HyperlinkDctfContabilContent) {
            contentPanel.getChildren().setAll((HyperlinkDctfContabilContent) contentInstance);
        } else if (contentInstance instanceof TesteFiscalContent) {
            contentPanel.getChildren().setAll((TesteFiscalContent) contentInstance);
        }
        updateTitle(title);
    }

    private void updateTitle(String title) {
        primaryStage.setTitle(title);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
