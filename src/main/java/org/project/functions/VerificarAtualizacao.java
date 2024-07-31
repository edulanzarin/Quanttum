package org.project.functions;

import org.project.view.VerificarAtualizacaoWindow;
import org.project.view.LoginWindow;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import javafx.application.Application;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class VerificarAtualizacao {

    private static final String SPREADSHEET_ID = "1G39rq0NGIMJ4LFHQ7-3unHAD39C_aZGAIZx1L3d7cD8";
    private static final String RANGE = "version!A2:C2";

    public static void verificarAtualizacao() {
        try {
            // Acessar a planilha do Google Sheets
            Sheets sheetsService = SheetsServiceUtil.getSheetsService();
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(SPREADSHEET_ID, RANGE)
                    .execute();
            List<List<Object>> values = response.getValues();

            if (values == null || values.isEmpty()) {
                System.out.println("Nenhuma versão encontrada na planilha.");
                return;
            }

            String actualVersion = (String) values.get(0).get(0);
            String newVersion = (String) values.get(0).get(1);
            String viewLink = (String) values.get(0).get(2);

            // Gerar o link de download direto
            String downloadLink = convertToDirectDownloadLink(viewLink);

            // Comparar as versões
            if (newVersion.compareTo(actualVersion) > 0) {
                // Passar informações para a janela de atualização
                VerificarAtualizacaoWindow.setVersions(actualVersion, newVersion, downloadLink);
                Application.launch(VerificarAtualizacaoWindow.class);
            } else {
                // Abrir a janela de login
                Application.launch(LoginWindow.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Converter o link do Google Drive para um link de download direto
    private static String convertToDirectDownloadLink(String viewLink) {
        // Extrair o ID do arquivo
        String fileId = viewLink.split("/d/")[1].split("/")[0];
        return "https://drive.google.com/uc?export=download&id=" + fileId;
    }

    // Função para baixar o arquivo
    public static void downloadFile(String fileURL, String saveDir) {
        try (InputStream inputStream = new URL(fileURL).openStream();
             FileOutputStream outputStream = new FileOutputStream(new File(saveDir))) {

            HttpURLConnection httpConn = (HttpURLConnection) new URL(fileURL).openConnection();
            int responseCode = httpConn.getResponseCode();

            // Verifica o código de resposta HTTP
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Define o buffer para leitura do arquivo
                byte[] buffer = new byte[4096];
                int bytesRead;

                // Lê e grava o arquivo
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                System.out.println("Download concluído!");
            } else {
                System.out.println("Nenhum arquivo encontrado. Código de resposta: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
