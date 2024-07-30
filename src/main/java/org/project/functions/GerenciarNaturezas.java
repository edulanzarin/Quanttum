package org.project.functions;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;

public class GerenciarNaturezas {

    private static final String SHEET_ID = "1G39rq0NGIMJ4LFHQ7-3unHAD39C_aZGAIZx1L3d7cD8";
    private static final String API_KEY = "AIzaSyAqpPB5ax6k0ERjfQz5qJYGuv4dkCa-N9o";
    private static final String NATUREZAS_RANGE = "naturezas!A2:D";

    public List<NaturezaConta> verificarCodigoECarregarNaturezas(String codigo) {
        try {
            if (!codigoExiste(codigo)) {
                return null; // Código não encontrado
            }

            // Carregar naturezas e contas da sheet "naturezas"
            List<NaturezaConta> naturezas = carregarNaturezas(codigo);
            if (naturezas != null) {
                naturezas.sort((o1, o2) -> o1.getNatureza().compareTo(o2.getNatureza()));
            }
            return naturezas;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean codigoExiste(String codigo) throws IOException {
        URL url = new URL("https://sheets.googleapis.com/v4/spreadsheets/" + SHEET_ID + "/values/empresas!A2:B?key=" + API_KEY);
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
            JSONArray values = jsonResponse.getJSONArray("values");

            for (int i = 0; i < values.length(); i++) {
                JSONArray row = values.getJSONArray(i);
                if (row.length() > 0 && row.getString(0).equals(codigo)) {
                    return true;
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
            JSONArray values = jsonResponse.getJSONArray("values");

            for (int i = 0; i < values.length(); i++) {
                JSONArray row = values.getJSONArray(i);
                if (row.length() > 3 && row.getString(3).equals(codigo)) {
                    naturezas.add(new NaturezaConta(row.getString(0), row.getString(1), row.getString(2)));
                }
            }
        }
        return naturezas;
    }

    public void marcarNaturezaComoInativa(String id) throws IOException {
        List<NaturezaConta> naturezas = carregarNaturezas(""); // Carregar todas as naturezas
        int rowIndex = -1;
        for (int i = 0; i < naturezas.size(); i++) {
            if (naturezas.get(i).getId().equals(id)) {
                rowIndex = i + 2; // +2 para ajustar o índice da planilha (começa na linha 2)
                break;
            }
        }

        if (rowIndex != -1) {
            // Atualizar o valor da coluna D para "0"
            URL url = new URL("https://sheets.googleapis.com/v4/spreadsheets/" + SHEET_ID + "/values/naturezas!D" + rowIndex + "?valueInputOption=RAW&key=" + API_KEY);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setDoOutput(true);

            String jsonInputString = "{\"values\": [[\"0\"]]}";
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("Erro ao marcar natureza como inativa: " + responseCode);
            }
        } else {
            throw new IOException("Natureza não encontrada para atualização");
        }
    }

    public void atualizarNatureza(String id, String novaConta) throws IOException {
        // Carregar todas as naturezas para encontrar o índice da linha correta
        List<NaturezaConta> naturezas = carregarNaturezas(""); // Carregar todas as naturezas
        int rowIndex = -1;
        for (int i = 0; i < naturezas.size(); i++) {
            if (naturezas.get(i).getId().equals(id)) {
                rowIndex = i + 2; // +2 para ajustar o índice da planilha (começa na linha 2)
                break;
            }
        }

        if (rowIndex != -1) {
            // Atualizar o valor da coluna C para a nova conta
            URL url = new URL("https://sheets.googleapis.com/v4/spreadsheets/" + SHEET_ID + "/values/naturezas!C" + rowIndex + "?valueInputOption=RAW&key=" + API_KEY);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setDoOutput(true);

            String jsonInputString = "{\"values\": [[\"" + novaConta + "\"]]}";
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("Erro ao atualizar natureza: " + responseCode);
            }
        } else {
            throw new IOException("Natureza não encontrada para atualização");
        }
    }


    public String obterNaturezaPorId(String id) throws IOException {
        URL url = new URL("https://sheets.googleapis.com/v4/spreadsheets/" + SHEET_ID + "/values/" + NATUREZAS_RANGE + "?key=" + API_KEY);
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
            JSONArray values = jsonResponse.getJSONArray("values");

            for (int i = 0; i < values.length(); i++) {
                JSONArray row = values.getJSONArray(i);
                if (row.length() > 0 && row.getString(0).equals(id)) {
                    return row.getString(1); // Retorna o valor da coluna B (natureza)
                }
            }
        }
        throw new IOException("ID não encontrado: " + id);
    }

    public static class NaturezaConta {
        private final String id;
        private final String natureza;
        private final String conta;

        public NaturezaConta(String id, String natureza, String conta) {
            this.id = id;
            this.natureza = natureza;
            this.conta = conta;
        }

        public String getId() {
            return id;
        }

        public String getNatureza() {
            return natureza;
        }

        public String getConta() {
            return conta;
        }
    }
}
