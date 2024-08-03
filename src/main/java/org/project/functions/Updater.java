package org.project.functions;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Updater {
    private static final String VERSION_URL = "https://seu_repositorio/version.txt";
    private static final String DOWNLOAD_URL = "https://seu_repositorio/update.zip";
    private static final String CURRENT_VERSION = "1.0.0"; // Versão atual do seu aplicativo
    private static final String INSTALL_DIR = "path/para/diretorio/instalacao";

    public static void main(String[] args) {
        try {
            // Verificar versão atual
            String latestVersion = new String(Files.readAllBytes(Paths.get(VERSION_URL))).trim();
            if (!CURRENT_VERSION.equals(latestVersion)) {
                System.out.println("Atualização disponível: " + latestVersion);
                downloadUpdate(DOWNLOAD_URL, "update.zip");
                unzip("update.zip", INSTALL_DIR);
                System.out.println("Atualização aplicada. Reiniciando o aplicativo...");
                restartApplication();
            } else {
                System.out.println("Seu aplicativo já está na versão mais recente.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void downloadUpdate(String downloadUrl, String outputFileName) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(downloadUrl).openConnection();
        connection.setRequestMethod("GET");

        try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
             FileOutputStream fileOutputStream = new FileOutputStream(outputFileName)) {
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        }
    }

    private static void unzip(String zipFilePath, String destDir) throws IOException {
        try (ZipInputStream zipIn = new ZipInputStream(Files.newInputStream(Paths.get(zipFilePath)))) {
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                Path filePath = Paths.get(destDir, entry.getName());
                if (!entry.isDirectory()) {
                    Files.createDirectories(filePath.getParent());
                    try (BufferedInputStream bis = new BufferedInputStream(zipIn);
                         FileOutputStream fos = new FileOutputStream(filePath.toString())) {
                        byte[] buffer = new byte[1024];
                        int read;
                        while ((read = bis.read(buffer)) != -1) {
                            fos.write(buffer, 0, read);
                        }
                    }
                } else {
                    Files.createDirectories(filePath);
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }
    }

    private static void restartApplication() {
        try {
            // Caminho para o executável principal do aplicativo
            String command = "path/para/aplicativo.exe";
            Runtime.getRuntime().exec(command);
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
