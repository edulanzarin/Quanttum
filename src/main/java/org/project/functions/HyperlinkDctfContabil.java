package org.project.functions;

import javafx.stage.Stage;
import javafx.scene.control.Alert;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HyperlinkDctfContabil {

    private File excelFile;
    private File folder;
    private String userId;

    public HyperlinkDctfContabil(File excelFile, File folder, String userId) {
        this.excelFile = excelFile;
        this.folder = folder;
        this.userId = userId;
    }

    public void processFiles(Stage primaryStage) {
        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            // Build the map of values in column E and their corresponding rows
            Map<String, Row> valueToRowMap = buildValueToRowMap(sheet);

            // Process files in the selected folder
            processFilesInFolder(sheet, valueToRowMap, primaryStage);

            // Save and close the workbook
            try (FileOutputStream fos = new FileOutputStream(excelFile)) {
                workbook.write(fos);
            }

            showAlert(primaryStage, Alert.AlertType.INFORMATION, "Concluído", "Processamento concluído.");

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(primaryStage, Alert.AlertType.ERROR, "Erro", "Erro ao processar arquivos.");
        }
    }

    private Map<String, Row> buildValueToRowMap(Sheet sheet) {
        Map<String, Row> valueToRowMap = new HashMap<>();
        int startRow = 2; // 0-based index, so 2 means third row

        for (int rowIndex = startRow; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);

            if (row == null) continue; // Skip empty rows

            Cell cellE = row.getCell(4); // Column E (index 4)

            // Convert column E value to String
            String valueE = getCellValueAsString(cellE);

            if (!valueE.isEmpty()) {
                valueToRowMap.put(valueE, row);
            }
        }
        return valueToRowMap;
    }

    private void processFilesInFolder(Sheet sheet, Map<String, Row> valueToRowMap, Stage primaryStage) {
        if (folder == null) return;

        File[] files = folder.listFiles();
        if (files == null) return;

        Pattern pattern = Pattern.compile("^(\\d+)_.*");
        Set<String> processedFiles = new HashSet<>();

        for (File file : files) {
            if (file.isFile() && !processedFiles.contains(file.getName())) {
                String fileName = file.getName();
                Matcher matcher = pattern.matcher(fileName);
                if (matcher.find()) {
                    String extractedValue = matcher.group(1);

                    // Check if there is a corresponding row for the extracted value
                    Row row = valueToRowMap.get(extractedValue);
                    if (row != null) {
                        // Retrieve the existing style from column E (index 4)
                        CellStyle originalCellStyle = null;
                        Cell cellE = row.getCell(4);
                        if (cellE != null) {
                            originalCellStyle = cellE.getCellStyle();
                        }

                        // Create hyperlink in column I
                        Cell cellI = row.createCell(8); // Column I (index 8)
                        CreationHelper creationHelper = sheet.getWorkbook().getCreationHelper();
                        Hyperlink hyperlink = creationHelper.createHyperlink(HyperlinkType.FILE);
                        String filePath = file.toURI().toString(); // Corrigir caminho para URI
                        hyperlink.setAddress(filePath);

                        // Determine the content to set based on file name
                        String content;
                        String lowerCaseFileName = file.getName().toLowerCase();
                        if (lowerCaseFileName.contains("recibo")) {
                            content = "RECIBO";
                        } else if (lowerCaseFileName.contains("erro")) {
                            content = "ERRO";
                        } else if (lowerCaseFileName.contains("declaracao")) { // Check for "declaracao" in any case
                            content = "DECLARAÇÃO"; // or any desired content
                        } else {
                            content = "Link para Arquivo";
                        }

                        cellI.setHyperlink(hyperlink);
                        cellI.setCellValue(content);

                        // Apply the original cell style
                        if (originalCellStyle != null) {
                            cellI.setCellStyle(originalCellStyle);
                        }

                        processedFiles.add(file.getName());
                    }
                }
            }
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString(); // Format as date if necessary
                } else if (Math.floor(cell.getNumericCellValue()) == cell.getNumericCellValue()) {
                    return String.valueOf((long) cell.getNumericCellValue()); // Convert to long if it's an integer
                } else {
                    return String.valueOf(cell.getNumericCellValue()); // Otherwise, keep as a string
                }
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private void showAlert(Stage primaryStage, Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        if (primaryStage != null) {
            alert.initOwner(primaryStage);
        }
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
