package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        var loader = new FXMLLoader(getClass().getResource("view.fxml"));
        Parent rootPane = loader.load();
        var scene = new Scene(rootPane);
        stage.setTitle("Stavkova kancelaria");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        // Define database connection parameters
        String url = "jdbc:mysql://localhost:3307/stavkova"; // Adjust for your Docker setup
        String username = "root";
        String password = "root";

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            if (connection != null) {
                System.out.println("Connection to the database was successful!");

                // Ensure you're using the correct database
                String useDatabaseQuery = "USE stavkova";
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute(useDatabaseQuery);
                }

                // Test query to check if data is accessible
                String query = "SELECT * FROM users"; // Select all users
                try (Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery(query)) {
                    if (rs.next()) {
                        System.out.println("Data is accessible!");
                        // Optionally, print the first column's value to verify data
                        System.out.println("First column value: " + rs.getString(1));
                    } else {
                        System.out.println("No data found in the table.");
                    }
                } catch (SQLException e) {
                    System.err.println("Query execution failed: " + e.getMessage());
                }
            } else {
                System.out.println("Failed to connect to the database!");
            }
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }

        launch(args);
    }

}
