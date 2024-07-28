package org.project.functions;

import org.project.view.contents.ProcessarExtratoContabilContent.Transaction;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ExportarExtrato {

    public static void exportToCSV(File file, List<Transaction> transactions) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Cabeçalho
            writer.write("Data;Descrição;Valor;Débito;Crédito");
            writer.newLine();

            // Dados da tabela
            for (Transaction transaction : transactions) {
                // Remover espaços em branco desnecessários
                String date = transaction.getDate().trim();
                String description = transaction.getDescription().trim();
                String value = transaction.getValue().trim();
                String debit = transaction.getDebit().trim();
                String credit = transaction.getCredit().trim();

                writer.write(date + ";" +
                        description + ";" +
                        value + ";" +
                        debit + ";" +
                        credit);
                writer.newLine();
            }
        }
    }
}
