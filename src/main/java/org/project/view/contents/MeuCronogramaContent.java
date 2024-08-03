package org.project.view.contents;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.project.functions.MeuCronograma;

import java.io.*;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
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
    private ScrollPane tarefaContentScrollPane;

    public MeuCronogramaContent(Stage primaryStage, String userId) {
        this.primaryStage = primaryStage;
        this.currentDate = LocalDate.now();
        this.userId = userId;

        // Configura o layout principal
        setPadding(new Insets(20));
        setSpacing(20);
        getStyleClass().add("meu-cronograma-content");
        setAlignment(Pos.CENTER);

        // VBox para a lista de tarefas
        tarefasBox = new VBox();
        tarefasBox.setPrefWidth(350);
        tarefasBox.setSpacing(10); // Espaço entre os elementos
        tarefasBox.getStyleClass().add("container");

        Label tarefasTitle = new Label("Lista de Tarefas");
        tarefasTitle.getStyleClass().add("tarefas-title");

// HBox para os botões de cadastrar e resetar tarefas
        HBox buttonBox = new HBox();
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(Pos.CENTER);

        // Botão de cadastrar tarefa
        Button cadastrarTarefaButton = new Button("Cadastrar Tarefa");
        cadastrarTarefaButton.getStyleClass().add("cadastrar-tarefa-button");
        cadastrarTarefaButton.setOnAction(e -> new CadastrarTarefaContent(primaryStage, userId, this::atualizarTarefas)); // Passa o callback

        // Botão de resetar tarefas
        Button resetarTarefasButton = new Button("Resetar");
        resetarTarefasButton.getStyleClass().add("resetar-tarefas-button");
        resetarTarefasButton.setOnAction(e -> resetarTarefas());

        buttonBox.getChildren().addAll(cadastrarTarefaButton, resetarTarefasButton);

        tarefasBox.getChildren().addAll(tarefasTitle, buttonBox);

        // Cria o ScrollPane para a lista de tarefas
        tarefasScrollPane = new ScrollPane(tarefasBox);
        tarefasScrollPane.setPrefWidth(350);
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
        exportBox.setPadding(new Insets(10, 0, 10, 0)); // Adiciona margem inferior
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
        tarefaContentBox.setPrefWidth(350);
        tarefaContentBox.setPrefHeight(400);
        tarefaContentBox.getStyleClass().add("container");
        tarefaContentBox.setAlignment(Pos.CENTER); // Centraliza verticalmente e horizontalmente

        // Cria o ScrollPane para o conteúdo das tarefas do dia
        tarefaContentScrollPane = new ScrollPane(tarefaContentBox);
        tarefaContentScrollPane.setPrefWidth(350);
        tarefaContentScrollPane.setPrefHeight(400); // Ajuste conforme necessário

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
            loadTarefas();
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace(); // Tratar erros adequadamente
        }
    }

    private void loadTarefas() throws IOException, GeneralSecurityException {
        // Obtém as tarefas do usuário
        List<Tarefa> tarefas = MeuCronograma.getTarefasPorUsuario(userId);
        tarefasBox.getChildren().removeIf(node -> node instanceof VBox); // Remove tarefas anteriores, se houver

        for (Tarefa tarefa : tarefas) {
            VBox tarefaBox = new VBox();
            tarefaBox.setPadding(new Insets(10));
            tarefaBox.setSpacing(5);
            tarefaBox.getStyleClass().add("tarefa-box");
            tarefaBox.setUserData(tarefa.getId()); // Define o ID da tarefa como userData

            Label titleLabel = new Label("Título: " + tarefa.getTitulo()); // Título
            titleLabel.getStyleClass().add("tarefa-titulo");

            Label descriptionLabel = new Label("Descrição: " + tarefa.getDescricao()); // Descrição
            descriptionLabel.getStyleClass().add("tarefa-descricao");

            Label dayLabel = new Label("Dia: " + (tarefa.getDia() != null ? tarefa.getDia().toString() : "Não definido")); // Data
            dayLabel.getStyleClass().add("tarefa-dia");

            Label statusLabel = new Label("Status: " + tarefa.getStatus()); // Status
            statusLabel.getStyleClass().add("tarefa-status");

            tarefaBox.getChildren().addAll(titleLabel, descriptionLabel, dayLabel, statusLabel);

            // Configura evento de clique para abrir a janela de seleção de data
            tarefaBox.setOnMouseClicked(e -> openDateSelectionWindow(tarefaBox));

            tarefasBox.getChildren().add(tarefaBox);
        }
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
        tarefaContentBox.getChildren().add(new Label("Tarefas para o dia " + selectedDate));

        try {
            List<Tarefa> tarefasParaDia = MeuCronograma.getTarefasPorDia(userId, selectedDate);
            for (Tarefa tarefa : tarefasParaDia) {
                VBox tarefaBox = new VBox();
                tarefaBox.setPadding(new Insets(10));
                tarefaBox.setSpacing(5);
                tarefaBox.getStyleClass().add("tarefa-box");

                Label titleLabel = new Label("Título: " + tarefa.getTitulo());
                titleLabel.getStyleClass().add("tarefa-titulo");

                Label descriptionLabel = new Label("Descrição: " + tarefa.getDescricao());
                descriptionLabel.getStyleClass().add("tarefa-descricao");

                Label dayLabel = new Label("Dia: " + tarefa.getDia());
                dayLabel.getStyleClass().add("tarefa-dia");

                Label statusLabel = new Label("Status: " + tarefa.getStatus());
                statusLabel.getStyleClass().add("tarefa-status");

                tarefaBox.getChildren().addAll(titleLabel, descriptionLabel, dayLabel, statusLabel);

                tarefaContentBox.getChildren().add(tarefaBox);
            }
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace(); // Tratar erros adequadamente
        }
    }

    private void openDateSelectionWindow(VBox tarefaBox) {
        Stage dateStage = new Stage();
        dateStage.initModality(Modality.APPLICATION_MODAL);
        dateStage.setTitle("Selecionar Data");

        VBox dateLayout = new VBox();
        dateLayout.setPadding(new Insets(20));
        dateLayout.setSpacing(10);
        dateLayout.setAlignment(Pos.CENTER);

        Label selectDateLabel = new Label("Selecione a data para a tarefa:");

        // Adiciona um DatePicker para selecionar a data
        DatePicker datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now()); // Define a data atual

        Button selectButton = new Button("Selecionar Data");
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
                        tarefaContentBox.getChildren().add(new Label("Tarefas para o dia " + selectedDate));
                        atualizarTarefas(); // Atualiza a lista de tarefas
                        dateStage.close();
                    } catch (IOException | GeneralSecurityException ex) {
                        ex.printStackTrace(); // Tratar erros adequadamente
                    }
                }
            }
        });

        dateLayout.getChildren().addAll(selectDateLabel, datePicker, selectButton);

        Scene scene = new Scene(dateLayout, 300, 200);
        dateStage.setScene(scene);
        dateStage.show();
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

        // Cria o documento PDF
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        // Adiciona o título ao PDF
        Paragraph titulo = new Paragraph("Lista de Tarefas", new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 16, com.itextpdf.text.Font.BOLD));
        titulo.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(titulo);
        document.add(new Paragraph("")); // Adiciona uma linha em branco

        // Cria uma tabela com duas colunas
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100); // Define a largura da tabela como 100% da largura da página
        table.setSpacingBefore(10); // Espaço antes da tabela
        table.setSpacingAfter(10); // Espaço depois da tabela

        // Adiciona as tarefas à tabela
        for (int i = 0; i < tarefas.size(); i += 2) {
            Tarefa tarefa1 = tarefas.get(i);
            PdfPCell cell1 = new PdfPCell();
            cell1.addElement(new Paragraph("Título: " + tarefa1.getTitulo()));
            cell1.addElement(new Paragraph("Descrição: " + tarefa1.getDescricao()));
            cell1.addElement(new Paragraph("Dia: " + (tarefa1.getDia() != null ? tarefa1.getDia().toString() : "Não definido")));
            cell1.addElement(new Paragraph("Status: " + tarefa1.getStatus()));
            table.addCell(cell1);

            // Adiciona a segunda tarefa se houver
            if (i + 1 < tarefas.size()) {
                Tarefa tarefa2 = tarefas.get(i + 1);
                PdfPCell cell2 = new PdfPCell();
                cell2.addElement(new Paragraph("Título: " + tarefa2.getTitulo()));
                cell2.addElement(new Paragraph("Descrição: " + tarefa2.getDescricao()));
                cell2.addElement(new Paragraph("Dia: " + (tarefa2.getDia() != null ? tarefa2.getDia().toString() : "Não definido")));
                cell2.addElement(new Paragraph("Status: " + tarefa2.getStatus()));
                table.addCell(cell2);
            } else {
                // Se houver apenas uma tarefa, adiciona uma célula vazia na segunda coluna
                table.addCell("");
            }
        }

        document.add(table);

        document.close();
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
                headerCell0.setCellValue("Título");

                Cell headerCell1 = headerRow.createCell(1);
                headerCell1.setCellValue("Descrição");

                Cell headerCell2 = headerRow.createCell(2);
                headerCell2.setCellValue("Data");

                Cell headerCell3 = headerRow.createCell(3);
                headerCell3.setCellValue("Status");

                // Adiciona as tarefas
                List<Tarefa> tarefas = MeuCronograma.getTarefasPorUsuario(userId);
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

    private void resetarTarefas() {
        try {
            MeuCronograma.resetarTarefas(userId); // Reseta todas as tarefas do usuário
            atualizarTarefas(); // Atualiza a lista de tarefas na interface
        } catch (IOException | GeneralSecurityException ex) {
            ex.printStackTrace(); // Tratar erros adequadamente
        }
    }
}