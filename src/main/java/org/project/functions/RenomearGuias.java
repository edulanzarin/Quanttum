package org.project.functions;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class RenomearGuias {

    public int processPDFsInFolder(String folderPath, String userId) {
        File folder = new File(folderPath);
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("O caminho informado não é um diretório.");
        }

        int numRenamedFiles = 0;
        for (File file : folder.listFiles()) {
            if (file.isFile() && file.getName().toLowerCase().endsWith(".pdf")) {
                String newFileName = readPDF(file);
                if (newFileName != null) {
                    File newFile = new File(file.getParent(), newFileName);
                    newFile = getUniqueFile(newFile);

                    if (file.renameTo(newFile)) {
                        numRenamedFiles++;
                    }
                }
            }
        }

        // Registra log no Google Sheets
        try {
            String username = GetUsername.getUsernameById(userId);
            String action = "rename-pdf-files";
            RegistrarLog.logAction(username, action);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return numRenamedFiles;
    }

    private String readPDF(File file) {
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);
            String[] lines = text.split("\\r?\\n");

            if (lines.length >= 3) {
                String thirdLine = lines[2];
                if (thirdLine.contains("/")) {
                    List<String> parts = Arrays.asList(thirdLine.split("\\s+"));
                    return String.join("_", parts.subList(1, parts.size())) + ".pdf";
                }
            } else {
                System.out.println("O arquivo PDF não possui pelo menos 3 linhas.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private File getUniqueFile(File file) {
        String fileName = file.getName();
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
        String extension = fileName.substring(fileName.lastIndexOf('.'));
        int count = 1;

        while (file.exists()) {
            file = new File(file.getParent(), baseName + " (" + count + ")" + extension);
            count++;
        }

        return file;
    }
}
