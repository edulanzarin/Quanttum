package org.project;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class LerPDF {

    public static void printPDFLines(String filePath) {
        File file = new File(filePath);
        if (!file.isFile()) {
            throw new IllegalArgumentException("O caminho informado não é um arquivo.");
        }

        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);
            String[] lines = text.split("\\r?\\n");

            // Verifica se há pelo menos 3 linhas no PDF
            if (lines.length >= 3) {
                String thirdLine = lines[2];
                if (thirdLine.contains("/")) {
                    List<String> parts = Arrays.asList(thirdLine.split("\\s+"));
                    // Imprime todas as partes exceto a primeira
                    for (int i = 1; i < parts.size(); i++) {
                        System.out.print(parts.get(i) + " ");
                    }
                    System.out.println(); // Pula para a próxima linha após a impressão
                }
            } else {
                System.out.println("O arquivo PDF não possui pelo menos 3 linhas.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Substitua pelo caminho do seu arquivo PDF
        String filePath = "C:/Users/eduardo_brito/Documents/testedas/a.pdf";
        printPDFLines(filePath);
    }
}
