package org.project.functions.bancos;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProcessarSicredi {

    public static List<String[]> processarPDF(File pdfFile, String codigo, boolean useComma) throws IOException {
        List<String[]> dataRows = new ArrayList<>();

        try (PDDocument document = PDDocument.load(pdfFile)) {

            PDFTextStripper pdfStripper = new PDFTextStripper();
            int totalPages = document.getNumberOfPages();

            for (int paginaNum = 1; paginaNum <= totalPages; paginaNum++) {
                pdfStripper.setStartPage(paginaNum);
                pdfStripper.setEndPage(paginaNum);
                String text = pdfStripper.getText(document);

                String[] lines = text.split("\n");
                // Defina linhas a pular apenas na primeira página
                int linhasAPular = paginaNum == 1 ? 6 : 0;

                for (int linhaNum = linhasAPular; linhaNum < lines.length; linhaNum++) {
                    String linha = lines[linhaNum];

                    if (linha.contains("Saldo da conta") || linha.contains("Saldo Atual")) {
                        break;
                    }

                    String[] partes = linha.trim().split("\\s+");
                    if (partes.length < 3) {
                        continue; // Linha não tem partes suficientes para processar
                    }

                    String data = partes[0];
                    String valor = partes[partes.length - 2];
                    String descricao = String.join(" ", Arrays.copyOfRange(partes, 1, partes.length - 2));

                    String debito = "";
                    String credito = "";

                    if (valor.contains("-")) {
                        credito = codigo;  // Código no crédito
                        valor = valor.replace("-", "");
                    } else {
                        debito = codigo;   // Código no débito
                    }

                    if (useComma) {
                        valor = valor.replace(".", "").replace(",", ".");
                    }

                    dataRows.add(new String[]{data, descricao, valor, debito, credito});
                }
            }
        }

        return dataRows;
    }
}
