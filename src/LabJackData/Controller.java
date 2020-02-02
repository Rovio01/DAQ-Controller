package LabJackData;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import java.util.Optional;

public class Controller {

    @FXML
    private Pane mainPane;
    @FXML
    private Button armButton, disarmButton, ignitionButton, hideLogButton;
    @FXML
    private Label connectionStatus, armStatus, logLabel;
    @FXML
    private TextArea logTextArea;

    public void armButtonPress(ActionEvent event) {
        if (getConnectionStatus()) {
            updateLog("Arming...");
            armButton.setDisable(true);
            disarmButton.setDisable(false);
            ignitionButton.setDisable(false);
            armStatus.setText("Armed");
            armStatus.setTextFill(Color.web("#00FF00"));

        } else {
            updateLog("No connection detected, check the connection then try again.");
        }
    }

    public void disarmButtonPress(ActionEvent event) {
        updateLog("Disarming...");
        disarmButton.setDisable(true);
        armButton.setDisable(false);
        ignitionButton.setDisable(true);
        armStatus.setText("Disarmed");
        armStatus.setTextFill(Color.web("#FF0000"));
    }

    public void ignitionButtonPress(ActionEvent event) {
        // Alert dialog to prompt for user confirmation on the ignition.
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setContentText("Are you sure you'd like to start the ignition?");
        alert.setTitle("Start Ignition?");

        // Add buttons to the alert dialog.
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType confirmButton = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        alert.getDialogPane().getButtonTypes().add(cancelButton);
        alert.getDialogPane().getButtonTypes().add(confirmButton);

        // Fetch the result of the users input and react appropriately
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == confirmButton) {
            disarmButton.setDisable(true);
            updateLog("Igniting");
            ignitionButton.setDisable(true);
        } else {
            updateLog("Ignition canceled");
        }
    }

    public void hideLog(ActionEvent event) {
        if (logTextArea.isVisible()) {
            hideLogButton.setText("Show Log");
            mainPane.setPrefHeight(500.0);
            logTextArea.setVisible(false);
            logLabel.setVisible(false);
        } else {
            showLog();
        }
    }

    public void showLog() {
        hideLogButton.setText("Hide Log");
        mainPane.setPrefHeight(750.0);
        logTextArea.setVisible(true);
        logLabel.setVisible(true);
    }

    public void updateLog(String logItem) {
        logTextArea.appendText(java.time.LocalTime.now() + ": " + logItem + "\n");
    }

    public void setConnectionStatus(Boolean isConnected) {
        if (isConnected) {
            connectionStatus.setText("Connected");
            connectionStatus.setTextFill(Color.web("#00ff00"));
        } else {
            connectionStatus.setText("Disconnected");
            connectionStatus.setTextFill(Color.web("#ff0000"));
        }
    }


    public boolean getConnectionStatus() {
        return(connectionStatus.getText().equalsIgnoreCase("Connected"));
    }
}
