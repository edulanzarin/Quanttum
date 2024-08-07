package org.project;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.File;
import java.io.IOException;

public class PdfReader {

    public static void main(String[] args) {
        // Caminho para o arquivo PDF
        String pdfPath = "S:\\CONTABIL\\CONTABILIDADE 2024\\CLIENTES FECHAMENTOS\\LUCRO REAL\\FRITZ DISTRIBUIDORA DE MATERIAIS ELETRICOS\\DOCUMENTOS RECEBIDOS\\072024\\Demais documentos\\CARTAO BB JUL 24.pdf";

        try (PDDocument document = PDDocument.load(new File(pdfPath))) {
            // Cria um PDFTextStripper para extrair texto
            PDFTextStripper pdfStripper = new PDFTextStripper();
            // Obtém o texto do PDF
            String text = pdfStripper.getText(document);

            // Divide o texto em linhas e imprime cada linha
            String[] lines = text.split(System.lineSeparator());
            for (String line : lines) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
