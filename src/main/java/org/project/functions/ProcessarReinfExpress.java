package org.project.functions;

import com.itextpdf.text.Image;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Font;
import com.itextpdf.text.Element;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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

                String company = getCellValue(row.getCell(0));
                String cnpj = getCellValue(row.getCell(1));
                String nmrEmpresa = getCellValue(row.getCell(2));

                for (int j = 3; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell == null) continue;

                    String header = getCellValue(headerRow.getCell(j));
                    String value = getCellValue(cell);

                    String normalizedValue = value.trim().toLowerCase();
                    String normalizedHeader = header.trim().toLowerCase();

                    if ("possui".equals(normalizedValue) && normalizedHeader.contains("aluguel")) continue;

                    Document document = new Document();
                    String cleanCompanyName = cleanFileName(company);
                    String cleanHeaderName = cleanFileName(header);
                    String pdfFileName = cleanCompanyName + " - " + cleanHeaderName + ".pdf";
                    pdfFileName = getUniqueFileName(outputDir, pdfFileName);
                    File pdfFile = new File(outputDir, pdfFileName);

                    PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
                    document.open();

                    try {
                        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                        String imagePath = "org/project/images/icon.png";
                        java.net.URL imageUrl = classLoader.getResource(imagePath);
                        if (imageUrl != null) {
                            Image image = Image.getInstance(imageUrl);
                            image.scaleToFit(70, 70);
                            image.setAlignment(Image.ALIGN_CENTER);
                            document.add(image);
                        } else {
                            System.err.println("Imagem não encontrada: " + imagePath);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    document.add(createStyledParagraph("Empresa: " + company));
                    document.add(createStyledParagraph("CNPJ: " + cnpj));
                    document.add(createStyledParagraph("Número: " + nmrEmpresa));
                    document.add(createStyledParagraph(header + ": " + value));

                    document.close();
                }
            }
        }
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
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

        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            baseName = fileName.substring(0, dotIndex);
            extension = fileName.substring(dotIndex);
        }

        while (file.exists()) {
            fileName = baseName + " (" + count + ")" + extension;
            file = new File(outputDir, fileName);
            count++;
        }

        return fileName;
    }
}
