package org.project.functions.bancos;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.regex.Pattern;

public class ProcessarSicoob {

    private static final Pattern DATA_PATTERN = Pattern.compile("\\d{2}/\\d{2}/\\d{4}");

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

                int linhasAPular = (paginaNum == 1) ? 7 : 0;

                for (int linhaNum = linhasAPular; linhaNum < lines.length; linhaNum++) {
                    String linha = lines[linhaNum].trim();

                    if (linha.contains("(+):")) {
                        return dataRows;
                    }

                    if (linha.contains("SALDO DO DIA") || linha.contains("SALDO ANTERIOR") || linha.contains("SALDO BLOQUEADO ANTERIOR")) {
                        continue;
                    }

                    String[] partes = linha.split("\\s+");
                    if (partes.length < 2) {
                        continue;
                    }

                    // Verifica se a primeira parte da linha é uma data válida
                    boolean isDataValida = DATA_PATTERN.matcher(partes[0]).matches();
                    if (isDataValida) {
                        // Armazena a data e a descrição inicial
                        String data = partes[0];
                        String descricao = String.join(" ", Arrays.copyOfRange(partes, 1, (partes.length - 1)));

                        // Procura pela linha com o valor
                        String valor = null;
                        while (linhaNum + 1 < lines.length) {
                            linhaNum++;
                            String proxLinha = lines[linhaNum].trim();
                            String[] proxPartes = proxLinha.split("\\s+");
                            if (proxPartes.length > 0) {
                                valor = proxPartes[proxPartes.length - 1];
                                // Se a última parte contiver uma vírgula, temos um valor
                                if (valor.contains(",")) {
                                    break;
                                }
                            }
                        }

                        if (valor == null || !valor.contains(",")) {
                            continue;
                        }

                        String debito = "";
                        String credito = "";

                        // Verifica e ajusta valor e tipos de lançamento
                        if (valor.contains("C")) {
                            debito = codigo;
                            valor = valor.replace("C", "").trim();
                        } else if (valor.contains("D")) {
                            credito = codigo;
                            valor = valor.replace("D", "").trim();
                        }

                        if (useComma) {
                            valor = valor.replace(".", "").replace(",", ".");
                        }

                        dataRows.add(new String[]{data, descricao, valor, debito, credito});
                    }
                }
            }
        }

        return dataRows;
    }
}
