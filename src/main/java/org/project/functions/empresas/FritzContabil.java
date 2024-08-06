package org.project.functions.empresas;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class FritzContabil {

    public static List<RegistroFritzContabil> processFritz(File excelFile, boolean removeCommas) throws IOException {
        List<RegistroFritzContabil> registrosFritzContabil = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0); // Supondo que você deseja processar a primeira aba

            for (Row row : sheet) {
                // Pular a primeira linha se for o cabeçalho
                if (row.getRowNum() == 0) {
                    continue;
                }

                Cell cellData = row.getCell(4); // Coluna E (Data)
                Cell cellValor = row.getCell(5); // Coluna F (Valor)
                Cell cellDesconto = row.getCell(6); // Coluna G (Desconto)
                Cell cellFornecedor = row.getCell(2); // Coluna C (Fornecedor)
                Cell cellJuros = row.getCell(7); // Coluna H (Juros), ajustar a coluna conforme necessário
                Cell cellNota = row.getCell(10); // Coluna K (Nota)

                if (cellData != null && cellValor != null && cellFornecedor != null && cellNota != null) {
                    String data = convertData(cellData.toString());
                    String valor = cellValor.toString();
                    String desconto = (cellDesconto != null) ? cellDesconto.toString() : "";
                    String fornecedor = cellFornecedor.toString();
                    String nota = processNota(cellNota.toString());

                    if (removeCommas) {
                        valor = valor.replace(",", "");
                        if (desconto != null && !desconto.isEmpty()) {
                            desconto = desconto.replace(",", "");
                        }
                        if (cellJuros != null && !cellJuros.toString().isEmpty()) {
                            String juros = cellJuros.toString().replace(",", "");
                        }
                    }

                    // Adiciona o lançamento original
                    registrosFritzContabil.add(new RegistroFritzContabil(data, fornecedor, nota, valor));

                    // Verifica se há desconto e adiciona um novo lançamento
                    if (isGreaterThanZero(desconto)) {
                        String descricaoDesconto = fornecedor + " - DESCONTO";
                        registrosFritzContabil.add(new RegistroFritzContabil(data, descricaoDesconto, nota, desconto));
                    }

                    // Verifica se há juros e adiciona um novo lançamento
                    if (isGreaterThanZero(cellJuros)) {
                        String juros = cellJuros.toString();
                        String descricaoJuros = fornecedor + " - JUROS";
                        registrosFritzContabil.add(new RegistroFritzContabil(data, descricaoJuros, nota, juros));
                    }
                }
            }
        }

        return registrosFritzContabil;
    }

    private static String convertData(String data) {
        // Define o formato da data de entrada
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd-MMM-uuuu");

        // Define o formato da data de saída
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try {
            // Faz o parse da data usando o formato de entrada
            LocalDate date = LocalDate.parse(data, inputFormatter);

            // Formata a data no formato de saída
            return date.format(outputFormatter);
        } catch (DateTimeParseException e) {
            // Retorna a data original se houver um erro de parse
            return data;
        }
    }


    private static boolean isGreaterThanZero(String value) {
        try {
            double number = Double.parseDouble(value.replace(",", ""));
            return number > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isGreaterThanZero(Cell cell) {
        if (cell != null && cell.getCellType() == CellType.NUMERIC) {
            double number = cell.getNumericCellValue();
            return number > 0;
        }
        return false;
    }

    private static String processNota(String nota) {
        if (nota != null && nota.contains("-")) {
            return nota.split("-")[0].trim();
        }
        return nota; // Retorna a nota original se não contiver um hífen
    }

    public static class RegistroFritzContabil {
        private final String data;
        private final String fornecedor;
        private final String nota;
        private final String valor;

        public RegistroFritzContabil(String data, String fornecedor, String nota, String valor) {
            this.data = data;
            this.fornecedor = fornecedor;
            this.nota = nota;
            this.valor = valor;
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
    }
}
