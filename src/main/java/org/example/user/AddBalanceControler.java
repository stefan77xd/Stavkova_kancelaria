package org.example.user;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import lombok.Setter;
import org.example.ConfigReader;
import org.example.Controller;
import org.example.Factory;
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
    public long UserID;
    @FXML
    private TextField bonusCode;

    @Setter
    private Controller mainController;

    @FXML
    private Label balanceLabel;

    boolean pridanie = false;

    private final UserDAO userDAO = Factory.INSTANCE.getUserDAO();

    @FXML
    public void initialize() {
        balanceLabel.setText("Pridajte prostriedky pre " + Auth.INSTANCE.getPrincipal().getUsername());

        amount.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            String input = amount.getText() + event.getCharacter();
            if (!input.matches("\\d*(\\.\\d{0,2})?") || input.startsWith(".")) {
                event.consume();
            }
        });
    }

    @FXML
    void submit(ActionEvent event) {
        if (!pridanie) {
            double amountValue = Double.parseDouble(amount.getText());
            if (amountValue > 0) {
                if (bonusCode.getText().equals("lukas10")) {
                    amountValue = amountValue * 1.1;
                }
                if (bonusCode.getText().equals("SK10")) {
                    amountValue = amountValue * 10;
                }

                userDAO.addBalance(((int) UserID), amountValue);
                pridanie = true;

                if (mainController != null) {
                    mainController.updateBalance();
                }
                Stage stage = (Stage) amount.getScene().getWindow();
                stage.close();
            }
        }
    }
}
