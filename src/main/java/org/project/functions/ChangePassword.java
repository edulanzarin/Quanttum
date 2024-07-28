package org.project.functions;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class ChangePassword {

    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);

    private static Credential getCredentials(final HttpTransport HTTP_TRANSPORT) throws IOException {
        // Substitua pelos tokens obtidos no OAuth Playground
        String accessToken = "1//04vNgFKvK_xc7CgYIARAAGAQSNwF-L9IrcIDLFovCu_vv-8-tU2kgZ52620Zuzk551aaRAGg0UnpYYf4dvEgoPf5n8jCwCWKgdx4";
        String refreshToken = "ya29.a0AXooCgs3ols8GEsp6nFefmi4m2mg0uBZorFlw5Vds_tZ8SvJSC0se26eo_9l2u4wDC8xtZygk27dBhfJoQ66PldZEwqX0SXRJF-_mcee5O0OVO-IFYA9_r26Sj8a2DZD-Z7_ZQCNc7p6_A6UpIU9T-NJrg_vyZhA4VE6aCgYKAfYSARESFQHGX2MiUerjuTnNPAwx6sNzd6zivA0171";

        TokenResponse tokenResponse = new TokenResponse()
                .setAccessToken(accessToken)
                .setRefreshToken(refreshToken);

        return new GoogleCredential.Builder()
                .setTransport(HTTP_TRANSPORT)
                .setJsonFactory(JSON_FACTORY)
                .setClientSecrets("601905702484-k77f1495flr29j5olo6f0hjnr3q054cp.apps.googleusercontent.com", "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")
                .build()
                .setFromTokenResponse(tokenResponse);
    }

    public boolean updatePassword(String userId, String newPassword) {
        try {
            final HttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            String spreadsheetId = "1G39rq0NGIMJ4LFHQ7-3unHAD39C_aZGAIZx1L3d7cD8";
            String range = "Sheet1!A2:D"; // Ajuste o intervalo conforme necessário

            ValueRange response = service.spreadsheets().values().get(spreadsheetId, range).execute();
            List<List<Object>> values = response.getValues();

            if (values == null || values.isEmpty()) {
                return false;
            } else {
                int rowIndex = -1;
                for (int i = 0; i < values.size(); i++) {
                    List<Object> row = values.get(i);
                    if (row.size() > 0 && row.get(0).equals(userId)) {
                        rowIndex = i + 2; // +2 para considerar o índice baseado em 1 e a linha do cabeçalho
                        row.set(2, newPassword); // Atualizar a senha (3ª coluna)
                        break;
                    }
                }

                if (rowIndex == -1) {
                    return false;
                }

                ValueRange body = new ValueRange()
                        .setValues(Collections.singletonList(values.get(rowIndex - 2)));

                service.spreadsheets().values()
                        .update(spreadsheetId, "Sheet1!A" + rowIndex + ":D" + rowIndex, body)
                        .setValueInputOption("RAW")
                        .execute();

                return true;
            }
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
