package org.example;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.codegen.maven.example.tables.Users; // Adjust based on your actual package path

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        // Define database connection parameters
        String url = "jdbc:mysql://localhost:3307/stavkova"; // Adjust for your Docker setup
        String username = "root";
        String password = "root";

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            if (connection != null) {
                System.out.println("Connection to the database was successful!");

                // Create a DSLContext for jOOQ
                DSLContext create = DSL.using(connection, SQLDialect.MYSQL);

                // jOOQ query to fetch all users
                create.selectFrom(Users.USERS) // Adjust to your actual table class
                        .fetch()
                        .forEach(record -> {
                            System.out.println("User ID: " + record.getValue(Users.USERS.USER_ID)); // Replace with your actual column names
                            System.out.println("Username: " + record.getValue(Users.USERS.USERNAME)); // Adjust accordingly
                        });
            } else {
                System.out.println("Failed to connect to the database!");
            }
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }
}
