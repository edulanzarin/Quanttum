package org.project.functions;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class ChangePassword {

    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    public boolean updatePassword(String userId, String newPassword) {
        try {
            Sheets service = SheetsServiceUtil.getSheetsService();

            String spreadsheetId = "1G39rq0NGIMJ4LFHQ7-3unHAD39C_aZGAIZx1L3d7cD8";
            String range = "usuarios!A2:D";

            ValueRange response = service.spreadsheets().values().get(spreadsheetId, range).execute();
            List<List<Object>> values = response.getValues();

            if (values == null || values.isEmpty()) {
                return false;
            } else {
                int rowIndex = -1;
                for (int i = 0; i < values.size(); i++) {
                    List<Object> row = values.get(i);
                    if (row.size() > 0 && row.get(0).equals(userId)) {
                        rowIndex = i + 2;
                        row.set(2, newPassword);
                        break;
                    }
                }

                if (rowIndex == -1) {
                    return false;
                }

                ValueRange body = new ValueRange()
                        .setValues(Collections.singletonList(values.get(rowIndex - 2)));

                service.spreadsheets().values()
                        .update(spreadsheetId, "usuarios!A" + rowIndex + ":D" + rowIndex, body)
                        .setValueInputOption("RAW")
                        .execute();

                // Obter o nome de usuário pelo ID
                String username = GetUsername.getUsernameById(userId);

                // Registrar log da ação de troca de senha
                RegistrarLog registrarLog = new RegistrarLog();
                registrarLog.logAction(username, "change-pass");

                return true;
            }
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}