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
                    .setValues(Collections.singletonList(Collections.singletonList(novoValor)));
            Sheets.Spreadsheets.Values.Update request = sheetsService.spreadsheets().values()
                    .update(spreadsheetId, range, body);
            request.setValueInputOption("RAW");

            request.execute();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    public void cadastrarNatureza(String codigo, String natureza, String conta) {
        try {
            // Verificar se a natureza já existe para o código da empresa
            if (naturezaExiste(codigo, natureza, conta)) {
                return; // Não prossegue se a natureza já existe
            }

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
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
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

    private static void duplicarNaturezas(GerenciarNaturezas gerenciarNaturezas, String codigoOriginal, String novoCodigo) {
        // 1. Recupera todas as naturezas da empresa original
        List<NaturezaConta> naturezasOriginal = gerenciarNaturezas.verificarCodigoECarregarNaturezas(codigoOriginal);

        if (naturezasOriginal != null) {
            // 2. Recupera todas as naturezas da nova empresa para comparação
            List<NaturezaConta> naturezasNovaEmpresa = gerenciarNaturezas.verificarCodigoECarregarNaturezas(novoCodigo);

            // Cria um conjunto para verificar rapidamente a existência
            Set<String> naturezasExistentes = new HashSet<>();
            if (naturezasNovaEmpresa != null) {
                for (NaturezaConta natureza : naturezasNovaEmpresa) {
                    naturezasExistentes.add(natureza.getNatureza() + "|" + natureza.getConta());
                }
            }

            // 3. Para cada natureza da empresa original
            for (NaturezaConta naturezaOriginal : naturezasOriginal) {
                String chaveNatureza = naturezaOriginal.getNatureza() + "|" + naturezaOriginal.getConta();

                // 4. Verifica se a combinação já existe na nova empresa
                if (!naturezasExistentes.contains(chaveNatureza)) {
                    // Adiciona a natureza na nova empresa
                    gerenciarNaturezas.cadastrarNatureza(novoCodigo, naturezaOriginal.getNatureza(), naturezaOriginal.getConta());
                }
            }
        }
    }

    public boolean empresaExiste(String codigo) throws IOException {
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

    public boolean naturezaExiste(String codigo, String natureza, String conta) throws IOException {
        List<NaturezaConta> naturezas = carregarNaturezas(codigo);
        if (naturezas != null) {
            for (NaturezaConta nc : naturezas) {
                if (nc.getNatureza().equals(natureza) && nc.getConta().equals(conta)) {
                    return true;
                }
            }
        }
        return false;
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
