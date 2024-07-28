package org.project.functions;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ConciliarPagos {

    public static List<List<String>> conciliar(File file1, File file2) {
        List<List<String>> reconciledData = new ArrayList<>();

        try (FileInputStream fis1 = new FileInputStream(file1);
             FileInputStream fis2 = new FileInputStream(file2);
             Workbook workbook1 = new XSSFWorkbook(fis1);
             Workbook workbook2 = new XSSFWorkbook(fis2)) {

            Sheet sheet1 = workbook1.getSheetAt(0);
            Sheet sheet2 = workbook2.getSheetAt(0);

            // Processar os arquivos e reconciliar os dados
            Map<String, List<String>> pagosMap = loadPagosData(sheet1);
            reconciledData = processSheets(sheet2, pagosMap);

        } catch (IOException e) {
            System.err.println("Erro ao conciliar arquivos: " + e.getMessage());
            e.printStackTrace();
        }

        return reconciledData;
    }

    private static Map<String, List<String>> loadPagosData(Sheet sheet1) {
        Map<String, List<String>> pagosMap = new HashMap<>();
        Iterator<Row> rowIterator1 = sheet1.iterator();

        while (rowIterator1.hasNext()) {
            Row row = rowIterator1.next();
            Cell dateCell = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            Cell valueCell = row.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            Cell descCell = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

            String date = getCellValue(dateCell);
            String value = getCellValue(valueCell);
            String description = getCellValue(descCell);

            String key = date + "|" + value;
            pagosMap.computeIfAbsent(key, k -> new ArrayList<>()).add(description);
        }

        return pagosMap;
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    return sdf.format(cell.getDateCellValue());
                } else {
                    return String.format("%.2f", cell.getNumericCellValue()).replace('.', ',');
                }
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            default:
                return cell.toString();
        }
    }

    private static List<List<String>> processSheets(Sheet sheet2, Map<String, List<String>> pagosMap) {
        List<List<String>> reconciledData = new ArrayList<>();
        Iterator<Row> rowIterator2 = sheet2.iterator();

        while (rowIterator2.hasNext()) {
            Row row2 = rowIterator2.next();
            List<String> rowData = new ArrayList<>();

            String date = getCellValue(row2.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK));
            String value = getCellValue(row2.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK));
            String key = date + "|" + value;

            for (int cn = 0; cn < row2.getLastCellNum(); cn++) {
                Cell cell2 = row2.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                String cell2Value = getCellValue(cell2);

                if (cn == 1) {  // Coluna de descrição
                    List<String> descriptions = pagosMap.get(key);
                    if (descriptions != null && !descriptions.isEmpty()) {
                        // Substituir a descrição se houver correspondência e remover a descrição usada
                        rowData.add(descriptions.remove(0));
                        if (descriptions.isEmpty()) {
                            pagosMap.remove(key);
                        }
                    } else {
                        rowData.add(cell2Value);
                    }
                } else {
                    rowData.add(cell2Value);
                }
            }

            reconciledData.add(rowData);
        }

        return reconciledData;
    }
}
