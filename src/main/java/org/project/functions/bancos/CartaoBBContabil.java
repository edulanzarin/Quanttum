package org.project.functions.bancos;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CartaoBBContabil {

    public static List<RegistroCartaoBB> processCartaoBB(File pdfFile) throws IOException {
        List<RegistroCartaoBB> registros = new ArrayList<>();
        boolean startProcessing = false; // Flag para iniciar o processamento

        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            int totalPages = document.getNumberOfPages();

            for (int pageNum = 1; pageNum <= totalPages; pageNum++) {
                pdfStripper.setStartPage(pageNum);
                pdfStripper.setEndPage(pageNum);
                String textoPagina = pdfStripper.getText(document);
                String[] linhas = textoPagina.split("\n");

                for (String linha : linhas) {
                    linha = linha.trim();

                    if (linha.contains("Detalhamento")) {
                        startProcessing = true; // Iniciar o processamento após encontrar "Detalhamento"
                        continue; // Pular para a próxima linha
                    }

                    if (!startProcessing) {
                        continue; // Ignorar linhas antes de "Detalhamento"
                    }

                    if (linha.contains("R$") && (!linha.contains("SALDO FATURA") && (!linha.contains("PGTO DEBITO") && (!linha.contains("R$ -"))))) {
                        // Dividir a linha em partes
                        String[] partes = linha.split("\\s+");

                        // Verificar se a linha contém pelo menos 4 partes
                        if (partes.length >= 4) {
                            String data = partes[0]; // Data é a primeira parte
                            String descricao = linha.substring(linha.indexOf(partes[1])); // Descrição começa a partir da segunda parte
                            String valor = extrairValor(descricao);

                            // Remover o valor da descrição
                            descricao = descricao.replace("R$ " + valor, "").trim();

                            // Adicionar o registro
                            registros.add(new RegistroCartaoBB(data, descricao, valor));
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return registros;
    }

    private static String extrairValor(String descricao) {
        // Regex para encontrar valores monetários no formato R$ XXX,XX
        Pattern pattern = Pattern.compile("R\\$\\s*(\\d+(?:\\.\\d+)?(?:,\\d{2})?)");
        Matcher matcher = pattern.matcher(descricao);
        if (matcher.find()) {
            return matcher.group(1); // Retornar o valor encontrado
        }
        return null;
    }

    public static class RegistroCartaoBB {
        private String data;
        private String descricao;
        private String valor;

        public RegistroCartaoBB(String data, String descricao, String valor) {
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
