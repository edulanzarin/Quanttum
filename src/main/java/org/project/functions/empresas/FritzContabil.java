package org.project.functions.empresas;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class FritzContabil {

    private static String joinRange(String delimiter, String[] array, int start, int end) {
        List<String> subList = Arrays.asList(array).subList(start, end);
        return String.join(delimiter, subList);
    }

    public static List<RegistroFritzContabil> processFritz(File pdfFile, boolean removeCommas) throws IOException {
        List<RegistroFritzContabil> registrosFritzContabil = new ArrayList<>();
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            int totalPages = document.getNumberOfPages();

            for (int paginaNum = 1; paginaNum <= totalPages; paginaNum++) {
                pdfStripper.setStartPage(paginaNum);
                pdfStripper.setEndPage(paginaNum);
                String textoPagina = pdfStripper.getText(document);
                String[] linhas = textoPagina.split("\n");

                for (String linha : linhas) {
                    String[] partes = linha.split("\\s+");

                    // Verifica se a linha tem pelo menos 15 partes
                    if (partes.length >= 15) {
                        String nota = partes[0]; // Parte 1: Nota
                        String data = partes[partes.length - 6]; // Parte 6: Data
                        String valor = partes[partes.length - 4]; // Parte 4 de trás para frente: Valor
                        String fornecedor = joinRange(" ", partes, partes.length - 9, partes.length - 6); // Fornecedor da parte 7 até a parte 9 de trás para frente
                        String desconto = ""; // Desconto removido

                        if (removeCommas) {
                            valor = valor.replace(",", "");
                        }

                        registrosFritzContabil.add(new RegistroFritzContabil(data, fornecedor, nota, valor, desconto));
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return registrosFritzContabil;
    }

    public static class RegistroFritzContabil {
        private final String data;
        private final String fornecedor;
        private final String nota;
        private final String valor;
        private final String desconto;

        public RegistroFritzContabil(String data, String fornecedor, String nota, String valor, String desconto) {
            this.data = data;
            this.fornecedor = fornecedor;
            this.nota = nota;
            this.valor = valor;
            this.desconto = desconto;
        }

        public String getData() {
            return data;
        }

        public String getFornecedor() {
            return fornecedor;
        }

        public String getNota() {
            return nota;
        }

        public String getValor() {
            return valor;
        }

        public String getDesconto() {
            return desconto;
        }
    }
}
