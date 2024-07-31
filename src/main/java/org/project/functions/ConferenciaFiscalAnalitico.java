package org.project.functions;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConferenciaFiscalAnalitico {

    public static List<NaturezaConta> processarPasta(File pasta) {
        List<NaturezaConta> naturezas = new ArrayList<>();

        if (!pasta.isDirectory()) {
            System.out.println("O caminho fornecido não é uma pasta.");
            return naturezas;
        }

        File[] arquivos = pasta.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
        if (arquivos == null || arquivos.length == 0) {
            System.out.println("Nenhum arquivo PDF encontrado na pasta.");
            return naturezas;
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

                naturezas.addAll(processarArquivo(linhas));
            } catch (IOException e) {
                System.err.println("Erro ao processar o arquivo: " + arquivo.getName());
                e.printStackTrace();
            }
        }
        return naturezas;
    }

    private static List<NaturezaConta> processarArquivo(String[] linhas) {
        Map<String, List<NaturezaConta>> naturezaMap = new HashMap<>();
        Map<String, BigDecimal> totalNaturezaMap = new HashMap<>();
        List<NaturezaConta> naturezas = new ArrayList<>();
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
                continue;
            }

            if (linha.contains("Pág:")) {
                pularLinhas = true;
                linhasParaPular = 4;
                continue;
            }

            if (linha.startsWith("Natureza:")) {
                chaveAtual = linha.split("Natureza:")[1].trim();
                naturezaMap.putIfAbsent(chaveAtual, new ArrayList<>());
                totalNaturezaMap.putIfAbsent(chaveAtual, BigDecimal.ZERO);
                dentroNatureza = true;
            } else if (dentroNatureza && !linha.contains("Total Natureza")) {
                String[] partes = linha.split("\\s+");
                if (partes.length >= 8) {
                    String parte4 = partes[3];
                    String parteSelecionada = partes[7];
                    if ("0,00".equals(parteSelecionada) && partes[6].contains(",")) {
                        parteSelecionada = partes[6];
                    }
                    String nota = partes[3]; // Supondo que a coluna "Nota" está na posição 1
                    BigDecimal valor = new BigDecimal(parteSelecionada.replace(".", "").replace(",", "."));

                    naturezaMap.get(chaveAtual).add(new NaturezaConta(chaveAtual, nota, valor));
                    totalNaturezaMap.put(chaveAtual, totalNaturezaMap.get(chaveAtual).add(valor));
                }
            } else if (linha.contains("Total Natureza")) {
                dentroNatureza = false;
            }
        }

        for (String chave : naturezaMap.keySet()) {
            List<NaturezaConta> lista = naturezaMap.get(chave);
            BigDecimal total = totalNaturezaMap.getOrDefault(chave, BigDecimal.ZERO);

            // Adiciona as entradas da natureza
            naturezas.addAll(lista);

            // Obtém a primeira parte da chave e cria a nota do total
            String notaTotal = chave.split("\\s+")[0] + "  Total";
            naturezas.add(new NaturezaConta(notaTotal, "", total, true));
        }

        return naturezas;
    }

    public static class NaturezaConta {
        private String natureza;
        private String nota;
        private String valor;
        private boolean isTotal;

        public NaturezaConta(String natureza, String nota, BigDecimal valor) {
            this(natureza, nota, valor.toString(), false);
        }

        public NaturezaConta(String natureza, String nota, BigDecimal valor, boolean isTotal) {
            this(natureza, nota, valor.toString(), isTotal);
        }

        public NaturezaConta(String natureza, String nota, String valor, boolean isTotal) {
            this.natureza = natureza;
            this.nota = nota;
            this.valor = valor;
            this.isTotal = isTotal;
        }

        public String getNatureza() {
            return natureza;
        }

        public String getNota() {
            return nota;
        }

        public String getValor() {
            return valor;
        }

        public boolean isTotal() {
            return isTotal;
        }

        @Override
        public String toString() {
            return "NaturezaConta{" +
                    "natureza='" + natureza + '\'' +
                    ", nota='" + nota + '\'' +
                    ", valor='" + valor + '\'' +
                    ", isTotal=" + isTotal +
                    '}';
        }
    }
}
