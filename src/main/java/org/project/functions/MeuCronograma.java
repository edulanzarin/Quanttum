package org.project.functions;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import org.project.model.Tarefa;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.*;
import java.time.format.DateTimeParseException;
import java.time.DayOfWeek;

public class MeuCronograma {

    private static final String SPREADSHEET_ID = "1G39rq0NGIMJ4LFHQ7-3unHAD39C_aZGAIZx1L3d7cD8"; // Substitua pelo ID da sua planilha
    private static final String SHEET_NAME = "tarefas"; // Nome da aba da planilha onde você quer adicionar a tarefa

    // Função para adicionar uma nova tarefa
    public static void addTarefa(String titulo, String descricao, LocalDate data, String usuarioId) throws IOException, GeneralSecurityException {
        Sheets service = SheetsServiceUtil.getSheetsService();

        // Gera um UUID aleatório para o ID
        String newId = UUID.randomUUID().toString();

        // Cria a nova linha de dados
        List<Object> rowData = new ArrayList<>();
        rowData.add(newId);                      // ID
        rowData.add(titulo);                     // Título
        rowData.add(descricao);                  // Descrição
        rowData.add(data != null ? data.toString() : ""); // Dia
        rowData.add("Aberta");                   // Status (definido como "Aberta")
        rowData.add(usuarioId);                  // Usuário

        // Adiciona a nova linha à planilha
        List<List<Object>> dataList = new ArrayList<>();
        dataList.add(rowData);

        ValueRange body = new ValueRange().setValues(dataList);
        AppendValuesResponse result = service.spreadsheets().values()
                .append(SPREADSHEET_ID, SHEET_NAME, body)
                .setValueInputOption("RAW")
                .execute();
    }

    // Função para obter as tarefas por usuário
    public static List<Tarefa> getTarefasPorUsuario(String usuarioId) throws IOException, GeneralSecurityException {
        Sheets service = SheetsServiceUtil.getSheetsService();
        String range = SHEET_NAME + "!A:F"; // Colunas da planilha (A a F)
        ValueRange response = service.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute();
        List<List<Object>> values = response.getValues();

        List<Tarefa> tarefasUsuario = new ArrayList<>();
        if (values != null) {
            for (List<Object> row : values) {
                if (row.size() > 5 && usuarioId.equals(row.get(5).toString())) {
                    String id = row.size() > 0 ? row.get(0).toString() : "";
                    String titulo = row.size() > 1 ? row.get(1).toString() : "";
                    String descricao = row.size() > 2 ? row.get(2).toString() : "";
                    LocalDate dataTarefa = null;
                    String dataStr = row.size() > 3 ? row.get(3).toString() : "";

                    // Verifica se o valor da data não está vazio e é um formato válido
                    if (!dataStr.isEmpty()) {
                        try {
                            dataTarefa = LocalDate.parse(dataStr);
                        } catch (DateTimeParseException e) {
                            // Se desejar, pode registrar ou tratar a data inválida aqui
                            dataTarefa = null; // Mantém como null em caso de data inválida
                        }
                    }

                    String status = row.size() > 4 ? row.get(4).toString() : "";

                    tarefasUsuario.add(new Tarefa(id, titulo, descricao, dataTarefa, status));
                }
            }
        }
        return tarefasUsuario;
    }

    // Função para obter as tarefas por data
    public static List<Tarefa> getTarefasPorDia(String usuarioId, LocalDate data) throws IOException, GeneralSecurityException {
        Sheets service = SheetsServiceUtil.getSheetsService();
        String range = SHEET_NAME + "!A:F"; // Colunas da planilha (A a F)
        ValueRange response = service.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute();
        List<List<Object>> values = response.getValues();

        List<Tarefa> tarefasPorDia = new ArrayList<>();
        if (values != null) {
            for (List<Object> row : values) {
                if (row.size() > 5 && usuarioId.equals(row.get(5).toString()) &&
                        data.toString().equals(row.get(3).toString())) {
                    String id = row.size() > 0 ? row.get(0).toString() : "";
                    String titulo = row.size() > 1 ? row.get(1).toString() : "";
                    String descricao = row.size() > 2 ? row.get(2).toString() : "";
                    LocalDate dataTarefa = row.size() > 3 ? LocalDate.parse(row.get(3).toString()) : null;
                    String status = row.size() > 4 ? row.get(4).toString() : "";

                    tarefasPorDia.add(new Tarefa(id, titulo, descricao, dataTarefa, status));
                }
            }
        }
        return tarefasPorDia;
    }

    // Atualiza a data de uma tarefa existente e define o status como "Aberta"
    public static void atualizarDataTarefa(String tarefaId, LocalDate novaData) throws IOException, GeneralSecurityException {
        Sheets service = SheetsServiceUtil.getSheetsService();
        String range = SHEET_NAME + "!A:F"; // Colunas da planilha (A a F)

        // Obter todas as tarefas
        ValueRange response = service.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute();
        List<List<Object>> values = response.getValues();

        // Encontrar a tarefa com o ID correspondente e atualizar a data e o status
        List<List<Object>> updatedData = new ArrayList<>();
        if (values != null) {
            for (List<Object> row : values) {
                if (row.size() > 0 && tarefaId.equals(row.get(0).toString())) {
                    row.set(3, novaData.toString()); // Atualiza a data na quarta coluna (índice 3)
                    row.set(4, "Aberta"); // Define o status como "Aberta" na quinta coluna (índice 4)
                }
                updatedData.add(row);
            }
        }

        // Atualizar a planilha com os dados modificados
        ValueRange body = new ValueRange().setValues(updatedData);
        UpdateValuesResponse result = service.spreadsheets().values()
                .update(SPREADSHEET_ID, SHEET_NAME, body)
                .setValueInputOption("RAW")
                .execute();
    }

    // Função para duplicar tarefas de um mês para outro
    public static void duplicarTarefas(String userId, int mesCopia, int mesAlvo) throws IOException, GeneralSecurityException {
        List<Tarefa> tarefasDoMes = getTarefasDoMes(userId, mesCopia);
        List<List<Object>> novasTarefas = new ArrayList<>();

        for (Tarefa tarefa : tarefasDoMes) {
            // Atualiza a data para o novo mês mantendo o mesmo dia
            LocalDate novaData = tarefa.getDia().withMonth(mesAlvo);

            // Verifica se a data resultante é um dia útil
            if (!isDiaUtil(novaData)) {
                // Ajusta para o próximo dia útil se a data resultante não for um dia útil
                novaData = ajustarParaProximoDiaUtil(novaData);
            }

            // Adiciona a nova tarefa à lista
            List<Object> novaTarefa = Arrays.asList(
                    UUID.randomUUID().toString(), // ID
                    tarefa.getTitulo(),            // Título
                    tarefa.getDescricao(),         // Descrição
                    novaData.toString(),           // Data
                    "Aberta",                    // Status
                    userId                         // Usuário
            );
            novasTarefas.add(novaTarefa);
        }

        // Insere todas as novas tarefas de uma vez
        if (!novasTarefas.isEmpty()) {
            inserirTarefasEmLote(novasTarefas);
        }
    }

    // Função para inserir várias tarefas de uma vez
    private static void inserirTarefasEmLote(List<List<Object>> novasTarefas) throws IOException, GeneralSecurityException {
        Sheets service = SheetsServiceUtil.getSheetsService();
        String range = SHEET_NAME + "!A:F"; // Colunas da planilha (A a F)

        ValueRange body = new ValueRange().setValues(novasTarefas);
        service.spreadsheets().values()
                .append(SPREADSHEET_ID, range, body)
                .setValueInputOption("RAW")
                .execute();
    }

    // Função para obter as tarefas de um mês específico para um usuário
    private static List<Tarefa> getTarefasDoMes(String usuarioId, int mes) throws IOException, GeneralSecurityException {
        List<Tarefa> todasTarefas = getTarefasPorUsuario(usuarioId);
        List<Tarefa> tarefasDoMes = new ArrayList<>();
        for (Tarefa tarefa : todasTarefas) {
            if (tarefa.getDia() != null && tarefa.getDia().getMonthValue() == mes) {
                tarefasDoMes.add(tarefa);
            }
        }
        return tarefasDoMes;
    }

    // Função para verificar se uma data é um dia útil (segunda a sexta)
    private static boolean isDiaUtil(LocalDate data) {
        DayOfWeek diaDaSemana = data.getDayOfWeek();
        return diaDaSemana != DayOfWeek.SATURDAY && diaDaSemana != DayOfWeek.SUNDAY;
    }

    // Função para ajustar uma data para o próximo dia útil
    private static LocalDate ajustarParaProximoDiaUtil(LocalDate data) {
        while (!isDiaUtil(data)) {
            data = data.plusDays(1);
        }
        return data;
    }

    public static void atualizarStatusTarefa(String tarefaId, String novoStatus) throws IOException, GeneralSecurityException {
        Sheets service = SheetsServiceUtil.getSheetsService();
        String range = SHEET_NAME + "!A:F"; // Colunas da planilha (A a F)

        // Obter todas as tarefas
        ValueRange response = service.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute();
        List<List<Object>> values = response.getValues();

        // Encontrar a tarefa com o ID correspondente e atualizar o status
        List<List<Object>> updatedData = new ArrayList<>();
        if (values != null) {
            for (List<Object> row : values) {
                if (row.size() > 0 && tarefaId.equals(row.get(0).toString())) {
                    row.set(4, novoStatus); // Atualiza o status na quinta coluna (índice 4)
                }
                updatedData.add(row);
            }
        }

        // Atualizar a planilha com os dados modificados
        ValueRange body = new ValueRange().setValues(updatedData);
        UpdateValuesResponse result = service.spreadsheets().values()
                .update(SPREADSHEET_ID, SHEET_NAME, body)
                .setValueInputOption("RAW")
                .execute();
    }

    // Função para excluir uma tarefa pelo ID
    public static void excluirTarefa(String tarefaId) throws IOException, GeneralSecurityException {
        Sheets service = SheetsServiceUtil.getSheetsService();
        String range = SHEET_NAME + "!A:F"; // Colunas da planilha (A a F)

        // Obter todas as tarefas
        ValueRange response = service.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute();
        List<List<Object>> values = response.getValues();

        // Identificar o índice da linha a ser excluída
        int rowIndexToDelete = -1;
        if (values != null) {
            for (int i = 0; i < values.size(); i++) {
                List<Object> row = values.get(i);
                if (row.size() > 0 && tarefaId.equals(row.get(0).toString())) {
                    rowIndexToDelete = i;
                    break;
                }
            }
        }

        if (rowIndexToDelete != -1) {
            // Remover a linha
            List<Request> requests = new ArrayList<>();
            requests.add(new Request()
                    .setDeleteDimension(new DeleteDimensionRequest()
                            .setRange(new DimensionRange()
                                    .setSheetId(getSheetId(service, SHEET_NAME))
                                    .setDimension("ROWS")
                                    .setStartIndex(rowIndexToDelete)
                                    .setEndIndex(rowIndexToDelete + 1))));

            BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest().setRequests(requests);
            service.spreadsheets().batchUpdate(SPREADSHEET_ID, body).execute();
        }
    }

    // Função auxiliar para obter o SheetId a partir do nome da aba
    private static int getSheetId(Sheets service, String sheetName) throws IOException {
        Spreadsheet spreadsheet = service.spreadsheets().get(SPREADSHEET_ID).execute();
        for (Sheet sheet : spreadsheet.getSheets()) {
            if (sheet.getProperties().getTitle().equals(sheetName)) {
                return sheet.getProperties().getSheetId();
            }
        }
        throw new IOException("Sheet with name '" + sheetName + "' not found");
    }
}
