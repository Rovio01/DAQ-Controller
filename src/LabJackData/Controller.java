package LabJackData;

import com.sun.jna.ptr.IntByReference;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.time.LocalTime;
import com.labjack.LJM;
import com.labjack.LJMException;

public class Controller {
    @FXML
    private Pane mainPane;
    @FXML
    private Button armButton, disarmButton, ignitionButton, hideLogButton;
    @FXML
    private Label connectionStatus, armStatus, logLabel;
    @FXML
    private TextArea logTextArea;
    @FXML
    private LineChart<String, Double> dataGraph;
    private XYChart.Series<String, Double> series = new XYChart.Series<>();
    private ScheduledExecutorService scheduledExecutorService;


    public void armButtonPress(ActionEvent event) {
        if (getConnectionStatus()) {
            updateLog("Arming...");
            armButton.setDisable(true);
            disarmButton.setDisable(false);
            ignitionButton.setDisable(false);
            armStatus.setText("Armed");
            armStatus.setTextFill(Color.web("#00FF00"));
            LJM.openS("ANY", "ANY", "ANY", new IntByReference(0));
            setupGraph();
            addToGraph();
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
        // Fetch the result of the users input and react appropriately
        disarmButton.setDisable(true);
        updateLog("Igniting");
        ignitionButton.setDisable(true);
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

    public void setupStreamOut() {
    }

    public double addData() {
    }

    public void setupGraph() {
        dataGraph.setTitle("Chart Test");
        series.setName("Data");
        dataGraph.setCreateSymbols(false);
        dataGraph.setLegendVisible(false); }

    public void addToGraph() {

        dataGraph.getData().add(series);
    }
}
