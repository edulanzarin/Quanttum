package org.project.functions;

import javafx.application.Application;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.json.JSONObject;
import org.project.view.LoginWindow;
import org.project.view.VerificarAtualizacaoWindow;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.*;
import java.util.List;

public class VerificarAtualizacao {

    private static final String SPREADSHEET_ID = "1G39rq0NGIMJ4LFHQ7-3unHAD39C_aZGAIZx1L3d7cD8";
    private static final String RANGE = "version!A2:B2";
    private static final String VERSION_FILE_PATH = "/org/project/json/version.json";

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

            String newVersion = (String) values.get(0).get(0);
            String downloadLink = (String) values.get(0).get(1); // Link direto do GitHub

            // Ler a versão atual do arquivo JSON
            String actualVersion = readVersionFromFile();

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

    private static String readVersionFromFile() {
        try (InputStream inputStream = VerificarAtualizacao.class.getResourceAsStream(VERSION_FILE_PATH)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Arquivo não encontrado: " + VERSION_FILE_PATH);
            }
            String content = new String(inputStream.readAllBytes());
            JSONObject json = new JSONObject(content);
            return json.getString("version");
        } catch (IOException e) {
            e.printStackTrace();
            return "0.0.0"; // Valor padrão em caso de erro
        }
    }

    // Função para baixar o arquivo
    public static void downloadFile(String fileURL, String saveDir, ProgressListener listener) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            File saveFile = new File(saveDir);
            if (!saveFile.getParentFile().exists()) {
                saveFile.getParentFile().mkdirs();
            }

            System.out.println("Iniciando download do arquivo: " + fileURL);

            HttpGet request = new HttpGet(fileURL);
            request.setHeader("User-Agent", "Mozilla/5.0");

            try (var response = httpclient.execute(request)) {
                int status = response.getCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        long contentLength = entity.getContentLength();
                        try (InputStream inputStream = entity.getContent();
                             FileOutputStream outputStream = new FileOutputStream(saveFile)) {
                            byte[] buffer = new byte[8192];
                            long totalBytesRead = 0;
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                                totalBytesRead += bytesRead;
                                double progress = (double) totalBytesRead / contentLength;
                                listener.onProgress(progress);
                            }
                            // Ensure the output stream is fully flushed and closed
                            outputStream.flush();
                            EntityUtils.consume(entity);
                            System.out.println("Download concluído!");
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.out.println("Erro ao processar o conteúdo do arquivo.");
                        }
                    } else {
                        throw new IOException("Entidade HTTP nula.");
                    }
                } else {
                    throw new IOException("Status de resposta inesperado: " + status);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Erro ao executar a requisição HTTP.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erro ao criar cliente HTTP.");
        }
    }

    // Interface para atualizar o progresso
    public interface ProgressListener {
        void onProgress(double progress);
    }
}
