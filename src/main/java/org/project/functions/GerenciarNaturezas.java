package org.project.functions;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.text.Collator;
import java.util.*;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.json.JSONArray;
import org.json.JSONObject;

public class GerenciarNaturezas {

    public static final String SHEET_ID = "1G39rq0NGIMJ4LFHQ7-3unHAD39C_aZGAIZx1L3d7cD8";
    private static final String API_KEY = "AIzaSyAqpPB5ax6k0ERjfQz5qJYGuv4dkCa-N9o";
    private static final String EMPRESAS_RANGE = "empresas!A2:B";
    private static final String NATUREZAS_RANGE = "naturezas!A2:D";

    public List<NaturezaConta> verificarCodigoECarregarNaturezas(String codigo) {
        try {
            if (!codigoExiste(codigo)) {
                return null; // Código não encontrado
            }

            // Carregar naturezas e contas da sheet "naturezas"
            List<NaturezaConta> naturezas = carregarNaturezas(codigo);
            if (naturezas != null) {
                naturezas.sort(Comparator.comparing(NaturezaConta::getNatureza, Collator.getInstance(Locale.getDefault())));
            }
            return naturezas;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean codigoExiste(String codigo) throws IOException {
        URL url = new URL("https://sheets.googleapis.com/v4/spreadsheets/" + SHEET_ID + "/values/" + EMPRESAS_RANGE + "?key=" + API_KEY);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            Scanner scanner = new Scanner(conn.getInputStream());
            StringBuilder response = new StringBuilder();
            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }
            scanner.close();

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray values = jsonResponse.optJSONArray("values");
            if (values != null) {
                for (int i = 0; i < values.length(); i++) {
                    JSONArray row = values.getJSONArray(i);
                    if (row.length() > 0 && row.getString(0).equals(codigo)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private List<NaturezaConta> carregarNaturezas(String codigo) throws IOException {
        URL url = new URL("https://sheets.googleapis.com/v4/spreadsheets/" + SHEET_ID + "/values/" + NATUREZAS_RANGE + "?key=" + API_KEY);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        int responseCode = conn.getResponseCode();
        List<NaturezaConta> naturezas = new ArrayList<>();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            Scanner scanner = new Scanner(conn.getInputStream());
            StringBuilder response = new StringBuilder();
            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }
            scanner.close();

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray values = jsonResponse.optJSONArray("values");
            if (values != null) {
                for (int i = 0; i < values.length(); i++) {
                    JSONArray row = values.getJSONArray(i);
                    if (row.length() > 3 && row.getString(3).equals(codigo)) {
                        int id = Integer.parseInt(row.getString(0)); // ID está na primeira coluna
                        String natureza = row.getString(1);
                        String conta = row.getString(2);
                        String codigoNatureza = row.getString(3);
                        naturezas.add(new NaturezaConta(id, natureza, conta, codigoNatureza));
                    }
                }
            }
        }
        return naturezas;
    }

    public void atualizarConta(String spreadsheetId, String range, String novoValor) {
        try {
            Sheets sheetsService = SheetsServiceUtil.getSheetsService();

            ValueRange body = new ValueRange()
                    .setValues(Arrays.asList(
                            Arrays.asList(novoValor)
                    ));
            Sheets.Spreadsheets.Values.Update request = sheetsService.spreadsheets().values()
                    .update(spreadsheetId, range, body);
            request.setValueInputOption("RAW");

            request.execute();
            System.out.println("Conta atualizada com sucesso!");
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            System.out.println("Não foi possível atualizar a conta");
        }
    }

    public void cadastrarNatureza(String codigo, String natureza, String conta) {
        try {
            Sheets sheetsService = SheetsServiceUtil.getSheetsService();

            // 1. Encontrar o maior ID existente
            int maxId = encontrarMaiorId(sheetsService);

            // 2. Adicionar o novo registro com o ID incrementado
            int novoId = maxId + 1;
            ValueRange body = new ValueRange()
                    .setValues(Arrays.asList(
                            Arrays.asList(novoId, natureza, conta, codigo)
                    ));
            Sheets.Spreadsheets.Values.Append request = sheetsService.spreadsheets().values()
                    .append(SHEET_ID, NATUREZAS_RANGE, body);
            request.setValueInputOption("RAW");
            request.setInsertDataOption("INSERT_ROWS");

            request.execute();
            System.out.println("Natureza cadastrada com sucesso!");
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            System.out.println("Não foi possível cadastrar a natureza");
        }
    }

    private int encontrarMaiorId(Sheets sheetsService) throws IOException {
        // Ler os dados da planilha para encontrar o maior ID
        URL url = new URL("https://sheets.googleapis.com/v4/spreadsheets/" + SHEET_ID + "/values/" + NATUREZAS_RANGE + "?key=" + API_KEY);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        int responseCode = conn.getResponseCode();
        int maxId = 0;
        if (responseCode == HttpURLConnection.HTTP_OK) {
            Scanner scanner = new Scanner(conn.getInputStream());
            StringBuilder response = new StringBuilder();
            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }
            scanner.close();

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray values = jsonResponse.optJSONArray("values");
            if (values != null) {
                for (int i = 0; i < values.length(); i++) {
                    JSONArray row = values.getJSONArray(i);
                    if (row.length() > 0) {
                        int id = Integer.parseInt(row.getString(0));
                        if (id > maxId) {
                            maxId = id;
                        }
                    }
                }
            }
        }
        return maxId > 0 ? maxId : 1; // Retorna 1 se nenhum ID for encontrado
    }

    public static class NaturezaConta {
        private int id;
        private String natureza;
        private String conta;

        public NaturezaConta(int id, String natureza, String conta, String codigo) {
            this.id = id;
            this.natureza = natureza;
            this.conta = conta;
        }

        public int getId() {
            return id;
        }

        public String getNatureza() {
            return natureza;
        }

        public void setNatureza(String natureza) {
            this.natureza = natureza;
        }

        public String getConta() {
            return conta;
        }

        public void setConta(String conta) {
            this.conta = conta;
        }
    }
}
