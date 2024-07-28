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
                    numRenamedFiles++;
                }
            }
        }

        // Registra log no Google Sheets
        try {
            String username = GetUsername.getUsernameById(userId);
            String action = "renomear-das";
            new RegistrarLog().logAction(username, action);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return numRenamedFiles;
    }

    private String readPDF(File file) {
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);
            List<String> lines = Arrays.asList(text.split("\\r?\\n"));

            String newFileName = null;

            if (lines.size() >= 3) {
                if (lines.get(2).contains("/")) {
                    List<String> secondLineParts = Arrays.asList(lines.get(2).split("\\s+")).subList(1, lines.get(2).split("\\s+").length);
                    newFileName = String.join(" ", secondLineParts) + ".pdf";
                } else if (lines.size() >= 4) {
                    List<String> thirdLineParts = Arrays.asList(lines.get(3).split("\\s+")).subList(1, lines.get(3).split("\\s+").length);
                    newFileName = String.join(" ", thirdLineParts) + ".pdf";
                }

                if (newFileName != null) {
                    File newFile = new File(file.getParent(), newFileName);
                    newFile = getUniqueFile(newFile);

                    if (file.renameTo(newFile)) {
                        return newFileName;
                    }
                }
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
