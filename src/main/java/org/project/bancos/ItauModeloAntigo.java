package org.project.bancos;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ItauModeloAntigo {

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
                int linhasAPular = 4;

                for (int linhaNum = linhasAPular; linhaNum < lines.length; linhaNum++) {
                    String linha = lines[linhaNum];

                    if (linha.contains("SALDO ANTERIOR") || linha.contains("SDO CTA")) {
                        continue;
                    }

                    if (linha.contains("S A L D O")) {
                        return dataRows;
                    }

                    String[] partes = linha.split("\\s+");
                    if (partes.length < 4) {
                        continue;
                    }

                    String data = partes[0];
                    StringBuilder descricao = new StringBuilder();
                    String ultimaParte = partes[partes.length - 1];
                    String valor;
                    String debito = "";
                    String credito = "";

                    if (ultimaParte.equals("-")) {
                        for (int i = 1; i < partes.length - 3; i++) {
                            descricao.append(partes[i]).append(" ");
                        }
                        valor = partes[partes.length - 2];
                        credito = codigo;
                    } else {
                        for (int i = 1; i < partes.length - 2; i++) {
                            descricao.append(partes[i]).append(" ");
                        }
                        valor = ultimaParte;
                        debito = codigo;
                    }

                    if (useComma) {
                        valor = valor.replace(".", "").replace(",", ".");
                    }

                    dataRows.add(new String[]{data, descricao.toString().trim(), valor, debito, credito});
                }
            }
        }

        return dataRows;
    }
}
