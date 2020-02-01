package LabJackData;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import java.util.Optional;

public class Controller {

    @FXML
    private Button armButton, disarmButton, ignitionButton;
    @FXML
    private Label connectionStatus;
    @FXML
    private TextArea logTextArea;

    public void armButtonPress(ActionEvent event) {
        if (getConnectionStatus()) {
            updateLog("Arming...");
            armButton.setDisable(true);
            disarmButton.setDisable(false);
            ignitionButton.setDisable(false);
        } else {
            updateLog("No connection detected, check the connection then try again.");
        }
    }

    public void disarmButtonPress(ActionEvent event) {
        updateLog("Disarming...");
        disarmButton.setDisable(true);
        armButton.setDisable(false);
        ignitionButton.setDisable(true);
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
            updateLog("Igniting");
        } else {
            updateLog("Ignition canceled");
        }
    }

    public void updateLog(String log) {
        logTextArea.appendText(java.time.LocalTime.now() + ": " + log + "\n");
    }

    public void setConnectionStatus(Boolean isConnected) {
        if (isConnected) {
            System.out.println(connectionStatus);
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
