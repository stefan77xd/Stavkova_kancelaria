package org.example;

import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {
    public static Properties loadProperties(String fileName) {
        Properties props = new Properties();
        try (InputStream input = ConfigReader.class.getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                System.err.println("Sorry, unable to find " + fileName);
                return props;
            }
            // Load properties from the file
            props.load(input);
        } catch (Exception e) {
            System.err.println("Failed to load properties: " + e.getMessage());
        }
        return props;
    }

    public static void main(String[] args) {
        // Load properties file from resources
        Properties config = loadProperties("config.properties");

        // Fetch properties
        String dbUrl = config.getProperty("db.url");
        String dbUsername = config.getProperty("db.username");
        String dbPassword = config.getProperty("db.password");

        // Print to confirm it's loaded correctly
        System.out.println("DB URL: " + dbUrl);
        System.out.println("DB Username: " + dbUsername);
        System.out.println("DB Password: " + dbPassword); // Avoid printing in production!
    }
}
