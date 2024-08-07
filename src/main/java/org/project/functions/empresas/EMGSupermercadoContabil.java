package org.project.functions.empresas;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EMGSupermercadoContabil {

    public static List<RegistroEMGSupermercado> processEMGSupermercado(File pdfFile) throws IOException {
        List<RegistroEMGSupermercado> registros = new ArrayList<>();

        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            int totalPages = document.getNumberOfPages();
            boolean stopProcessing = false; // Flag para controlar a parada do processamento

            for (int pageNum = 1; pageNum <= totalPages && !stopProcessing; pageNum++) {
                pdfStripper.setStartPage(pageNum);
                pdfStripper.setEndPage(pageNum);
                String textoPagina = pdfStripper.getText(document);
                String[] linhas = textoPagina.split("\n");

                for (int i = 3; i < linhas.length; i++) {
                    String linha = linhas[i].trim();

                    // Verificar se a linha contém "Sub-Total:"
                    if (linha.contains("Banco: 343 - SANTANDER")) {
                        stopProcessing = true;
                        break; // Interromper o loop de linhas
                    }

                    if (linha.contains("Portador") || (linha.contains("Sub-Total:"))) {
                        continue;
                    }

                    // Encontrar a data e o valor
                    String data = null;
                    String valor = null;
                    String descricao = null;
                    String desconto = null;
                    String juros = null;

                    // Identificar data, valor e outros detalhes
                    String[] partes = linha.split("\\s+");
                    String descricaoTemp = null;
                    int valorIndex = -1;

                    for (int j = 0; j < partes.length; j++) {
                        if (partes[j].matches("\\d{2}/\\d{2}/\\d{4}")) {
                            data = partes[j];
                            if (j + 2 < partes.length) {
                                valor = partes[j + 2]; // Corrigido para pegar o valor diretamente após a data
                                valorIndex = j + 2; // Salvar o índice do valor
                            }
                        } else if (data != null && valor != null) {
                            // Construir a descrição
                            if (descricaoTemp == null) {
                                descricaoTemp = linha.substring(0, linha.indexOf(data)).trim();
                            }
                        }
                    }

                    // Remover as últimas 5 partes da descrição
                    if (descricaoTemp != null) {
                        String[] descricaoPartes = descricaoTemp.split("\\s+");
                        if (descricaoPartes.length > 5) {
                            StringBuilder descricaoFinal = new StringBuilder();
                            for (int k = 0; k < descricaoPartes.length - 5; k++) {
                                descricaoFinal.append(descricaoPartes[k]).append(" ");
                            }
                            descricao = descricaoFinal.toString().trim();
                        } else {
                            // Se não houver pelo menos 5 partes, não há nada a remover
                            descricao = descricaoTemp.trim();
                        }
                    }

                    if (data != null && valor != null) {
                        registros.add(new RegistroEMGSupermercado(data, descricao, valor));

                        // Verificar e adicionar registros de desconto e juros com base no índice do valor
                        if (valorIndex + 1 < partes.length && !partes[valorIndex + 1].equals("0,00")) {
                            desconto = partes[valorIndex + 1];
                            registros.add(new RegistroEMGSupermercado(data, descricao + " - DESCONTO", desconto));
                        }
                        if (valorIndex + 2 < partes.length && !partes[valorIndex + 2].equals("0,00")) {
                            juros = partes[valorIndex + 2];
                            registros.add(new RegistroEMGSupermercado(data, descricao + " - JUROS", juros));
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return registros;
    }

    public static class RegistroEMGSupermercado {
        private String data;
        private String descricao;
        private String valor;

        public RegistroEMGSupermercado(String data, String descricao, String valor) {
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
