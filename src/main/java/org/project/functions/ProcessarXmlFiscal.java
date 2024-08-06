package org.project.functions;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessarXmlFiscal {

    public static void processarPasta(String camposDesejados, String pastaSelecionada, String caminhoCsv, String userId) {
        String username = GetUsername.getUsernameById(userId);

        try (PrintWriter writer = new PrintWriter(new File(caminhoCsv))) {
            String[] campos = camposDesejados.split(";");

            // Escreve o cabeçalho no CSV
            StringBuilder header = new StringBuilder();
            for (String campo : campos) {
                header.append(campo).append(";");
            }
            writer.println(header.toString());

            Files.walk(Paths.get(pastaSelecionada))
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".xml"))
                    .forEach(path -> processarArquivoXML(path.toFile(), campos, writer));

            // Registrar log de sucesso
            RegistrarLog registrarLog = new RegistrarLog();
            registrarLog.logAction(username, "process-xml-fiscal");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void processarArquivoXML(File arquivoXML, String[] campos, PrintWriter writer) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(arquivoXML);
            XPath xPath = XPathFactory.newInstance().newXPath();

            Map<String, List<String>> dados = new HashMap<>();
            int numeroLinhas = 0;

            for (String campo : campos) {
                NodeList nodeList = (NodeList) xPath.evaluate("//*[local-name()='" + campo + "']", doc, XPathConstants.NODESET);
                List<String> valores = new ArrayList<>();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    // Garante que o valor seja tratado como string
                    String valor = nodeList.item(i).getTextContent();
                    valores.add(valor != null ? valor : "");  // Adiciona uma string vazia se o valor for null
                }

                if (valores.isEmpty()) {
                    valores.add("");
                }

                dados.put(campo, valores);
                numeroLinhas = Math.max(numeroLinhas, valores.size());
            }

            for (int i = 0; i < numeroLinhas; i++) {
                StringBuilder row = new StringBuilder();
                for (String campo : campos) {
                    List<String> valores = dados.get(campo);
                    if (i < valores.size()) {
                        row.append(valores.get(i));
                    } else {
                        row.append("-");
                    }
                    row.append(";");
                }
                writer.println(row.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
