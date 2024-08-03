package org.project.functions;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import org.project.view.contents.Tarefa;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.time.format.DateTimeParseException;


public class MeuCronograma {

    private static final String SPREADSHEET_ID = "1G39rq0NGIMJ4LFHQ7-3unHAD39C_aZGAIZx1L3d7cD8"; // Substitua pelo ID da sua planilha
    private static final String SHEET_NAME = "tarefas"; // Nome da aba da planilha onde você quer adicionar a tarefa

    // Função para adicionar uma nova tarefa
// Função para adicionar uma nova tarefa
    public static void addTarefa(String titulo, String descricao, LocalDate data, String usuarioId) throws IOException, GeneralSecurityException {
        Sheets service = SheetsServiceUtil.getSheetsService();

        // Obtém o último ID para auto-incremento
        int lastId = getLastId(service);
        int newId = lastId + 1;

        // Cria a nova linha de dados
        List<Object> rowData = new ArrayList<>();
        rowData.add(String.valueOf(newId));  // ID
        rowData.add(titulo);                 // Título
        rowData.add(descricao);              // Descrição
        rowData.add(data != null ? data.toString() : ""); // Dia
        rowData.add("Pendente");               // Status (definido como "Aberta")
        rowData.add(usuarioId);              // Usuário

        // Adiciona a nova linha à planilha
        List<List<Object>> dataList = new ArrayList<>();
        dataList.add(rowData);

        ValueRange body = new ValueRange().setValues(dataList);
        AppendValuesResponse result = service.spreadsheets().values()
                .append(SPREADSHEET_ID, SHEET_NAME, body)
                .setValueInputOption("RAW")
                .execute();
    }

    // Função para obter o último ID da planilha
    private static int getLastId(Sheets service) throws IOException {
        String range = SHEET_NAME + "!A:A"; // Coluna de IDs
        ValueRange response = service.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute();
        List<List<Object>> values = response.getValues();

        // O último ID é o maior número na coluna A
        int lastId = 0;
        if (values != null && !values.isEmpty()) {
            for (List<Object> row : values) {
                try {
                    int id = Integer.parseInt(row.get(0).toString());
                    if (id > lastId) {
                        lastId = id;
                    }
                } catch (NumberFormatException e) {
                    // Ignora valores não numéricos
                }
            }
        }
        return lastId;
    }

    // Função para obter as tarefas por usuário
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

    // Atualiza a data de uma tarefa existente
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
                    row.set(4, "Pendente"); // Define o status como "Aberta" na quinta coluna (índice 4)
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

    public static void resetarTarefas(String userId) throws IOException, GeneralSecurityException {
        Sheets service = SheetsServiceUtil.getSheetsService();
        String range = SHEET_NAME + "!A:F"; // Colunas da planilha (A a F)

        // Obtém todas as tarefas
        ValueRange response = service.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute();
        List<List<Object>> values = response.getValues();

        // Verifica se há tarefas a serem atualizadas
        if (values != null && !values.isEmpty()) {
            List<ValueRange> data = new ArrayList<>();
            for (List<Object> row : values) {
                // Verifica se a tarefa pertence ao usuário
                if (row.size() >= 6 && row.get(5).equals(userId)) {
                    row.set(4, "Pendente"); // Define o status da tarefa como "Aberta"
                    String updateRange = SHEET_NAME + "!A" + (values.indexOf(row) + 1) + ":F" + (values.indexOf(row) + 1);
                    data.add(new ValueRange().setRange(updateRange).setValues(Collections.singletonList(row)));
                }
            }

            // Atualiza todas as tarefas na planilha
            BatchUpdateValuesRequest body = new BatchUpdateValuesRequest().setValueInputOption("RAW").setData(data);
            service.spreadsheets().values().batchUpdate(SPREADSHEET_ID, body).execute();
        }
    }
}

