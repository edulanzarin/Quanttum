package org.project.model;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

public class HeaderFooterPageEvent extends PdfPageEventHelper {

    private Image logo;
    private String dateTime;

    public HeaderFooterPageEvent(Image logo, String dateTime) {
        this.logo = logo;
        this.dateTime = dateTime;
    }

    @Override
    public void onEndPage(PdfWriter writer, com.itextpdf.text.Document document) {
        PdfContentByte cb = writer.getDirectContent();
        try {
            // Centraliza a imagem horizontalmente
            float pageWidth = document.getPageSize().getWidth();
            float imageWidth = logo.getScaledWidth();
            float imageHeight = logo.getScaledHeight();
            float x = (pageWidth - imageWidth) / 2;
            float y = document.getPageSize().getHeight() - imageHeight - 10;

            logo.setAbsolutePosition(x, y);
            cb.addImage(logo);

            // Adiciona a data e hora ao rodapé
            cb.beginText();
            cb.setFontAndSize(com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD).getBaseFont(), 10);
            cb.setTextMatrix(document.left(), document.getPageSize().getBottom(40)); // Ajusta a posição vertical do texto
            cb.showText("Exportado em: " + dateTime);
            cb.endText();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }
}
