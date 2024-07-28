package org.project.view;

import org.project.view.contents.*;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class MainWindow extends Application {

    private StackPane contentPanel; // Painel que exibe o conteúdo principal
    private Stage primaryStage; // Janela principal da aplicação

    private String userId;

    public MainWindow(String userId) {
        this.userId = userId;
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Quanttum");

        BorderPane mainLayout = new BorderPane(); // Layout principal da janela
        VBox sidebar = createSidebar(); // Cria a barra lateral
        mainLayout.setLeft(sidebar);

        contentPanel = new StackPane(); // Painel central para exibir o conteúdo
        contentPanel.setStyle("-fx-background-color: #f8f9fa;");
        mainLayout.setCenter(contentPanel);

        // Define o conteúdo inicial
        showContent(new MainContent(), "Main Content");

        Scene scene = new Scene(mainLayout, 800, 600); // Cria a cena com o layout principal

        String mainCssFile = getClass().getResource("/org/project/styles/main-styles.css") != null ?
                getClass().getResource("/org/project/styles/main-styles.css").toExternalForm() : null;

        if (mainCssFile != null) {
            scene.getStylesheets().add(mainCssFile);
        }

        primaryStage.setScene(scene); // Define a cena para o estágio principal
        primaryStage.setMaximized(true); // Maximiza a janela
        primaryStage.show(); // Exibe a janela
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPadding(new Insets(10));
        sidebar.getStyleClass().add("sidebar");

        BorderPane footer = createFooter(); // Cria o rodapé
        sidebar.getChildren().add(footer);

        HBox header = createSidebarHeader(); // Cria o cabeçalho da barra lateral
        sidebar.getChildren().add(header);

        // Cria os menus da barra lateral
        TitledPane contabilMenu = createMenu("Contábil", "Bancos", "Empresas", "Conciliações", "Hyperlink");
        TitledPane fiscalMenu = createMenu("Fiscal", "Processar XML");
        TitledPane expressMenu = createMenu("Express", "Fiscal", "RH");

        sidebar.getChildren().addAll(contabilMenu, fiscalMenu, expressMenu);

        // Espaço vazio para empurrar os menus e botões para cima
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        // Adiciona o botão Alterar Senha centralizado
        Button changePasswordButton = new Button("Alterar Senha");
        changePasswordButton.getStyleClass().add("change-button");
        changePasswordButton.setOnAction(e -> openChangePasswordWindow());

        HBox buttonContainer = new HBox(changePasswordButton);
        buttonContainer.setAlignment(Pos.CENTER);

        sidebar.getChildren().add(buttonContainer);
        return sidebar;
    }

    private HBox createSidebarHeader() {
        HBox header = new HBox();
        header.setSpacing(10);
        header.setPadding(new Insets(10));
        header.getStyleClass().add("sidebar-header");

        Image icon = new Image(getClass().getResourceAsStream("/org/project/images/icon.png"));
        ImageView iconView = new ImageView(icon);
        iconView.setFitWidth(64);
        iconView.setFitHeight(64);
        iconView.setOnMouseClicked(e -> showContent(new MainContent(), "Main Content"));

        header.getChildren().addAll(iconView);
        return header;
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
        return item.equals("Bancos") || item.equals("Empresas") || item.equals("Conciliações") || item.equals("Hyperlink") || item.equals("Fiscal") || item.equals("RH") || item.equals("Processar XML");
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
            case "Bancos": return new String[]{"Processar Extrato"};
            case "Empresas": return new String[]{"Lojão", "Capital Six", "Qualitplacas"};
            case "Conciliações": return new String[]{"Conciliar Pagos"};
            case "Hyperlink": return new String[]{"DCTF"};
            case "Fiscal": return new String[]{"Renomear DAS"};
            case "RH": return new String[]{"Mover Arquivos"};
            case "Processar XML": return new String[]{"Gerar Planilha"};
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
                showContent(new MoverArquivosExpressContent(primaryStage, userId), "Mover Arquivos - Express");
                break;
            case "Processar Extrato":
                showContent(new ProcessarExtratoContabilContent(primaryStage, userId), "Bancos - Contábil");
                break;
            case "Lojão":
            case "Capital Six":
                showContent(new EmpresasContabilContent(), "Empresas - Contábil");
                break;
            case "Conciliar Pagos":
                showContent(new ConciliarPagosContabilContent(primaryStage), "Conciliações - Contábil");
                break;
            case "DCTF":
                showContent(new HyperlinkDctfContabilContent(primaryStage, userId), "Hyperlink - Contábil");
                break;
            case "Gerar Planilha":
                showContent(new ProcessarXmlFiscalContent(primaryStage, userId), "Gerar Planilha - Fiscal");
                break;
            case "Renomear DAS":
                showContent(new RenomearGuiasExpressContent(primaryStage, userId), "Nome DAS - Express");
            case "Qualitplacas":
                showContent(new QualitplacasContabilContent(primaryStage), "Qualitplacas - Contábil");
            default:
                // Gerenciar outros itens do menu, se necessário
                break;
        }
    }

    private void showContent(Object contentInstance, String title) {
        if (contentInstance instanceof MainContent) {
            contentPanel.getChildren().setAll((MainContent) contentInstance);
        } else if (contentInstance instanceof MoverArquivosExpressContent) {
            contentPanel.getChildren().setAll((MoverArquivosExpressContent) contentInstance);
        } else if (contentInstance instanceof ProcessarExtratoContabilContent) {
            contentPanel.getChildren().setAll((ProcessarExtratoContabilContent) contentInstance);
        } else if (contentInstance instanceof EmpresasContabilContent) {
            contentPanel.getChildren().setAll((EmpresasContabilContent) contentInstance);
        } else if (contentInstance instanceof ConciliarPagosContabilContent) {
            contentPanel.getChildren().setAll((ConciliarPagosContabilContent) contentInstance);
        } else if (contentInstance instanceof HyperlinkDctfContabilContent) {
            contentPanel.getChildren().setAll((HyperlinkDctfContabilContent) contentInstance);
        } else if (contentInstance instanceof ProcessarXmlFiscalContent) {
            contentPanel.getChildren().setAll((ProcessarXmlFiscalContent) contentInstance);
        } else if (contentInstance instanceof RenomearGuiasExpressContent) {
            contentPanel.getChildren().setAll((RenomearGuiasExpressContent) contentInstance);
        } else if (contentInstance instanceof QualitplacasContabilContent) {
                contentPanel.getChildren().setAll((QualitplacasContabilContent) contentInstance);
        }
        applyContentStyles();
        primaryStage.setTitle("Quanttum - " + title);
    }

    private void openChangePasswordWindow() {
        ChangePasswordWindow changePasswordWindow = new ChangePasswordWindow(userId);
        try {
            changePasswordWindow.start(new Stage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyContentStyles() {
        contentPanel.getChildren().forEach(node -> {
            if (node instanceof Region) {
                String contentCssFile = getClass().getResource("/org/project/styles/content-styles.css") != null ?
                        getClass().getResource("/org/project/styles/content-styles.css").toExternalForm() : null;
                if (contentCssFile != null) {
                    ((Region) node).getStylesheets().add(contentCssFile);
                }
            }
        });
    }
}