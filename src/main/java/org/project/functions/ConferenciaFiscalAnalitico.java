package org.project.functions;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConferenciaFiscalAnalitico {

    public static void processarPasta(File pasta) {
        if (!pasta.isDirectory()) {
            System.out.println("O caminho fornecido não é uma pasta.");
            return;
        }

        File[] arquivos = pasta.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
        if (arquivos == null || arquivos.length == 0) {
            System.out.println("Nenhum arquivo PDF encontrado na pasta.");
            return;
        }

        for (File arquivo : arquivos) {
            try {
                PDDocument document = PDDocument.load(arquivo);
                PDFTextStripper pdfStripper = new PDFTextStripper();
                String texto = pdfStripper.getText(document);
                document.close();

                String[] linhas = texto.split("\\r?\\n");
                if (linhas.length >= 3 && linhas[2].contains("Balancete")) {
                    continue;
                }

                processarArquivo(linhas);
            } catch (IOException e) {
                System.err.println("Erro ao processar o arquivo: " + arquivo.getName());
                e.printStackTrace();
            }
        }
    }

    private static void processarArquivo(String[] linhas) {
        Map<String, StringBuilder> naturezaMap = new HashMap<>();
        Map<String, Double> totalNaturezaMap = new HashMap<>();
        String chaveAtual = null;
        boolean dentroNatureza = false;
        boolean pularLinhas = false;
        int linhasParaPular = 0;

        for (int i = 0; i < linhas.length; i++) {
            String linha = linhas[i];

            if (pularLinhas) {
                linhasParaPular--;
                if (linhasParaPular <= 0) {
                    pularLinhas = false;
                }
                continue; // Ignorar as linhas enquanto pularLinhas é verdadeiro
            }

            if (linha.contains("Pág:")) {
                pularLinhas = true;
                linhasParaPular = 4; // Pular a linha atual e as próximas 4
                continue; // Ignorar a linha com "Pág:"
            }

            if (linha.startsWith("Natureza:")) {
                chaveAtual = linha.split("Natureza:")[1].trim();
                naturezaMap.putIfAbsent(chaveAtual, new StringBuilder());
                totalNaturezaMap.putIfAbsent(chaveAtual, 0.0);
                dentroNatureza = true;
            } else if (dentroNatureza && !linha.contains("Total Natureza")) {
                String[] partes = linha.split("\\s+");
                if (partes.length >= 8) {
                    String parte4 = partes[3];
                    String parteSelecionada = partes[7];
                    if ("0,00".equals(parteSelecionada) && partes[6].contains(",")) {
                        parteSelecionada = partes[6];
                    }
                    // Adicionar ao StringBuilder
                    naturezaMap.get(chaveAtual).append(chaveAtual).append(" ").append(parte4).append(" ").append(parteSelecionada).append("\n");

                    // Calcular o total
                    try {
                        // Substituir "." por "" e "," por "."
                        String valorFormatado = parteSelecionada.replace(".", "").replace(",", ".");
                        double valor = Double.parseDouble(valorFormatado);
                        totalNaturezaMap.put(chaveAtual, totalNaturezaMap.get(chaveAtual) + valor);
                    } catch (NumberFormatException e) {
                        // Ignorar valores inválidos
                        System.err.println("Valor inválido: " + parteSelecionada);
                    }
                }
            } else if (linha.contains("Total Natureza")) {
                dentroNatureza = false;
            }
        }

        // Imprimir resultados
        for (String chave : naturezaMap.keySet()) {
            System.out.print(naturezaMap.get(chave).toString());
            double total = totalNaturezaMap.getOrDefault(chave, 0.0);
            System.out.printf("Total %s: %.2f\n", chave, total);
        }
    }
}
