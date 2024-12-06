package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import lombok.Setter;
import org.example.security.Auth;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import javafx.stage.Stage;

import static org.jooq.codegen.maven.example.Tables.USERS;

public class AddBalanceControler {

    @FXML
    private TextField amount;
    long UserID;
    @FXML
    private TextField bonusCode;

    @Setter
    private Controller mainController;

    @FXML
    private Label balanceLabel;

    boolean pridanie = false;

    @FXML
    public void initialize() {
        balanceLabel.setText("Pridajte prostriedky pre " + Auth.INSTANCE.getPrincipal().getUsername());

        // Add a listener for the amount field to restrict input
        amount.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            String input = amount.getText() + event.getCharacter();
            if (!input.matches("\\d*(\\.\\d{0,2})?") || input.startsWith(".")) {
                event.consume();  // Ignore the input if it doesn't match the pattern
            }
        });
    }

    @FXML
    void submit(ActionEvent event) throws SQLException {
        if (!pridanie) {
            Properties config = ConfigReader.loadProperties("config.properties");
            String dbUrl = config.getProperty("db.url");

            // Parse amount as a double directly
            double amountValue = Double.parseDouble(amount.getText());

            if (amountValue > 0) {
                // Apply bonus codes
                if (bonusCode.getText().equals("lukas10")) {
                    amountValue = amountValue * 1.1;
                }
                if (bonusCode.getText().equals("SK10")) {
                    amountValue = amountValue * 10;
                }

                try (Connection connection = DriverManager.getConnection(dbUrl)) {
                    DSLContext create = DSL.using(connection);
                    create.update(USERS)
                            .set(USERS.BALANCE, USERS.BALANCE.plus(amountValue))
                            .where(USERS.USER_ID.eq((int) UserID))
                            .execute();
                    pridanie = true;
                }

                if (mainController != null) {
                    mainController.updateBalance();
                }
                Stage stage = (Stage) amount.getScene().getWindow();
                stage.close();
            }
        }
    }
}
