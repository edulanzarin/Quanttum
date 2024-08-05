package org.project.empresas;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SupermercadoJK {

    public static List<RegistroSupermercadoJK> processSupermercadoJK(File pdfFile) throws IOException {
        List<RegistroSupermercadoJK> registrosSupermercadoJK = new ArrayList<>();
        String condicaoProcessamento = "Boletos";

        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            int totalPages = document.getNumberOfPages();

            for (int paginaNum = 1; paginaNum <= totalPages; paginaNum++) {
                pdfStripper.setStartPage(paginaNum);
                pdfStripper.setEndPage(paginaNum);
                String textoPagina = pdfStripper.getText(document);
                String[] linhas = textoPagina.split("\n");

                int linhasPular = (paginaNum == 1) ? 6 : 3;

                for (int i = linhasPular; i < linhas.length; i++) {
                    String linha = linhas[i].trim();

                    if (linha.toUpperCase().contains("PIX") ||
                            (linha.contains(condicaoProcessamento) && !linha.matches("\\d{2}/\\d{2}/\\d{4}.*"))) {
                        continue;
                    }

                    if (linha.contains(condicaoProcessamento)) {
                        String[] partes = linha.split("\\s+");

                        String data = partes[0];
                        String valor = "";
                        StringBuilder descricaoBuilder = new StringBuilder();

                        int index = -1;
                        for (int j = 1; j < partes.length; j++) {
                            if (partes[j].matches(".*-\\d{2}")) {
                                index = j;
                                break;
                            }
                        }

                        if (index != -1 && index + 1 < partes.length) {
                            valor = partes[index + 1];
                        }

                        for (int j = 1; j < partes.length; j++) {
                            if (j == index) {
                                break;
                            }
                            if (j != 1) { // Pular a segunda parte
                                descricaoBuilder.append(partes[j]).append(" ");
                            }
                        }

                        String descricao = descricaoBuilder.toString().trim();
                        registrosSupermercadoJK.add(new RegistroSupermercadoJK(data, descricao, valor));

                    } else if (linha.split("\\s+").length > 3 && linha.matches("\\d{2}/\\d{2}/\\d{4}.*") && !linha.contains("-")) {
                        if (i + 2 < linhas.length) {
                            String[] partesLinhaAtual = linha.split("\\s+");
                            String data = partesLinhaAtual[0];

                            String descricao = partesLinhaAtual.length > 2 ? partesLinhaAtual[2] : "";
                            descricao += " " + linhas[i + 1].trim();

                            String[] partesLinhaAposProxima = linhas[i + 2].split("\\s+");
                            String valor = partesLinhaAposProxima.length > 1 ? partesLinhaAposProxima[1] : "";

                            registrosSupermercadoJK.add(new RegistroSupermercadoJK(data, descricao, valor));
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return registrosSupermercadoJK;
    }

    public static List<RegistroSupermercadoJK> processCaixaSupermercadoJK(File pdfFile) throws IOException {
        List<RegistroSupermercadoJK> registrosCaixa = new ArrayList<>();

        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            int totalPages = document.getNumberOfPages();

            for (int pageNum = 1; pageNum <= totalPages; pageNum++) {
                pdfStripper.setStartPage(pageNum);
                pdfStripper.setEndPage(pageNum);
                String pageText = pdfStripper.getText(document);
                String[] lines = pageText.split("\n");

                for (int i = 4; i < lines.length - 1; i++) {
                    String line = lines[i].trim();

                    if (!line.isEmpty()) {
                        String[] parts = line.split("\\s+");
                        if (parts.length >= 8) {
                            StringBuilder descriptionBuilder = new StringBuilder();
                            for (int j = 7; j < parts.length - 1; j++) {
                                descriptionBuilder.append(parts[j]).append(" ");
                            }
                            String description = descriptionBuilder.toString().trim();

                            String value = null;
                            for (int k = i; k < lines.length; k++) {
                                String[] valueParts = lines[k].split("\\s+");
                                String lastPart = valueParts[valueParts.length - 1];
                                if (!lastPart.matches(".*[a-zA-Z]+.*")) {
                                    value = lastPart;
                                    break;
                                }
                            }

                            String date = null;
                            for (int k = i + 1; k < lines.length; k++) {
                                String[] nextLineParts = lines[k].split("\\s+");
                                String lastPart = nextLineParts[nextLineParts.length - 1];
                                if (lastPart.matches("\\d{2}/\\d{2}/\\d{4}")) {
                                    date = lastPart;
                                    i = k; // Pula as linhas já processadas
                                    break;
                                }
                            }

                            if (date != null && value != null) {
                                registrosCaixa.add(new RegistroSupermercadoJK(date, description, value));
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return registrosCaixa;
    }

    public static class RegistroSupermercadoJK {
        private String data;
        private String descricao;
        private String valor;

        public RegistroSupermercadoJK(String data, String descricao, String valor) {
            this.data = data;
            this.descricao = descricao;
            this.valor = valor;
        }

        public String getData() {
            return data;
        }

        public String getDescricao() {
            return descricao;
        }

        public String getValor() {
            return valor;
        }

        public void setValor(String valor) {
            this.valor = valor;
        }
    }
}
