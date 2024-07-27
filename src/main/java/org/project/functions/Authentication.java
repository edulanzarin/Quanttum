package org.project.functions;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A classe Authentication é responsável por verificar a validade de um usuário
 * consultando uma planilha do Google Sheets.
 *
 * Esta classe utiliza a API do Google Sheets para obter os dados e validar se o usuário
 * fornecido está ativo.
 */
public class Authentication {

    // URL para acessar os dados da planilha do Google Sheets
    private static final String SHEET_URL = "https://sheets.googleapis.com/v4/spreadsheets/1G39rq0NGIMJ4LFHQ7-3unHAD39C_aZGAIZx1L3d7cD8/values/Sheet1?key=AIzaSyACkvpC78bwzQgV7bGC6auf_eR0TjHWhrY";

    /**
     * Verifica se o usuário fornecido está ativo consultando a planilha do Google Sheets.
     *
     * @param user O nome do usuário a ser verificado.
     * @return true se o usuário estiver ativo, caso contrário, false.
     */
    public boolean isUserActive(String user) {
        try {
            // Cria uma URL para acessar a planilha
            URL url = new URL(SHEET_URL);
            // Abre uma conexão HTTP com a URL
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET"); // Define o método de requisição como GET

            // Obtém o código de resposta da conexão
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Se a resposta for OK, lê a resposta da conexão
                Scanner scanner = new Scanner(url.openStream());
                StringBuilder response = new StringBuilder();
                while (scanner.hasNext()) {
                    response.append(scanner.nextLine());
                }
                scanner.close();

                // Analisa a resposta JSON
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray values = jsonResponse.getJSONArray("values");

                // Itera sobre as linhas da planilha, começando do índice 1 para pular o cabeçalho
                for (int i = 1; i < values.length(); i++) {
                    JSONArray row = values.getJSONArray(i);
                    String userFromSheet = row.getString(0); // Obtém o nome do usuário da planilha
                    String statusFromSheet = row.getString(1); // Obtém o status do usuário da planilha

                    // Verifica se o usuário fornecido está ativo
                    if (userFromSheet.equals(user) && statusFromSheet.equalsIgnoreCase("ativo")) {
                        return true; // Usuário encontrado e ativo
                    }
                }
            }
        } catch (IOException e) {
            // Captura e imprime exceções de entrada/saída
            e.printStackTrace();
        }
        return false; // Usuário não encontrado ou não está ativo
    }
}
