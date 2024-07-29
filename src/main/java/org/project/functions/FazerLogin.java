package org.project.functions;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;

public class FazerLogin {

    private static final String SHEET_URL = "https://sheets.googleapis.com/v4/spreadsheets/1G39rq0NGIMJ4LFHQ7-3unHAD39C_aZGAIZx1L3d7cD8/values/usuarios?key=AIzaSyAqpPB5ax6k0ERjfQz5qJYGuv4dkCa-N9o";

    public String isUserActive(String user, String password) {
        try {
            URL url = new URL(SHEET_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Scanner scanner = new Scanner(url.openStream());
                StringBuilder response = new StringBuilder();
                while (scanner.hasNext()) {
                    response.append(scanner.nextLine());
                }
                scanner.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray values = jsonResponse.getJSONArray("values");

                for (int i = 1; i < values.length(); i++) {
                    JSONArray row = values.getJSONArray(i);
                    String userId = row.getString(0);
                    String userFromSheet = row.getString(1);
                    String passwordFromSheet = row.getString(2);
                    String statusFromSheet = row.getString(3);

                    if (userFromSheet.equals(user) && passwordFromSheet.equals(password) && statusFromSheet.equalsIgnoreCase("ativo")) {
                        return userId;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}