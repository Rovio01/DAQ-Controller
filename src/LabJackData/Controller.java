package LabJackData;

import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class Controller {
	@FXML
	public AreaChart areaChart;
	@FXML
	private Pane mainPane;
	@FXML
	private Button armButton, disarmButton, ignitionButton, hideLogButton, startStreamButton, stopStreamButton;
	@FXML
	private Label connectionStatus, armStatus, logLabel;
	@FXML
	private TextArea logTextArea;

	private LabJackData app;
	private ArrayList<double[]> currentData = new ArrayList<>();


	void addApplication(LabJackData app) {
		this.app = app;
	}

	public void startStreamButtonPress() {
		if (getConnectionStatus()) {
			updateLog("Starting Stream...");
			startStreamButton.setDisable(true);
			stopStreamButton.setDisable(false);
			app.startStream();
		} else {
			updateLog("No connection detected, check the connection then try again.");
		}
	}

	public void stopStreamButtonPress() {
		if (getConnectionStatus()) {
			updateLog("Stopping Stream...");
			startStreamButton.setDisable(false);
			stopStreamButton.setDisable(true);
			app.stopStream();
			if (armButton.isDisabled()) {
				disarmButton.setDisable(true);
				armButton.setDisable(false);
				ignitionButton.setDisable(true);
				armStatus.setText("Disarmed");
				armStatus.setTextFill(Color.web("#FF0000"));
				updateLog("Disarming to prevent accidental ignition while not collecting data.");
			}
		} else {
			updateLog("No connection detected, check the connection then try again.");
		}
	}

	public void armButtonPress() {
		if (getConnectionStatus()) {
			if (app.streaming) {
				updateLog("Arming...");
				armButton.setDisable(true);
				disarmButton.setDisable(false);
				ignitionButton.setDisable(false);
				armStatus.setText("Armed");
				armStatus.setTextFill(Color.web("#00FF00"));
			} else {
				updateLog("Arming failed. Please begin streaming to ensure no data is lost.");
			}
		} else {
			updateLog("No connection detected, check the connection then try again.");
		}
	}

	public void disarmButtonPress() {
		updateLog("Disarming...");
		disarmButton.setDisable(true);
		armButton.setDisable(false);
		ignitionButton.setDisable(true);
		armStatus.setText("Disarmed");
		armStatus.setTextFill(Color.web("#FF0000"));
	}

	public void ignitionButtonPress() {
		// Fetch the result of the users input and react appropriately
		disarmButton.setDisable(true);
		updateLog("Igniting");
		ignitionButton.setDisable(true);
		app.ignite();
	}

	public void hideLog() {
		if (logTextArea.isVisible()) {
			hideLogButton.setText("Show Log");
			mainPane.setPrefHeight(500.0);
			logTextArea.setVisible(false);
			logLabel.setVisible(false);
		} else {
			showLog();
		}
	}

	private void showLog() {
		hideLogButton.setText("Hide Log");
		mainPane.setPrefHeight(750.0);
		logTextArea.setVisible(true);
		logLabel.setVisible(true);
	}

	void updateLog(String logItem) {
		logTextArea.appendText(java.time.LocalTime.now() + ": " + logItem + "\n");
	}

	void setConnectionStatus(Boolean isConnected) {
		if (isConnected) {
			connectionStatus.setText("Connected");
			connectionStatus.setTextFill(Color.web("#00ff00"));
		} else {
			connectionStatus.setText("Disconnected");
			connectionStatus.setTextFill(Color.web("#ff0000"));
		}
	}

	private boolean getConnectionStatus() {
		return (connectionStatus.getText().equalsIgnoreCase("Connected"));
	}

	void updateAfterFiring() {
		updateLog("Disarming...");
		disarmButton.setDisable(true);
		armButton.setDisable(false);
		ignitionButton.setDisable(true);
		armStatus.setText("Disarmed");
		armStatus.setTextFill(Color.web("#FF0000"));
	}

	void updateGraph(ArrayList<double[]> dataPacket, int numAddresses) {
		System.out.println("Starting graph update");
		//Initialize the graph with data the first time it is run, otherwise append to existing graph data
		if (currentData!=null) {
	    	currentData.addAll(dataPacket);
		} else {
			currentData = dataPacket;
		}
		//Remove any elements that are too old, so that the graph does not get too crowded, values in milliseconds
		double lastTime = currentData.get(currentData.size()-1)[0];
		double timeToKeep = 100000;
		while (currentData.get(0)[0]<lastTime-timeToKeep) {
			currentData.remove(0);
		}
		//Convert the data to a format the graph can use
		ArrayList<XYChart.Series<Double, Double>> graphData = new ArrayList<>();
		for (int addressIndex = 0; addressIndex < numAddresses; addressIndex++) {
			graphData.add(new XYChart.Series<>());
		}
		for (double[] currentDatum : currentData) {
			for (int addressIndex = 0; addressIndex < numAddresses; addressIndex++) {
				graphData.get(addressIndex).getData().add(new XYChart.Data<>(currentDatum[0], currentDatum[addressIndex + 1]));
			}
		}
		System.out.println("Finished graph update");
	}
}
