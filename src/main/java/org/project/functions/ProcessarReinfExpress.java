package org.project.functions;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Font;
import com.itextpdf.text.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcessarReinfExpress {

    private static final Font BOLD_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);

    public static void processarPlanilha(String excelFilePath, String outputDir) throws IOException, DocumentException {
        try (FileInputStream fis = new FileInputStream(excelFilePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            int numRows = sheet.getPhysicalNumberOfRows();
            Row headerRow = sheet.getRow(0);

            for (int i = 1; i < numRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String company = row.getCell(0).getStringCellValue();
                String cnpj = row.getCell(1).getStringCellValue();
                String nmrEmpresa = row.getCell(2).getStringCellValue();

                for (int j = 3; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell == null || cell.getCellType() != CellType.STRING) continue;

                    String header = headerRow.getCell(j).getStringCellValue();
                    String value = cell.getStringCellValue();
                    if ("Possui".equals(value) && header.contains("Aluguel")) continue;

                    Document document = new Document();
                    String cleanCompanyName = cleanFileName(company);
                    String cleanHeaderName = cleanFileName(header);
                    String pdfFileName = cleanCompanyName + " - " + cleanHeaderName + ".pdf";
                    pdfFileName = getUniqueFileName(outputDir, pdfFileName);
                    File pdfFile = new File(outputDir, pdfFileName);

                    PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
                    document.open();

                    // Adiciona parágrafos formatados em negrito e centralizados
                    document.add(createStyledParagraph("Empresa: " + company));
                    document.add(createStyledParagraph("CNPJ: " + cnpj));
                    document.add(createStyledParagraph("Número: " + nmrEmpresa));
                    document.add(createStyledParagraph(header + ": " + value));

                    document.close();
                }
            }
        }
    }

    private static Paragraph createStyledParagraph(String text) {
        Paragraph paragraph = new Paragraph(text, BOLD_FONT);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        return paragraph;
    }

    private static String cleanFileName(String name) {
        Pattern pattern = Pattern.compile("[\\\\/:\"*?<>|]");
        Matcher matcher = pattern.matcher(name);
        return matcher.replaceAll("");
    }

    private static String getUniqueFileName(String outputDir, String fileName) {
        File file = new File(outputDir, fileName);
        int count = 1;
        String baseName = fileName;
        String extension = "";

        // Verifica se o nome do arquivo contém uma extensão
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            baseName = fileName.substring(0, dotIndex);
            extension = fileName.substring(dotIndex);
        }

        // Adiciona um número ao nome do arquivo se o arquivo já existir
        while (file.exists()) {
            fileName = baseName + " (" + count + ")" + extension;
            file = new File(outputDir, fileName);
            count++;
        }

        return fileName;
    }
}
