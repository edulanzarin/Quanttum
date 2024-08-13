package org.project.functions.bancos;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.regex.Pattern;

public class ProcessarBB {

    private static final Pattern DATA_PATTERN = Pattern.compile("\\d{2}/\\d{2}/\\d{4}");

    public static List<String[]> processarPDF(File pdfFile, String codigo, boolean useComma) throws IOException {
        List<String[]> dataRows = new ArrayList<>();
        String dataAnterior = "";

        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            int totalPages = document.getNumberOfPages();

            for (int paginaNum = 1; paginaNum <= totalPages; paginaNum++) {
                pdfStripper.setStartPage(paginaNum);
                pdfStripper.setEndPage(paginaNum);
                String text = pdfStripper.getText(document);

                String[] lines = text.split("\n");

                int linhasAPular = (paginaNum == 1) ? 6 : 0;

                for (int linhaNum = linhasAPular; linhaNum < lines.length; linhaNum++) {
                    String linha = lines[linhaNum].trim();

                    if (linha.contains("S A L D O")) {
                        return dataRows;
                    }

                    String[] partes = linha.split("\\s+");
                    if (partes.length < 2) {
                        continue;
                    }

                    // Verifica se a primeira parte da linha é uma data válida
                    boolean isDataValida = DATA_PATTERN.matcher(partes[0]).matches();
                    if (isDataValida) {
                        String data = partes[0];
                        String descricao;
                        String valor;
                        String tipo = "";

                        if (data.equals(dataAnterior)) {
                            // Não é o primeiro lançamento do dia
                            if (partes.length >= 3) {
                                descricao = String.join(" ", Arrays.copyOfRange(partes, 1, partes.length - 2));
                                valor = partes[partes.length - 2];
                                tipo = partes[partes.length - 1];
                            } else {
                                continue;
                            }
                        } else {
                            // Primeiro lançamento do dia
                            if (partes.length >= 4) {
                                descricao = String.join(" ", Arrays.copyOfRange(partes, 2, partes.length - 2));
                                valor = partes[partes.length - 3];
                                tipo = partes[partes.length - 2];
                            } else {
                                continue;
                            }
                            dataAnterior = data;
                        }

                        if (valor.contains(",")) {
                            if (useComma) {
                                valor = valor.replace(".", "").replace(",", ".");
                            }

                            String debito = "";
                            String credito = "";

                            // Ajusta valor e tipos de lançamento
                            if ("C".equals(tipo)) {
                                debito = codigo;
                                valor = valor.replace("C", "").trim();
                            } else if ("D".equals(tipo)) {
                                credito = codigo;
                                valor = valor.replace("D", "").trim();
                            }

                            dataRows.add(new String[]{data, descricao, valor, debito, credito});
                        }
                    }
                }
            }
        }

        return dataRows;
    }
}
