package org.project.view.contents;

import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import org.project.functions.MeuCronograma;
import org.project.model.HeaderFooterPageEvent;

import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;




import javax.imageio.ImageIO;
import java.io.*;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class MeuCronogramaContent extends HBox {

    private Stage primaryStage;
    private String userId;
    private LocalDate currentDate;
    private VBox calendarBox;
    private GridPane calendarGrid;
    private VBox tarefaContentBox;
    private VBox tarefasBox;
    private ScrollPane tarefasScrollPane;
    private VBox buttonAndFilterBox; // Mantenha uma referência direta
    private HBox buttonBox;
    private ScrollPane tarefaContentScrollPane;
    private ComboBox<String> statusFilterComboBox;
    private ComboBox<String> tituloFilterComboBox;
    private DatePicker dataFilterDatePickerInicial;
    private DatePicker dataFilterDatePickerFinal;

    public MeuCronogramaContent(Stage primaryStage, String userId) {
        this.primaryStage = primaryStage;
        this.currentDate = LocalDate.now();
        this.userId = userId;

        // Configura o layout principal
        setPadding(new Insets(20));
        setSpacing(80);
        getStyleClass().add("meu-cronograma-content");
        setAlignment(Pos.CENTER);

        // VBox para a lista de tarefas
        tarefasBox = new VBox();
        tarefasBox.setPrefWidth(347);
        tarefasBox.setSpacing(10); // Espaço entre os elementos
        tarefasBox.getStyleClass().add("container");

        Label tarefasTitle = new Label("Lista de Tarefas");
        tarefasTitle.getStyleClass().add("tarefas-title");

        buttonAndFilterBox = new VBox();
        buttonAndFilterBox.setSpacing(10);
        buttonAndFilterBox.setMinHeight(70); // Aumentado para garantir espaço
        buttonAndFilterBox.setMinWidth(300);
        buttonAndFilterBox.setAlignment(Pos.CENTER); // Alinha os filhos ao centro

        buttonBox = new HBox();
        buttonBox.setSpacing(10);
        buttonBox.setPadding(new Insets(10));
        buttonBox.setAlignment(Pos.CENTER);

        Button cadastrarTarefaButton = new Button("Cadastrar Tarefa");
        cadastrarTarefaButton.setMinSize(100, 30);
        cadastrarTarefaButton.getStyleClass().add("cadastrar-tarefa-button");
        cadastrarTarefaButton.setOnAction(e -> new CadastrarTarefaContent(primaryStage, userId, this::atualizarTarefas));

        Button duplicarTarefasButton = new Button("Duplicar Tarefas");
        duplicarTarefasButton.setMinSize(100, 30);
        duplicarTarefasButton.getStyleClass().add("duplicar-tarefas-button");
        duplicarTarefasButton.setOnAction(e -> duplicarTarefas());

        buttonBox.getChildren().addAll(cadastrarTarefaButton, duplicarTarefasButton);

        Label statusLabel = new Label("Status:");
        statusLabel.getStyleClass().add("filter-label");

        statusFilterComboBox = new ComboBox<>();
        statusFilterComboBox.getItems().addAll("Todos", "Aberta", "Concluída", "Pendência Interna", "Pendência Externa", "Atrasada");
        statusFilterComboBox.setValue("Todos"); // Define o valor padrão
        statusFilterComboBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-width: 1px; -fx-border-radius: 5px;");

        HBox statusFilterBox = new HBox(10); // Espaço entre o Label e o ComboBox
        statusFilterBox.setAlignment(Pos.CENTER); // Alinha ao centro
        statusFilterBox.getChildren().addAll(statusLabel, statusFilterComboBox);

        Label tipoLabel = new Label("Tipo:");
        tipoLabel.getStyleClass().add("filter-label");

        tituloFilterComboBox = new ComboBox<>();
        tituloFilterComboBox.getItems().addAll("Todos", "Digitação", "Fechamento", "Obrigação Acessória", "Outros");
        tituloFilterComboBox.setValue("Todos"); // Define o valor padrão
        tituloFilterComboBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-width: 1px; -fx-border-radius: 5px;");

        HBox tipoFilterBox = new HBox(10); // Espaço entre o Label e o ComboBox
        tipoFilterBox.setAlignment(Pos.CENTER); // Alinha ao centro
        tipoFilterBox.getChildren().addAll(tipoLabel, tituloFilterComboBox);

        dataFilterDatePickerInicial = new DatePicker();
        dataFilterDatePickerFinal = new DatePicker();

        HBox dataFilterBox = new HBox(10);
        dataFilterBox.setAlignment(Pos.CENTER);
        dataFilterBox.getChildren().addAll(dataFilterDatePickerInicial, dataFilterDatePickerFinal);

        Button filtrarButton = new Button("Filtrar");
        filtrarButton.setMinSize(100, 30);
        filtrarButton.getStyleClass().add("filtrar-button");
        filtrarButton.setOnAction(e -> atualizarTarefas()); // Chama a função de atualizar as tarefas

        buttonAndFilterBox.getChildren().addAll(buttonBox, statusFilterBox, tipoFilterBox, dataFilterBox, filtrarButton);

        tarefasBox.getChildren().addAll(tarefasTitle, buttonAndFilterBox);

        // Cria o ScrollPane para a lista de tarefas
        tarefasScrollPane = new ScrollPane(tarefasBox);
        tarefasScrollPane.setPrefWidth(360);
        tarefasScrollPane.setPrefHeight(500); // Ajuste conforme necessário

        // Cria o VBox para o calendário
        calendarBox = new VBox();
        calendarBox.setPrefWidth(350);
        calendarBox.setPrefHeight(500);
        calendarBox.getStyleClass().add("container");
        calendarBox.setAlignment(Pos.CENTER); // Centraliza verticalmente e horizontalmente

        // Adiciona botões para navegação entre meses
        HBox navBox = new HBox();
        navBox.setSpacing(20);
        navBox.setAlignment(Pos.CENTER);
        navBox.setPadding(new Insets(0, 0, 20, 0)); // Adiciona margem inferior

        Button prevMonthButton = new Button("<");
        Button nextMonthButton = new Button(">");

        prevMonthButton.getStyleClass().add("nav-button"); // Aplica o estilo
        nextMonthButton.getStyleClass().add("nav-button"); // Aplica o estilo

        prevMonthButton.setOnAction(e -> changeMonth(-1));
        nextMonthButton.setOnAction(e -> changeMonth(1));

        Label monthLabel = new Label(currentDate.getMonth().name());
        monthLabel.getStyleClass().add("month-label");

        navBox.getChildren().addAll(prevMonthButton, monthLabel, nextMonthButton);

        calendarBox.getChildren().add(navBox);

        // Criação do GridPane para o calendário
        calendarGrid = new GridPane();
        calendarGrid.setHgap(10);
        calendarGrid.setVgap(10);
        calendarGrid.getStyleClass().add("calendar-grid");
        calendarGrid.setAlignment(Pos.CENTER);

        // Adiciona os títulos dos dias da semana
        String[] diasSemana = {"Seg", "Ter", "Qua", "Qui", "Sex", "Sab", "Dom"};
        for (int i = 0; i < diasSemana.length; i++) {
            Label dayLabel = new Label(diasSemana[i]);
            dayLabel.getStyleClass().add("day-label");
            dayLabel.setAlignment(Pos.CENTER);
            GridPane.setHalignment(dayLabel, javafx.geometry.HPos.CENTER);
            calendarGrid.add(dayLabel, i, 0);
        }

        calendarBox.getChildren().add(calendarGrid);

        // Cria uma VBox para o botão de exportar com margem inferior
        HBox exportBox = new HBox();
        exportBox.setAlignment(Pos.CENTER);
        exportBox.setPadding(new Insets(30, 0, 10, 0)); // Adiciona margem inferior
        exportBox.setSpacing(10);

        // Botão de exportar
        Button exportPdfButton = new Button("Exportar PDF");
        exportPdfButton.getStyleClass().add("export-button");
        exportPdfButton.setOnAction(e -> exportData());

        Button exportSheetButton = new Button("Exportar Planilha");
        exportSheetButton.getStyleClass().add("export-button");
        exportSheetButton.setOnAction(e -> exportarPlanilha());

        exportBox.getChildren().addAll(exportPdfButton, exportSheetButton);

        calendarBox.getChildren().add(exportBox);

        // VBox para o conteúdo das tarefas do dia
        tarefaContentBox = new VBox();
        tarefaContentBox.setPrefWidth(347);
        tarefaContentBox.setSpacing(10);
        tarefaContentBox.getStyleClass().add("container");

        // Cria o ScrollPane para o conteúdo das tarefas do dia
        tarefaContentScrollPane = new ScrollPane(tarefaContentBox);
        tarefaContentScrollPane.setPrefWidth(360);
        tarefaContentScrollPane.setPrefHeight(500); // Ajuste conforme necessário

        tarefaContentBox.getChildren().add(new Label("Clique em um dia para ver as tarefas"));

        // Adiciona os ScrollPanes e o VBox do calendário ao HBox principal
        getChildren().addAll(
                tarefasScrollPane,
                calendarBox,
                tarefaContentScrollPane
        );

        // Atualiza o calendário
        updateCalendar();

        // Carrega as tarefas ao iniciar
        atualizarTarefas();
    }

    private void exportData() {
        exportarPDF();
    }

    public void atualizarTarefas() {
        try {
            // Obtém os valores selecionados do ComboBox e carrega as tarefas
            String selectedStatus = statusFilterComboBox.getValue();
            String selectedTitulo = tituloFilterComboBox.getValue();
            LocalDate dataInicial = dataFilterDatePickerInicial.getValue();
            LocalDate dataFinal = dataFilterDatePickerFinal.getValue();
            loadTarefas(selectedStatus, selectedTitulo, dataInicial, dataFinal);
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace(); // Tratar erros adequadamente
        }
    }

    private void loadTarefas(String statusFiltro, String tituloFiltro, LocalDate dataInicial, LocalDate dataFinal) throws IOException, GeneralSecurityException {
        List<Tarefa> tarefas = MeuCronograma.getTarefasPorUsuario(userId);

        // Filtra as tarefas com base no status selecionado
        if (!statusFiltro.equals("Todos")) {
            tarefas.removeIf(tarefa -> !tarefa.getStatus().equals(statusFiltro));
        }

        // Filtra as tarefas com base no título selecionado
        if (!tituloFiltro.equals("Todos")) {
            tarefas.removeIf(tarefa -> !tarefa.getTitulo().equals(tituloFiltro));
        }

        // Filtra as tarefas com base no intervalo de datas selecionado
        if (dataInicial != null && dataFinal != null) {
            tarefas.removeIf(tarefa -> tarefa.getDia() == null || tarefa.getDia().isBefore(dataInicial) || tarefa.getDia().isAfter(dataFinal));
        }

        // Ordena a lista de tarefas por data (do dia mais antigo para o mais atual)
        tarefas.sort(Comparator.comparing(
                tarefa -> tarefa.getDia() != null ? tarefa.getDia() : LocalDate.MAX // Define uma data alta para tarefas com data null
        ));

        // Remove apenas as caixas de tarefas, não os botões ou o ComboBox
        tarefasBox.getChildren().removeIf(node -> node instanceof VBox && ((VBox) node).getStyleClass().contains("tarefa-box"));

        for (Tarefa tarefa : tarefas) {
            VBox tarefaBox = new VBox();
            tarefaBox.setPadding(new Insets(10));
            tarefaBox.setSpacing(5);
            tarefaBox.getStyleClass().add("tarefa-box");
            tarefaBox.setUserData(tarefa.getId());

            // Define a cor da borda com base no status
            setTarefaBorderColor(tarefaBox, tarefa.getStatus());

            Label titleLabel = new Label("Tipo: " + tarefa.getTitulo());
            titleLabel.getStyleClass().add("tarefa-titulo");

            Label descriptionLabel = new Label("Descrição/Empresa: " + tarefa.getDescricao());
            descriptionLabel.getStyleClass().add("tarefa-descricao");

            Label dayLabel = new Label("Data: " + (tarefa.getDia() != null ? tarefa.getDia().toString() : "Não definido"));
            dayLabel.getStyleClass().add("tarefa-dia");

            Label statusLabel = new Label("Status: " + tarefa.getStatus());
            statusLabel.getStyleClass().add("tarefa-status");

            tarefaBox.getChildren().addAll(titleLabel, descriptionLabel, dayLabel, statusLabel);

            tarefaBox.setOnMouseClicked(event -> openDateSelectionWindow(tarefaBox));

            tarefasBox.getChildren().add(tarefaBox);
        }

        // Atualiza o layout após as mudanças
        tarefasBox.layout();
    }

    private void changeMonth(int offset) {
        currentDate = currentDate.plusMonths(offset);
        updateCalendar();
    }

    private void updateCalendar() {
        calendarGrid.getChildren().clear();

        String[] diasSemana = {"Seg", "Ter", "Qua", "Qui", "Sex", "Sab", "Dom"};
        for (int i = 0; i < diasSemana.length; i++) {
            Label dayLabel = new Label(diasSemana[i]);
            dayLabel.getStyleClass().add("day-label");
            dayLabel.setAlignment(Pos.CENTER);
            GridPane.setHalignment(dayLabel, javafx.geometry.HPos.CENTER);
            calendarGrid.add(dayLabel, i, 0);
        }

        LocalDate firstDayOfMonth = LocalDate.of(currentDate.getYear(), currentDate.getMonth(), 1);
        int firstDayOfWeek = firstDayOfMonth.getDayOfWeek().getValue();
        int daysInMonth = firstDayOfMonth.lengthOfMonth();

        int startColumn = (firstDayOfWeek - 1 + 7) % 7;

        for (int day = 1; day <= daysInMonth; day++) {
            final int dayFinal = day;
            Button dayButton = new Button(String.valueOf(day));
            dayButton.getStyleClass().add("day-button");
            dayButton.setOnAction(e -> showTarefasDiaContent(dayFinal));
            dayButton.setPrefSize(50, 50);
            dayButton.setAlignment(Pos.CENTER);
            GridPane.setHalignment(dayButton, javafx.geometry.HPos.CENTER);
            dayButton.setUserData(day); // Define o dia como userData

            LocalDate today = LocalDate.now();
            if (today.getYear() == currentDate.getYear() &&
                    today.getMonth() == currentDate.getMonth() &&
                    today.getDayOfMonth() == day) {
                dayButton.getStyleClass().add("today-button");
            }

            calendarGrid.add(dayButton, (day + startColumn - 1) % 7, (day + startColumn - 1) / 7 + 1);
        }

        Label monthLabel = (Label) ((HBox) calendarBox.getChildren().get(0)).getChildren().get(1);
        monthLabel.setText(currentDate.getMonth().name());
    }

    private void showTarefasDiaContent(int day) {
        LocalDate selectedDate = LocalDate.of(currentDate.getYear(), currentDate.getMonth(), day);
        tarefaContentBox.getChildren().clear();
        tarefaContentBox.getChildren().add(new Label("Tarefas para a data " + selectedDate));

        try {
            List<Tarefa> tarefasParaDia = MeuCronograma.getTarefasPorDia(userId, selectedDate);

            // Ordena a lista de tarefas por data (caso tenha mais de uma data para um mesmo dia)
            tarefasParaDia.sort(Comparator.comparing(Tarefa::getDia));

            for (Tarefa tarefa : tarefasParaDia) {
                VBox tarefaBox = new VBox();
                tarefaBox.setPadding(new Insets(10));
                tarefaBox.setSpacing(5);
                tarefaBox.getStyleClass().add("tarefa-box");

                // Define a cor da borda com base no status
                setTarefaBorderColor(tarefaBox, tarefa.getStatus());

                Label titleLabel = new Label("Tipo: " + tarefa.getTitulo());
                titleLabel.getStyleClass().add("tarefa-titulo");

                Label descriptionLabel = new Label("Descrição/Empresa: " + tarefa.getDescricao());
                descriptionLabel.getStyleClass().add("tarefa-descricao");

                Label dayLabel = new Label("Data: " + tarefa.getDia());
                dayLabel.getStyleClass().add("tarefa-dia");

                Label statusLabel = new Label("Status: " + tarefa.getStatus());
                statusLabel.getStyleClass().add("tarefa-status");

                tarefaBox.getChildren().addAll(titleLabel, descriptionLabel, dayLabel, statusLabel);

                // Adiciona um evento de clique ao VBox
                tarefaBox.setOnMouseClicked(event -> openStatusSelectionWindow(tarefaBox));

                // Define o ID da tarefa como userData do VBox
                tarefaBox.setUserData(tarefa.getId());

                tarefaContentBox.getChildren().add(tarefaBox);
            }
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    private void openStatusSelectionWindow(VBox tarefaBox) {
        try {
            Stage statusStage = new Stage();
            statusStage.initModality(Modality.APPLICATION_MODAL);
            statusStage.setTitle("Selecionar Status");

            // Cria e estiliza o layout principal
            VBox statusLayout = new VBox();
            statusLayout.setPadding(new Insets(20));
            statusLayout.setSpacing(10);
            statusLayout.setAlignment(Pos.CENTER);
            statusLayout.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #cccccc; -fx-border-width: 1px; -fx-border-radius: 5px;");

            // Cria e estiliza o label
            Label selectStatusLabel = new Label("Selecione o status para a tarefa:");
            selectStatusLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            // Cria e estiliza o ComboBox
            ComboBox<String> statusComboBox = new ComboBox<>();
            statusComboBox.getItems().addAll("Aberta", "Concluída", "Pendência Interna", "Pendência Externa", "Atrasada");
            statusComboBox.setValue("Aberta"); // Define o valor padrão
            statusComboBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-width: 1px; -fx-border-radius: 5px;");

            // Cria e estiliza o botão
            Button selectButton = new Button("Selecionar Status");
            selectButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10px; -fx-border-radius: 5px; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
            selectButton.setOnAction(e -> {
                String selectedStatus = statusComboBox.getValue();
                if (selectedStatus != null) {
                    // Obtém o ID da tarefa a partir do userData do VBox
                    String tarefaId = null;
                    Object userData = tarefaBox.getUserData();
                    if (userData instanceof String) {
                        tarefaId = (String) userData;
                    }

                    if (tarefaId != null) {
                        try {
                            MeuCronograma.atualizarStatusTarefa(tarefaId, selectedStatus);
                            tarefaContentBox.getChildren().clear();
                            tarefaContentBox.getChildren().add(new Label("Tarefas para a data " + currentDate));
                            atualizarTarefas(); // Atualiza a lista de tarefas
                            statusStage.close();
                        } catch (IOException | GeneralSecurityException ex) {
                            ex.printStackTrace(); // Tratar erros adequadamente
                        }
                    }
                }
            });

            // Adiciona os componentes ao layout
            statusLayout.getChildren().addAll(selectStatusLabel, statusComboBox, selectButton);

            // Cria a cena e configura a janela
            Scene scene = new Scene(statusLayout, 300, 200);
            statusStage.setScene(scene);
            statusStage.show();
        } catch (Exception e) {
            e.printStackTrace(); // Log de erro para depuração
        }
    }

    private void setTarefaBorderColor(VBox tarefaBox, String status) {
        switch (status) {
            case "Aberta":
                tarefaBox.setStyle("-fx-border-color: gray; -fx-border-width: 2px;");
                break;
            case "Atrasada":
                tarefaBox.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-background-color: #ff7d7d");
                break;
            case "Concluída":
                tarefaBox.setStyle("-fx-border-color: green; -fx-border-width: 2px; -fx-background-color: #99ff7d");
                break;
            case "Pendência Interna":
                tarefaBox.setStyle("-fx-border-color: yellow; -fx-border-width: 2px; -fx-background-color: #ffff7d");
                break;
            case "Pendência Externa":
                tarefaBox.setStyle("-fx-border-color: #c800ff; -fx-border-width: 2px; -fx-background-color: #c87dff");
                break;
            default:
                tarefaBox.setStyle("-fx-border-color: black; -fx-border-width: 2px;"); // Cor padrão
                break;
        }
    }

    private void openDateSelectionWindow(VBox tarefaBox) {
        try {
            Stage dateStage = new Stage();
            dateStage.initModality(Modality.APPLICATION_MODAL);
            dateStage.setTitle("Selecionar Data");

            // Cria e estiliza o layout principal
            VBox dateLayout = new VBox();
            dateLayout.setPadding(new Insets(20));
            dateLayout.setSpacing(10);
            dateLayout.setAlignment(Pos.CENTER);
            dateLayout.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #cccccc; -fx-border-width: 1px; -fx-border-radius: 5px;");

            // Cria e estiliza o label
            Label selectDateLabel = new Label("Selecione a data para a tarefa:");
            selectDateLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            // Cria e estiliza o DatePicker
            DatePicker datePicker = new DatePicker();
            datePicker.setValue(LocalDate.now()); // Define a data atual
            datePicker.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-width: 1px; -fx-border-radius: 5px;");

            // Cria e estiliza o botão de Selecionar Data
            Button selectButton = new Button("Selecionar");
            selectButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10px; -fx-border-radius: 5px; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
            selectButton.setOnAction(e -> {
                LocalDate selectedDate = datePicker.getValue();
                if (selectedDate != null) {
                    // Obtém o ID da tarefa a partir do userData do VBox
                    String tarefaId = null;
                    Object userData = tarefaBox.getUserData();
                    if (userData instanceof String) {
                        tarefaId = (String) userData;
                    }

                    if (tarefaId != null) {
                        try {
                            MeuCronograma.atualizarDataTarefa(tarefaId, selectedDate);
                            tarefaContentBox.getChildren().clear();
                            tarefaContentBox.getChildren().add(new Label("Tarefas para a data " + selectedDate));
                            atualizarTarefas(); // Atualiza a lista de tarefas
                            dateStage.close();
                        } catch (IOException | GeneralSecurityException ex) {
                            ex.printStackTrace(); // Tratar erros adequadamente
                        }
                    }
                }
            });

            // Cria e estiliza o botão de Excluir
            Button deleteButton = new Button("Excluir");
            deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 10px; -fx-border-radius: 5px; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
            deleteButton.setOnAction(e -> {
                // Obtém o ID da tarefa a partir do userData do VBox
                String tarefaId = null;
                Object userData = tarefaBox.getUserData();
                if (userData instanceof String) {
                    tarefaId = (String) userData;
                }

                if (tarefaId != null) {
                    try {
                        MeuCronograma.excluirTarefa(tarefaId);
                        tarefaContentBox.getChildren().clear();
                        atualizarTarefas(); // Atualiza a lista de tarefas
                        dateStage.close();
                    } catch (IOException | GeneralSecurityException ex) {
                        ex.printStackTrace(); // Tratar erros adequadamente
                    }
                }
            });

            // Cria um HBox para manter os botões lado a lado
            HBox buttonBox = new HBox(10); // Espaçamento de 10px entre os botões
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.getChildren().addAll(selectButton, deleteButton);

            // Adiciona os componentes ao layout
            dateLayout.getChildren().addAll(selectDateLabel, datePicker, buttonBox);

            // Cria a cena e configura a janela
            Scene scene = new Scene(dateLayout, 300, 200);
            dateStage.setScene(scene);
            dateStage.show();
        } catch (Exception e) {
            e.printStackTrace(); // Log de erro para depuração
        }
    }

    public void exportarPDF() {
        Stage stage = (Stage) getScene().getWindow();

        // Cria o FileChooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        // Abre o diálogo para escolher o local para salvar
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                // Gera o PDF
                gerarPDF(file);
            } catch (DocumentException | IOException | GeneralSecurityException e) {
                e.printStackTrace(); // Tratar erros adequadamente
            }
        }
    }

    private void gerarPDF(File file) throws IOException, DocumentException, GeneralSecurityException {
        // Obtém as tarefas do usuário
        List<Tarefa> tarefas = MeuCronograma.getTarefasPorUsuario(userId);

        // Filtra as tarefas para o mês atual
        LocalDate hoje = LocalDate.now();
        int mesAtual = hoje.getMonthValue();
        tarefas.removeIf(tarefa -> tarefa.getDia() == null || tarefa.getDia().getMonthValue() != mesAtual);

        // Ordena as tarefas por data em ordem crescente
        tarefas.sort((t1, t2) -> t1.getDia().compareTo(t2.getDia()));

        // Cria o documento PDF com margens ajustadas
        Document document = new Document(PageSize.A4, 50, 50, 60, 50); // Margens: esquerda, direita, superior, inferior
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
        HeaderFooterPageEvent event = new HeaderFooterPageEvent(getLogo(), new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
        writer.setPageEvent(event);
        document.open();

        // Adiciona o título ao PDF
        Paragraph titulo = new Paragraph("Cronograma", new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD));
        titulo.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(titulo);

        // Cria uma tabela com colunas para tipo, descrição, data e status
        PdfPTable table = new PdfPTable(4); // Usa 4 colunas
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f); // Espaço antes da tabela

        // Define o estilo das células da tabela
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
        Font contentFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);

        // Cabeçalhos das colunas
        addCellToTable(table, "Data", headerFont, BaseColor.BLUE);
        addCellToTable(table, "Tipo", headerFont, BaseColor.BLUE);
        addCellToTable(table, "Descrição/Empresa", headerFont, BaseColor.BLUE);
        addCellToTable(table, "Status", headerFont, BaseColor.BLUE);

        for (Tarefa tarefa : tarefas) {
            addCellToTable(table, (tarefa.getDia() != null ? tarefa.getDia().toString() : "Não definido"), contentFont);
            addCellToTable(table, tarefa.getTitulo(), contentFont);
            addCellToTable(table, tarefa.getDescricao(), contentFont);
            addCellToTable(table, tarefa.getStatus(), contentFont);
        }

        document.add(table);
        document.close();
    }

    private void addCellToTable(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5f); // Ajusta o padding para um layout mais compacto
        cell.setBorderColor(BaseColor.LIGHT_GRAY);
        cell.setBorderWidth(0.5f);
        table.addCell(cell);
    }

    private void addCellToTable(PdfPTable table, String text, Font font, BaseColor backgroundColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5f); // Ajusta o padding para um layout mais compacto
        cell.setBackgroundColor(backgroundColor);
        cell.setBorderColor(BaseColor.LIGHT_GRAY);
        cell.setBorderWidth(0.5f);
        table.addCell(cell);
    }

    private Image getLogo() throws IOException, BadElementException {
        // Carrega o logo a partir do classpath
        String logoPath = "/org/project/images/icon.png";
        InputStream logoStream = getClass().getResourceAsStream(logoPath);
        if (logoStream == null) {
            throw new FileNotFoundException("Logo não encontrado: " + logoPath);
        }
        Image logo = Image.getInstance(ImageIO.read(logoStream), null);
        float width = 30f;
        float height = 30f;
        logo.scaleToFit(width, height);
        return logo;
    }


    private void exportarPlanilha() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar Planilha");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showSaveDialog(primaryStage);

        if (file != null) {
            try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                XSSFSheet sheet = workbook.createSheet("Tarefas");

                // Criação do cabeçalho
                Row headerRow = sheet.createRow(0);
                Cell headerCell0 = headerRow.createCell(0);
                headerCell0.setCellValue("Tipo");

                Cell headerCell1 = headerRow.createCell(1);
                headerCell1.setCellValue("Descrição/Empresa");

                Cell headerCell2 = headerRow.createCell(2);
                headerCell2.setCellValue("Data");

                Cell headerCell3 = headerRow.createCell(3);
                headerCell3.setCellValue("Status");

                // Adiciona as tarefas
                List<Tarefa> tarefas = MeuCronograma.getTarefasPorUsuario(userId);

                // Filtra as tarefas para o mês atual
                LocalDate hoje = LocalDate.now();
                int mesAtual = hoje.getMonthValue();
                tarefas.removeIf(tarefa -> tarefa.getDia() == null || tarefa.getDia().getMonthValue() != mesAtual);

                // Ordena as tarefas por data em ordem crescente
                tarefas.sort((t1, t2) -> t1.getDia().compareTo(t2.getDia()));

                int rowNum = 1;
                for (Tarefa tarefa : tarefas) {
                    Row row = sheet.createRow(rowNum++);

                    Cell cell0 = row.createCell(0);
                    cell0.setCellValue(tarefa.getTitulo());

                    Cell cell1 = row.createCell(1);
                    cell1.setCellValue(tarefa.getDescricao());

                    Cell cell2 = row.createCell(2);
                    cell2.setCellValue(tarefa.getDia() != null ? tarefa.getDia().toString() : "Não definido");

                    Cell cell3 = row.createCell(3);
                    cell3.setCellValue(tarefa.getStatus());
                }

                // Autoajusta a largura das colunas
                for (int i = 0; i < 4; i++) {
                    sheet.autoSizeColumn(i);
                }

                // Salva a planilha
                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    workbook.write(fileOut);
                }

            } catch (IOException | GeneralSecurityException e) {
                e.printStackTrace(); // Tratar erros adequadamente
            }
        }
    }

    private void duplicarTarefas() {
        Stage duplicarStage = new Stage();
        duplicarStage.initModality(Modality.APPLICATION_MODAL);
        duplicarStage.setTitle("Duplicar Tarefas");

        VBox duplicarLayout = new VBox();
        duplicarLayout.setPadding(new Insets(20));
        duplicarLayout.setSpacing(10);
        duplicarLayout.setAlignment(Pos.CENTER);

        Label mesCopiaLabel = new Label("Selecione o mês de origem:");
        mesCopiaLabel.getStyleClass().add("duplicar-dialog-label");

        ComboBox<String> mesCopiaComboBox = new ComboBox<>();
        mesCopiaComboBox.getItems().addAll(
                "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        );
        mesCopiaComboBox.getStyleClass().add("duplicar-dialog-combobox");

        Label mesAlvoLabel = new Label("Selecione o mês de destino:");
        mesAlvoLabel.getStyleClass().add("duplicar-dialog-label");

        ComboBox<String> mesAlvoComboBox = new ComboBox<>();
        mesAlvoComboBox.getItems().addAll(
                "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        );
        mesAlvoComboBox.getStyleClass().add("duplicar-dialog-combobox");

        Button duplicarButton = new Button("Duplicar");
        duplicarButton.getStyleClass().add("duplicar-dialog-button");
        duplicarButton.setOnAction(e -> {
            String mesCopia = mesCopiaComboBox.getValue();
            String mesAlvo = mesAlvoComboBox.getValue();
            if (mesCopia != null && mesAlvo != null) {
                // Lógica para duplicar as tarefas aqui, usando mesCopia e mesAlvo
                try {
                    duplicarTarefasPorMes(userId, mesCopia, mesAlvo);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                } catch (GeneralSecurityException ex) {
                    throw new RuntimeException(ex);
                }
                duplicarStage.close();
            }
        });

        duplicarLayout.getChildren().addAll(mesCopiaLabel, mesCopiaComboBox, mesAlvoLabel, mesAlvoComboBox, duplicarButton);

        Scene scene = new Scene(duplicarLayout, 300, 250);
        scene.getStylesheets().add(getClass().getResource("/org/project/styles/content-styles.css").toExternalForm()); // Atualize o caminho para o CSS
        duplicarStage.setScene(scene);
        duplicarStage.showAndWait();
    }

    private void duplicarTarefasPorMes(String userId, String mesCopia, String mesAlvo) throws IOException, GeneralSecurityException {
        int mesOrigem = obterNumeroDoMes(mesCopia);
        int mesDestino = obterNumeroDoMes(mesAlvo);

        // Chama a função de duplicar tarefas do pacote org.project.functions.MeuCronograma
        MeuCronograma.duplicarTarefas(userId, mesOrigem, mesDestino);

        // Atualizar a interface do usuário para refletir as mudanças
        atualizarTarefas();
    }

    private static final Map<String, Integer> mesMap = new HashMap<>();

    static {
        mesMap.put("Janeiro", 1);
        mesMap.put("Fevereiro", 2);
        mesMap.put("Março", 3);
        mesMap.put("Abril", 4);
        mesMap.put("Maio", 5);
        mesMap.put("Junho", 6);
        mesMap.put("Julho", 7);
        mesMap.put("Agosto", 8);
        mesMap.put("Setembro", 9);
        mesMap.put("Outubro", 10);
        mesMap.put("Novembro", 11);
        mesMap.put("Dezembro", 12);
    }

    public static int obterNumeroDoMes(String mes) {
        return mesMap.getOrDefault(mes, -1);
    }
}