package LabJackData;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class Controller {
	@FXML
	public LineChart<Number,Number> lineChart;
	@FXML
	private Button armButton, disarmButton, ignitionButton1, ignitionButton2, hideLogButton, startStreamButton, stopStreamButton;
	@FXML
	private Label connectionStatus, armStatus, streamStatus, loadCellData, pt1Data, pt2Data;
	@FXML
	private TextArea logTextArea;

	private LabJackData app;
	ArrayList<double[]> currentData = new ArrayList<>();
	double streamStartTime;

	Color red = Color.RED;
	Color green = Color.LIMEGREEN;


	void addApplication(LabJackData app) {
		this.app = app;
	}

	public void startStreamButtonPress() {
		if (getConnectionStatus()) {
			updateLog("Starting Stream...");
			startStreamButton.setDisable(true);
			stopStreamButton.setDisable(false);
			armButton.setDisable(false);
			streamStatus.setText("Recording");
			streamStatus.setTextFill(green);
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
			streamStatus.setTextFill(red);
			streamStatus.setText("Not Recording");
			if (armButton.isDisabled()) {
				disarmButton.setDisable(true);
				armButton.setDisable(true);
				ignitionButton1.setDisable(true);
				ignitionButton2.setDisable(true);
				armStatus.setText("Disarmed");
				armStatus.setTextFill(red);
				updateLog("Disarming to prevent accidental ignition while not collecting data.");
			} else {
				armButton.setDisable(true);
			}
			app.stopStream();
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
				ignitionButton1.setDisable(false);
				ignitionButton2.setDisable(false);
				armStatus.setText("Armed");
				armStatus.setTextFill(green);
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
		ignitionButton1.setDisable(true);
		ignitionButton2.setDisable(true);
		armStatus.setText("Disarmed");
		armStatus.setTextFill(red);
	}

	public void ignitionButton1Press() {
		// Fetch the result of the users input and react appropriately
		disarmButton.setDisable(true);
		updateLog("Igniting channel 1");
		ignitionButton1.setDisable(true);
		ignitionButton2.setDisable(true);
		app.ignite_1();
	}
	public void ignitionButton2Press() {
		// Fetch the result of the users input and react appropriately
		disarmButton.setDisable(true);
		updateLog("Igniting channel 2");
		ignitionButton1.setDisable(true);
		ignitionButton2.setDisable(true);
		app.ignite_2();
	}

	public void hideLog() {
		if (logTextArea.isVisible()) {
			hideLogButton.setText("Show Log");
			//mainPane.setPrefHeight(500.0);
			logTextArea.setVisible(false);
			//logLabel.setVisible(false);
		} else {
			showLog();
		}
	}

	private void showLog() {
		hideLogButton.setText("Hide Log");
		//mainPane.setPrefHeight(750.0);
		logTextArea.setVisible(true);
		//logLabel.setVisible(true);
	}

	void initialize() {
		armStatus.setTextFill(red);
		streamStatus.setTextFill(red);
		connectionStatus.setTextFill(green);
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
		ignitionButton1.setDisable(true);
		ignitionButton2.setDisable(true);
		armStatus.setText("Disarmed");
		armStatus.setTextFill(Color.web("#FF0000"));
	}

	void updateGraph(ArrayList<double[]> dataPacket, int numAddresses) {
		//System.out.println("Starting graph update");
		//Initialize the graph with data the first time it is run, otherwise append to existing graph data
		//double timeA = System.currentTimeMillis();
		if (currentData==null) {
			currentData = new ArrayList<>();
		}
		for (int i=0; i< dataPacket.size(); i+=10) {
			currentData.add(dataPacket.get(i));
		}
		//System.out.println("Data size: "+currentData.size());
		//Remove any elements that are too old, so that the graph does not get too crowded, values in milliseconds
		double lastTime = currentData.get(currentData.size()-1)[0];
		double timeToKeep = 10000;
		double timeDiff = lastTime-timeToKeep;
		while (currentData.get(0)[0]<timeDiff) {
			//System.out.println("Removing element from graph data");
			currentData.remove(0);
		}
		//System.out.println("Data size: "+currentData.size());
		//Convert the data to a format the graph can use
		ArrayList<XYChart.Series<Number, Number>> graphData = new ArrayList<>();
		//double timeB = System.currentTimeMillis();
		for (int addressIndex = 0; addressIndex < numAddresses; addressIndex++) {
			graphData.add(new XYChart.Series<>());
		}
		//double timeC = System.currentTimeMillis();
		for (int i = 0; i < currentData.size(); i+=10) {
			double[] currentDatum = currentData.get(i);
			for (int addressIndex = 0; addressIndex < numAddresses; addressIndex++) {
				graphData.get(addressIndex).getData().add(new XYChart.Data<>((currentDatum[0]-streamStartTime)/1000, currentDatum[addressIndex + 1]));
			}
		}
		graphData.get(0).setName("Load Cell");
		graphData.get(1).setName("Pressure Transducer 1");
		graphData.get(2).setName("Pressure Transducer 2");
		//double timeD = System.currentTimeMillis();
		lineChart.getData().setAll(graphData);
		//System.out.println("Finished graph update");
		//System.out.println("Time to initialize: "+(timeB-timeA));
		//System.out.println("Time to create series: "+(timeC-timeB));
		//System.out.println("Time to insert data: "+(timeD-timeC));


		loadCellData.setText(String.format("%.4f",currentData.get(currentData.size()-1)[1])+" lbf");
		pt1Data.setText(String.format("%.4f",currentData.get(currentData.size()-1)[2])+" psi");
		pt2Data.setText(String.format("%.4f",currentData.get(currentData.size()-1)[3])+" psi");
	}
}
