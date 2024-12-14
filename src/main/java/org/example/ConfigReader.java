package org.example;

import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {

    private static Properties props = new Properties();

    public static Properties loadProperties(String fileName) {
        try (InputStream input = ConfigReader.class.getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                System.err.println("Nenašiel sa súbor: " + fileName);
                return props;
            }
            props.load(input);
        } catch (Exception e) {
            System.err.println("Nepodarilo sa načítať parametre: " + e.getMessage());
        }
        return props;
    }
}
