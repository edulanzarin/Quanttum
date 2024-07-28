package org.project.functions;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.net.InetAddress;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class RegistrarLog {

    private static final String SPREADSHEET_ID = "1G39rq0NGIMJ4LFHQ7-3unHAD39C_aZGAIZx1L3d7cD8";
    private static final String RANGE = "logs!A:F";

    public boolean logAction(String username, String action) {
        try {
            Sheets service = SheetsServiceUtil.getSheetsService();

            // Obter o último ID
            ValueRange response = service.spreadsheets().values()
                    .get(SPREADSHEET_ID, RANGE)
                    .execute();
            List<List<Object>> values = response.getValues();

            int nextId = 1; // Valor inicial se a planilha estiver vazia
            if (values != null && !values.isEmpty()) {
                List<Object> lastRow = values.get(values.size() - 1);
                if (lastRow != null && !lastRow.isEmpty()) {
                    try {
                        String lastIdStr = lastRow.get(0).toString();
                        nextId = Integer.parseInt(lastIdStr) + 1;
                    } catch (NumberFormatException e) {
                        // Se a célula não contém um número, o próximo ID será 1
                        nextId = 1;
                    }
                }
            }

            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String ip = InetAddress.getLocalHost().getHostAddress();
            String hostname = InetAddress.getLocalHost().getHostName();

            List<Object> row = List.of(nextId, timestamp, username, ip, hostname, action);
            ValueRange appendBody = new ValueRange()
                    .setValues(Collections.singletonList(row));

            service.spreadsheets().values()
                    .append(SPREADSHEET_ID, RANGE, appendBody)
                    .setValueInputOption("RAW")
                    .execute();

            return true;
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
