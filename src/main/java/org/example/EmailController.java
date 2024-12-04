package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import static org.jooq.codegen.maven.example.Tables.USERS;

public class EmailController {

    @FXML
    private TextField emailField;

    @FXML
    void confirmEmail(ActionEvent event) throws SQLException {
        Properties config = ConfigReader.loadProperties("config.properties");
        String dbUrl = config.getProperty("db.url");
        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            DSLContext create = DSL.using(connection);
            if (create.select(USERS.EMAIL).from(USERS).where(USERS.EMAIL.eq(emailField.getText())).fetchOneInto(String.class)==null){
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Pozor!");
                alert.setHeaderText("Email neexistuje!");
                alert.showAndWait();
            }else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Výborne!");
                alert.setHeaderText("Email bol odoslany!");
                alert.showAndWait();
            }


        }
    }


}
