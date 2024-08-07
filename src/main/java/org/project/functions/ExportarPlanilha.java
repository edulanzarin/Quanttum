package org.project.functions;

import org.project.view.bancos.ProcessarExtratoContabilContent.Transaction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class ExportarPlanilha {

    public static void exportExtrato(File file, List<Transaction> transactions, String userId) throws IOException, GeneralSecurityException {
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

            // Registrar log da ação de exportação da planilha
            RegistrarLog registrarLog = new RegistrarLog();
            String username = GetUsername.getUsernameById(userId);
            registrarLog.logAction(username, "export-planilha");
        }
    }

    public static void exportConciliacao(List<List<String>> data, File file) {
        if (!file.getName().endsWith(".csv")) {
            file = new File(file.getAbsolutePath() + ".csv");
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Adicionar o cabeçalho
            writer.write("Data;Descrição;Valor");
            writer.newLine();

            // Adicionar os dados
            for (List<String> row : data) {
                // Assumindo que os dados estão na ordem correta: Data, Descrição, Valor
                writer.write(String.join(";", row));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Erro ao exportar dados para CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
