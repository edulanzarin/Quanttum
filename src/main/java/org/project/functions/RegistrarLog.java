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
import java.util.UUID;

public class RegistrarLog {

    private static final String SPREADSHEET_ID = "1G39rq0NGIMJ4LFHQ7-3unHAD39C_aZGAIZx1L3d7cD8";
    private static final String RANGE = "logs!A:F";

    public static boolean logAction(String username, String action) {
        try {
            Sheets service = SheetsServiceUtil.getSheetsService();

            // Gera um UUID aleatório
            String uuid = UUID.randomUUID().toString();

            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String ip = InetAddress.getLocalHost().getHostAddress();
            String hostname = InetAddress.getLocalHost().getHostName();

            List<Object> row = List.of(uuid, timestamp, username, ip, hostname, action);
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
