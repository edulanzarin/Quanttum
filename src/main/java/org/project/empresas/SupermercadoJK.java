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

                // Define o número de linhas a pular com base na página atual
                int linhasPular = (paginaNum == 1) ? 6 : 3;

                for (int i = linhasPular; i < linhas.length; i++) {
                    String linha = linhas[i].trim();

                    // Pular linhas que contêm "PIX"
                    if (linha.toUpperCase().contains("PIX") ||
                            (linha.contains(condicaoProcessamento) && !linha.matches("\\d{2}/\\d{2}/\\d{4}.*"))) {
                        continue;
                    }

                    // Verifica se a linha contém a condição de processamento
                    if (linha.contains(condicaoProcessamento)) {
                        String[] partes = linha.split("\\s+");

                        String data = partes[0];
                        String valor = "";
                        StringBuilder descricaoBuilder = new StringBuilder();

                        // Encontra o índice onde o padrão "-XX" ocorre
                        int index = -1;
                        for (int j = 1; j < partes.length; j++) {
                            if (partes[j].matches(".*-\\d{2}")) {
                                index = j;
                                break;
                            }
                        }

                        // Extraí o valor da parte que vem após o padrão "-XX"
                        if (index != -1 && index + 1 < partes.length) {
                            valor = partes[index + 1];
                        }

                        // Monta a descrição a partir das partes antes do padrão "-XX", excluindo a segunda parte
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
                        // Processa linhas com mais de 3 partes que não contêm "Boletos" e não possuem "-XX"
                        if (i + 2 < linhas.length) {
                            String[] partesLinhaAtual = linha.split("\\s+");
                            String data = partesLinhaAtual[0];

                            // Descrição: terceira parte da linha atual e toda a próxima linha
                            String descricao = partesLinhaAtual.length > 2 ? partesLinhaAtual[2] : "";
                            descricao += " " + linhas[i + 1].trim();

                            // Valor: segunda parte da linha após a próxima linha
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

    public static class RegistroSupermercadoJK {
        private String data;
        private String descricao;
        private String valor;

        // Construtor, getters e setters
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
