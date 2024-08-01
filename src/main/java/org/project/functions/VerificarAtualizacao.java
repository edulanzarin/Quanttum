package org.project.functions;

import org.project.view.VerificarAtualizacaoWindow;
import org.project.view.LoginWindow;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import javafx.application.Application;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
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
            String viewLink = (String) values.get(0).get(1);

            // Gerar o link de download direto
            String downloadLink = convertToDirectDownloadLink(viewLink);

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

    public static void writeVersionToFile(String newVersion) {
        try {
            JSONObject json = new JSONObject();
            json.put("version", newVersion);

            // O caminho para o recurso não pode ser usado para escrita; salvar fora do JAR
            File versionFile = new File(System.getProperty("user.home") + File.separator + "Downloads" + File.separator + "version.json");
            Files.write(versionFile.toPath(), json.toString(4).getBytes());
        } catch (IOException e) {
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
        try {
            // Cria o diretório se não existir
            File saveFile = new File(saveDir);
            if (!saveFile.getParentFile().exists()) {
                saveFile.getParentFile().mkdirs();
            }

            // Cria a conexão e baixa o arquivo
            HttpURLConnection httpConn = (HttpURLConnection) new URL(fileURL).openConnection();
            int responseCode = httpConn.getResponseCode();

            // Verifica o código de resposta HTTP
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream inputStream = httpConn.getInputStream();
                     FileOutputStream outputStream = new FileOutputStream(saveFile)) {

                    // Define o buffer para leitura do arquivo
                    byte[] buffer = new byte[4096];
                    int bytesRead;

                    // Lê e grava o arquivo
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    System.out.println("Download concluído!");

                    // Atualizar a versão no arquivo JSON
                    writeVersionToFile(readVersionFromFile());

                    // Reiniciar a aplicação
                    restartApplication();
                }
            } else {
                System.out.println("Nenhum arquivo encontrado. Código de resposta: " + responseCode);
            }
            httpConn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void replaceCurrentJar(File source, File target) throws IOException {
        Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    public static void restartApplication() throws IOException {
        String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        File currentJar = new File(VerificarAtualizacao.class.getProtectionDomain().getCodeSource().getLocation().getPath());

        /* is it a jar file? */
        if (!currentJar.getName().endsWith(".jar"))
            return;

        /* Build command: java -jar application.jar */
        final ArrayList<String> command = new ArrayList<>();
        command.add(javaBin);
        command.add("-jar");
        command.add(currentJar.getPath());

        final ProcessBuilder builder = new ProcessBuilder(command);
        builder.start();
        System.exit(0);
    }
}
