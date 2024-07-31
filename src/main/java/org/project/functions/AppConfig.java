package org.project.functions;

import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static final String CONFIG_FILE = "config.properties";
    private static Properties properties = new Properties();

    static {
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
            } else {
                throw new RuntimeException("Configuration file not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getVersion() {
        return properties.getProperty("version", "unknown");
    }
}
