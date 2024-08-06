package org.project.functions.empresas;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QualitplacasContabil {

    private static String joinRange(String delimiter, String[] array, int start, int end) {
        List<String> subList = Arrays.asList(array).subList(start, end);
        return String.join(delimiter, subList);
    }

    public static List<RegistroQualitplacas> processQualitplacas(File pdfFile, boolean aplicarSubstituicoes) throws IOException {
        List<RegistroQualitplacas> registrosQualitplacas = new ArrayList<>();
        boolean linhasImprimir = false;
        boolean linhaAnteriorValorZero = false;
        String quebraCondicao = "QUALITPLACAS";

        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            int totalPages = document.getNumberOfPages();

            for (int paginaNum = 1; paginaNum <= totalPages; paginaNum++) {
                pdfStripper.setStartPage(paginaNum);
                pdfStripper.setEndPage(paginaNum);
                String textoPagina = pdfStripper.getText(document);
                String[] linhas = textoPagina.split("\n");

                boolean quebraPagina = false;
                int linhasQuebra = 0;
                String linhaAnterior = ""; // Para armazenar a linha anterior à linha do pagamento
                String valorLinhaAnterior = ""; // Para armazenar o valor da linha anterior

                for (String linha : linhas) {
                    if (linha.contains(quebraCondicao)) {
                        quebraPagina = true;
                        linhasQuebra = 0;
                    }

                    if (quebraPagina && paginaNum == 1) {
                        linhasQuebra++;
                        if (linhasQuebra >= 8 && !linha.contains("Histórico") && !linha.contains("Complemento")) {
                            linhasQuebra = 0;
                            linhasImprimir = true;
                            quebraPagina = false;
                        }
                    }

                    if (linhasImprimir) {
                        if (!linha.contains("Histórico") && !linha.contains("Complemento") && !linha.contains("Página:")) {
                            String[] partes = linha.split("\\s+");

                            // Define data e valor baseado na linha anterior
                            if (partes.length > 1 && !linhaAnterior.trim().isEmpty()) {
                                String data = limparData(linhaAnterior.trim()); // Usa a função limparData para extrair a data
                                String valor = partes[partes.length - 2]; // Penúltima parte da linha atual para valor
                                String credito = "";
                                String nota = "";
                                String desconto = ""; // Inicialmente vazio

                                // Se houver valor na linha anterior, defina o valor
                                if (!valorLinhaAnterior.isEmpty()) {
                                    valor = valorLinhaAnterior;
                                }

                                if (linha.contains("BANCO SAFRA MATRIZ") || linha.contains("9VIACREDI")) {
                                    if (!linha.contains("RECEITA DE REBATE") && !linha.contains("DEVOLUÇÃO DE COMPRA")) {
                                        // Valor já definido acima como penúltima parte
                                    } else {
                                        valor = partes[partes.length - 3]; // Corrigido para o caso especial
                                        credito = partes[partes.length - 3];
                                    }

                                    if (!valor.equals("0") && !valor.equals("0,00") && !valor.equals("0,0")
                                            && !linha.contains("DESPESAS BANCARIA") && !linha.contains("ALUGUEIS MAQUINA")) {
                                        if (valor.equals(credito)) {
                                            linhaAnteriorValorZero = false;
                                            valor = "";
                                        } else {
                                            linhaAnteriorValorZero = false;
                                        }
                                    } else {
                                        linhaAnteriorValorZero = true;
                                    }
                                } else {
                                    if (linhaAnteriorValorZero) {
                                        linhaAnteriorValorZero = false;
                                    } else {
                                        if (linha.contains("REC.REF.DOC.:")) {
                                            partes[0] = partes[0].replace("REC.REF.DOC.:", "");
                                        }
                                        if (linha.contains("PAG.REF.DOC.:")) {
                                            partes[0] = partes[0].replace("PAG.REF.DOC.:", "");
                                        }
                                        if (!linha.contains("PAG.REF.DOC.:AGR") && !linha.contains("REC.REF.DOC.:AGR")) {
                                            if (partes[0].contains("-")) {
                                                partes[0] = partes[0].split("-", 2)[0].trim();
                                            }
                                        } else {
                                            partes[0] = partes[0].replace("PAG.REF.DOC.:AGR", "");
                                            partes[0] = partes[0].replace("REC.REF.DOC.:AGR", "");
                                            if (partes[0].contains("-")) {
                                                partes[0] = partes[0].split("-")[1];
                                            }
                                            if (partes[0].contains("/")) {
                                                partes[0] = partes[0].split("/")[1];
                                            }
                                            if (partes[0].contains("-")) {
                                                partes[0] = partes[0].split("-", 2)[1].trim();
                                            }
                                        }
                                        if (linha.contains("SACADO")) {
                                            partes[1] = partes[1].replace("SACADO:", "");
                                        }
                                        if (linha.contains("DESC.TITULO")) {
                                            partes[0] = partes[0].replace("DESC.TITULO", "").trim();
                                            if (partes[1].contains("-")) {
                                                partes[1] = partes[1].split("-", 2)[0].trim();
                                            }
                                            if (partes[1].contains("/")) {
                                                partes[1] = partes[1].split("/", 2)[0].trim();
                                            }
                                            desconto = "DESCONTO DO TITULO " + partes[1];
                                        } else {
                                            if (partes[1].contains("-")) {
                                                partes[1] = partes[1].split("-", 2)[1].trim();
                                            }
                                        }

                                        if (linha.contains("PAG.REF.DOC.: ")) {
                                            credito = joinRange(" ", partes, 2, partes.length); // Corrigido
                                            nota = partes[1];
                                        } else {
                                            credito = joinRange(" ", partes, 1, partes.length); // Corrigido
                                            nota = partes[0];
                                            nota = nota.replaceAll("[a-zA-Z]", "");
                                        }

                                        if (aplicarSubstituicoes) {
                                            String[] substituicoes = substituirLista(new String[]{credito, valor});
                                            credito = substituirVirgulaPorPonto(substituicoes[0]);
                                            valor = substituirVirgulaPorPonto(substituicoes[1]);
                                        }

                                        if (!valor.equals("0") && !valor.equals("0,00") && !valor.equals("0,0")) {
                                            registrosQualitplacas.add(new RegistroQualitplacas(data, credito, nota, valor, desconto));
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Atualiza a linha anterior e o valor da linha anterior
                    linhaAnterior = linha;
                    String[] partesLinhaAnterior = linha.split("\\s+");
                    if (partesLinhaAnterior.length > 1) {
                        valorLinhaAnterior = partesLinhaAnterior[partesLinhaAnterior.length - 2];
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return registrosQualitplacas;
    }

    private static String limparData(String linha) {
        // Expressão regular para capturar uma data no formato dd/MM/yyyy
        String regex = "\\b\\d{2}/\\d{2}/\\d{4}\\b";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(linha);

        if (matcher.find()) {
            return matcher.group(); // Retorna a primeira data encontrada
        }
        return "";
    }


    private static String[] substituirLista(String[] valores) {
        for (int i = 0; i < valores.length; i++) {
            valores[i] = valores[i].replace(".", "");
        }
        return valores;
    }

    private static String substituirVirgulaPorPonto(String valor) {
        String[] substituicoes = {".10", ".20", ".30", ".40", ".50", ".60", ".70", ".80", ".90"};
        valor = valor.replace(",", ".");
        valor = valor.replace(".00", "");
        for (String substituicao : substituicoes) {
            if (valor.endsWith(substituicao)) {
                valor = valor.substring(0, valor.length() - 2) + substituicao.charAt(substituicao.length() - 1);
            }
        }
        return valor;
    }

    public static class RegistroQualitplacas {
        private final String data;
        private final String fornecedor;
        private final String nota;
        private final String valor;
        private final String desconto;

        public RegistroQualitplacas(String data, String fornecedor, String nota, String valor, String desconto) {
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
