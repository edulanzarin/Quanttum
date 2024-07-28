package org.project.functions;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class GetUsername {

    private static final String SPREADSHEET_ID = "1G39rq0NGIMJ4LFHQ7-3unHAD39C_aZGAIZx1L3d7cD8";
    private static final String RANGE = "usuarios!A2:D";

    public static String getUsernameById(String userId) {
        try {
            Sheets service = SheetsServiceUtil.getSheetsService();
            ValueRange response = service.spreadsheets().values().get(SPREADSHEET_ID, RANGE).execute();
            List<List<Object>> values = response.getValues();

            if (values == null || values.isEmpty()) {
                return null;
            } else {
                for (List<Object> row : values) {
                    if (row.size() > 1 && row.get(0).equals(userId)) {
                        return row.get(1).toString(); // Assume que a coluna B contém o nome de usuário
                    }
                }
            }
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }
}
