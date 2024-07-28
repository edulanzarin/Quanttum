package org.project.functions;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class SheetsServiceUtil {

    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String TOKENS_FILE_PATH = "tokens.json";

    private static Credential getCredentials(final HttpTransport HTTP_TRANSPORT) throws IOException {
        JsonObject tokens = loadTokens();
        if (tokens == null) {
            throw new FileNotFoundException("Tokens file not found");
        }

        String accessToken = tokens.get("access_token").getAsString();
        String refreshToken = tokens.get("refresh_token").getAsString();

        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(HTTP_TRANSPORT)
                .setJsonFactory(JSON_FACTORY)
                .setClientSecrets("CLIENT_ID", "CLIENT_SECRET")
                .build()
                .setAccessToken(accessToken)
                .setRefreshToken(refreshToken);

        // Refresh the access token if needed
        if (credential.getExpiresInSeconds() != null && credential.getExpiresInSeconds() <= 60) {
            try {
                credential.refreshToken();
                saveTokens(credential);
            } catch (Exception e) {
                e.printStackTrace();
                throw new IOException("Unable to refresh token", e);
            }
        }

        return credential;
    }

    private static JsonObject loadTokens() throws IOException {
        File tokenFile = new File(TOKENS_FILE_PATH);
        if (!tokenFile.exists()) {
            throw new FileNotFoundException("Tokens file not found");
        }
        try (FileReader reader = new FileReader(tokenFile)) {
            return new Gson().fromJson(reader, JsonObject.class);
        }
    }

    private static void saveTokens(GoogleCredential credential) throws IOException {
        JsonObject tokens = new JsonObject();
        tokens.addProperty("access_token", credential.getAccessToken());
        tokens.addProperty("refresh_token", credential.getRefreshToken());
        try (FileWriter writer = new FileWriter(TOKENS_FILE_PATH)) {
            new Gson().toJson(tokens, writer);
        }
    }

    public static Sheets getSheetsService() throws GeneralSecurityException, IOException {
        final HttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = getCredentials(HTTP_TRANSPORT);
        return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
