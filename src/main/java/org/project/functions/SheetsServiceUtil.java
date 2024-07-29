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
import java.nio.charset.StandardCharsets;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class SheetsServiceUtil {

    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String TOKENS_FILE_PATH = "tokens.json";
    private static final String CREDENTIALS_FILE_PATH = "credentials.json";

    private static Credential getCredentials(final HttpTransport HTTP_TRANSPORT) throws IOException {
        JsonObject tokens = loadTokens();
        JsonObject credentials = loadCredentials();

        if (tokens == null || credentials == null) {
            throw new FileNotFoundException("Tokens or credentials file not found");
        }

        if (!tokens.has("access_token") || !tokens.has("refresh_token")) {
            throw new IOException("Missing access_token or refresh_token in tokens.json");
        }
        if (!credentials.has("client_id") || !credentials.has("client_secret")) {
            throw new IOException("Missing client_id or client_secret in credentials.json");
        }

        String accessToken = tokens.get("access_token").getAsString();
        String refreshToken = tokens.get("refresh_token").getAsString();
        String clientId = credentials.get("client_id").getAsString();
        String clientSecret = credentials.get("client_secret").getAsString();

        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(HTTP_TRANSPORT)
                .setJsonFactory(JSON_FACTORY)
                .setClientSecrets(clientId, clientSecret)
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
        return loadJsonFromResource("tokens.json");
    }

    private static JsonObject loadCredentials() throws IOException {
        JsonObject credentialsJson = loadJsonFromResource("credentials.json");
        if (credentialsJson != null && credentialsJson.has("web")) {
            return credentialsJson.getAsJsonObject("web");
        }
        throw new IOException("Invalid credentials format in credentials.json");
    }

    private static JsonObject loadJsonFromResource(String resourcePath) throws IOException {
        InputStream inputStream = SheetsServiceUtil.class.getClassLoader().getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new FileNotFoundException("Resource not found: " + resourcePath);
        }
        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
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
