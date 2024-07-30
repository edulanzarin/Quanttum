package org.project.functions;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;

public class GerenciarNaturezas {

    private static final String SHEET_ID = "1G39rq0NGIMJ4LFHQ7-3unHAD39C_aZGAIZx1L3d7cD8";
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
            naturezas.sort(new Comparator<NaturezaConta>() {
                @Override
                public int compare(NaturezaConta o1, NaturezaConta o2) {
                    Collator collator = Collator.getInstance(Locale.getDefault());
                    return collator.compare(o1.getNatureza(), o2.getNatureza());
                }
            });
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
                    naturezas.add(new NaturezaConta(row.getString(1), row.getString(2)));
                }
            }
        }
        return naturezas;
    }

    public static class NaturezaConta {
        private final String natureza;
        private final String conta;

        public NaturezaConta(String natureza, String conta) {
            this.natureza = natureza;
            this.conta = conta;
        }

        public String getNatureza() {
            return natureza;
        }

        public String getConta() {
            return conta;
        }
    }
}
