package org.project.functions;

import org.project.view.LoginWindow;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Properties;

public class AtualizarVersao {

    private String downloadUrl = ""; // URL de download direto
    private String savePath = ""; // Caminho para salvar o arquivo
    private String configFilePath = ""; // Caminho do arquivo config.properties
    private String remoteVersion; // Versão remota obtida da planilha
    private String localVersion; // Versão local obtida do arquivo config.properties

    public AtualizarVersao(String configFilePath) {
        this.configFilePath = configFilePath;
        this.localVersion = getLocalVersion();
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public void setRemoteVersion(String remoteVersion) {
        this.remoteVersion = remoteVersion;
    }

    public String getRemoteVersion() {
        return remoteVersion;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public boolean isUpdateAvailable(String remoteVersion, String localVersion) {
        return !remoteVersion.equals(localVersion);
    }

    public void iniciarAtualizacao(ProgressBar progressBar, Button downloadButton, Label messageLabel, Stage primaryStage) {
        Task<Void> downloadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Verifica a versão local
                String localVersion = getLocalVersion();

                // Compara com a versão remota
                if (localVersion != null && remoteVersion != null && isUpdateAvailable(localVersion, remoteVersion)) {
                    URL url = new URL(downloadUrl); // Use a URL obtida da planilha
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    int fileSize = connection.getContentLength();

                    // Imprime o nome do arquivo e o tamanho
                    System.out.println("Download URL: " + downloadUrl);
                    System.out.println("Saving to: " + savePath);
                    System.out.println("File size: " + fileSize + " bytes");

                    try (InputStream input = new BufferedInputStream(connection.getInputStream());
                         OutputStream output = new BufferedOutputStream(new FileOutputStream(savePath))) { // Usa o caminho de salvamento definido

                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        long totalBytesRead = 0;

                        while ((bytesRead = input.read(buffer)) != -1) {
                            output.write(buffer, 0, bytesRead);
                            totalBytesRead += bytesRead;
                            final double progress = (double) totalBytesRead / fileSize;
                            Platform.runLater(() -> progressBar.setProgress(progress));
                        }

                        // Atualiza o arquivo config.properties
                        updateConfigFile(remoteVersion);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Platform.runLater(() -> {
                        messageLabel.setText("Nenhuma atualização disponível.");
                        primaryStage.close();
                        try {
                            new LoginWindow().start(new Stage()); // Certifique-se de que LoginWindow é uma classe existente
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                downloadButton.setText("Download Concluído");
                downloadButton.setDisable(true);
                Platform.runLater(() -> {
                    // Aqui você deve chamar o método para abrir a LoginWindow
                    try {
                        new LoginWindow().start(new Stage()); // Certifique-se de que LoginWindow é uma classe existente
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            protected void failed() {
                super.failed();
                downloadButton.setText("Falha no Download");
                downloadButton.setDisable(false);
                progressBar.setVisible(false);
            }

            @Override
            protected void cancelled() {
                super.cancelled();
                downloadButton.setText("Download Cancelado");
                downloadButton.setDisable(false);
                progressBar.setVisible(false);
            }
        };

        new Thread(downloadTask).start();
    }

    public String getLocalVersion() {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(configFilePath)) {
            properties.load(input);
            return properties.getProperty("version");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void updateConfigFile(String newVersion) {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(configFilePath)) {
            properties.load(input);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (OutputStream output = new FileOutputStream(configFilePath)) {
            properties.setProperty("version", newVersion);
            properties.store(output, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fetchUpdateInfoFromSheet(Runnable onComplete) {
        new Thread(() -> {
            try {
                // Cria o serviço Sheets
                Sheets service = SheetsServiceUtil.getSheetsService();

                // Define o ID da planilha e o intervalo que você deseja ler
                String spreadsheetId = "1G39rq0NGIMJ4LFHQ7-3unHAD39C_aZGAIZx1L3d7cD8"; // Substitua pelo ID da sua planilha
                String range = "version!A1"; // Define o intervalo para ler a versão

                // Lê os dados da planilha
                ValueRange response = service.spreadsheets().values()
                        .get(spreadsheetId, range)
                        .execute();
                List<List<Object>> values = response.getValues();

                // Verifica se há dados e atualiza as variáveis de versão e URL
                if (values != null && !values.isEmpty()) {
                    // Supondo que a versão está na primeira célula
                    remoteVersion = (String) values.get(0).get(1);

                    // Imprime a versão obtida da planilha
                    System.out.println("Versão remota obtida da planilha: " + remoteVersion);

                    range = "version!B1";
                    ValueRange responseUrl = service.spreadsheets().values()
                            .get(spreadsheetId, range)
                            .execute();
                    List<List<Object>> valuesUrl = responseUrl.getValues();
                    if (valuesUrl != null && !valuesUrl.isEmpty()) {
                        downloadUrl = (String) valuesUrl.get(1).get(1);
                        System.out.println("URL de download obtida da planilha: " + downloadUrl);
                    }
                } else {
                    throw new IOException("Nenhuma versão encontrada na planilha.");
                }

                // Executa a tarefa após obter as informações de atualização
                Platform.runLater(onComplete);

            } catch (GeneralSecurityException | IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Erro ao acessar a planilha do Google Sheets.", e);
            }
        }).start();
    }
}
