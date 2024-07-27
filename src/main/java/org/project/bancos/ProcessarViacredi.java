package org.project.bancos;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcessarViacredi {

    public static List<String[]> processarPDF(File pdfFile, String codigo, boolean useComma) throws IOException {
        List<String[]> dataRows = new ArrayList<>();

        try (PDDocument document = PDDocument.load(pdfFile)) {

            PDFTextStripper pdfStripper = new PDFTextStripper();
            int totalPages = document.getNumberOfPages();
            Pattern datePattern = Pattern.compile("\\d{2}/\\d{2}/\\d{4}");

            for (int paginaNum = 1; paginaNum <= totalPages; paginaNum++) {
                pdfStripper.setStartPage(paginaNum);
                pdfStripper.setEndPage(paginaNum);
                String text = pdfStripper.getText(document);

                String[] lines = text.split("\n");
                int linhasAPular = (paginaNum == 1) ? 7 : 1;

                for (int linhaNum = linhasAPular; linhaNum < lines.length; linhaNum++) {
                    String linha = lines[linhaNum];

                    if (linha.contains("TOTAL")) {
                        break;
                    }

                    StringBuilder descricao = new StringBuilder();
                    String data = "";
                    String valor = "";
                    String debito = "";
                    String credito = "";

                    Matcher matcher = datePattern.matcher(linha);
                    if (matcher.find()) {
                        int dataIndex = matcher.start();
                        descricao.append(linha, 0, dataIndex);
                        data = matcher.group();

                        String[] partes = linha.substring(dataIndex).split("\\s+");
                        if (partes.length >= 2) {
                            String valorParte = partes[partes.length - 2];
                            if (valorParte.contains("-")) {
                                valor = valorParte;  // Pagamento (valor negativo)
                                credito = codigo;  // Utilize o código fornecido pelo usuário
                            } else {
                                valor = partes[1];  // Recebimento (valor positivo)
                                if (!valor.contains("-")) {
                                    debito = codigo;  // Utilize o código fornecido pelo usuário
                                } else {
                                    valor = "";
                                }
                            }
                        }
                    } else {
                        descricao.append(linha);
                    }

                    // Remove os "-" do valor após definir débito e crédito
                    if (valor.contains("-")) {
                        valor = valor.replace("-", "");
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
