package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.text.Text;
import org.example.possibleoutcome.StatusForOutcomes;
import org.example.sportevent.SportEvent;
import org.example.sportevent.StatusForEvent;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

import static org.jooq.codegen.maven.example.Tables.POSSIBLE_OUTCOMES;
import static org.jooq.codegen.maven.example.Tables.SPORT_EVENTS;

public class EventPreviewController {
    SportEvent sportEvent;
    @FXML
    Text Name;


    @FXML
    CheckBox checkbox1;

    @FXML
    CheckBox checkbox2;

    @FXML
    CheckBox checkbox3;


    @FXML
    void Action(ActionEvent event) throws SQLException {
        Properties config = ConfigReader.loadProperties("config.properties");
        String dbUrl = config.getProperty("db.url");

        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            DSLContext create = DSL.using(connection);


            if (checkbox1.isSelected()) {
                var result = checkbox1.getText();
                create.update(POSSIBLE_OUTCOMES).set(POSSIBLE_OUTCOMES.STATUS, StatusForOutcomes.winning.name()).where(POSSIBLE_OUTCOMES.RESULT_NAME.eq(result)).execute();
            }else {
                if (!checkbox1.getText().isEmpty()){
                    create.update(POSSIBLE_OUTCOMES).set(POSSIBLE_OUTCOMES.STATUS, StatusForOutcomes.loosing.name()).where(POSSIBLE_OUTCOMES.RESULT_NAME.eq(checkbox1.getText())).execute();
                }
            }
            if (checkbox2.isSelected()) {
                var result = checkbox2.getText();
                create.update(POSSIBLE_OUTCOMES).set(POSSIBLE_OUTCOMES.STATUS, StatusForOutcomes.winning.name()).where(POSSIBLE_OUTCOMES.RESULT_NAME.eq(result)).execute();
            }else {
                if (!checkbox2.getText().isEmpty()){
                    create.update(POSSIBLE_OUTCOMES).set(POSSIBLE_OUTCOMES.STATUS, StatusForOutcomes.loosing.name()).where(POSSIBLE_OUTCOMES.RESULT_NAME.eq(checkbox2.getText())).execute();
                }
            }

            if (checkbox3.isSelected()) {
                var result = checkbox3.getText();
                create.update(POSSIBLE_OUTCOMES).set(POSSIBLE_OUTCOMES.STATUS, StatusForOutcomes.winning.name()).where(POSSIBLE_OUTCOMES.RESULT_NAME.eq(result)).execute();
            }else {
                if (!checkbox3.getText().isEmpty()){
                    System.out.println("mal by byt prazdny");
                    create.update(POSSIBLE_OUTCOMES).set(POSSIBLE_OUTCOMES.STATUS, StatusForOutcomes.loosing.name()).where(POSSIBLE_OUTCOMES.RESULT_NAME.eq(checkbox3.getText())).execute();
                }
            }
            create.update(SPORT_EVENTS).set(SPORT_EVENTS.STATUS, StatusForEvent.finished.name()).where(SPORT_EVENTS.EVENT_ID.eq((int) sportEvent.getEventId())).execute();


        }
    }
}
