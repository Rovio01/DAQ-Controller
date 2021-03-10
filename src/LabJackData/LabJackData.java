package LabJackData;

import com.labjack.LJM;
import com.sun.jna.ptr.IntByReference;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LabJackData extends Application {
	private int handle;
	private StreamTask streamer;
	boolean streaming = false;
	Controller controller;

	@Override
	public void start(Stage primaryStage) throws Exception {
		System.out.println("Initializing...");
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("view.fxml"));
		Scene scene = new Scene(loader.load());
		controller = loader.getController();

		// Check if there was no error connection
		if (initializeDAQ()) {
			controller.setConnectionStatus(true);
			LJM.eWriteAddress(handle, 2015, 0, 0);
		} else {
			controller.setConnectionStatus(false);
		}

		controller.addApplication(this);

		System.out.println("Initialized successfully");
		primaryStage.setTitle("Data Acquisition Device Monitor");
		primaryStage.setScene(scene);
		primaryStage.show();
		controller.initialize();
	}

	@Override
	public void stop() {
		System.out.println("Shutting down...");
		stopStream();
		LJM.close(handle);
	}

	private boolean initializeDAQ() {
		IntByReference handleRef = new IntByReference(0);
		int status = LJM.openS("ANY", "ANY", "ANY", handleRef);
		handle = handleRef.getValue();
		System.out.println("handle: " + handle);
		System.out.println("status: " + status);
		return status == 0;
	}

	void startStream() {
		streamer = new StreamTask(handle, controller);
		new Thread(streamer).start();
		streaming = true;
	}

	void stopStream() {
		if (streamer != null) {
			streamer.stopStream();
		}
		streaming = false;
	}

	void ignite() {
		IgniterTask igniter = new IgniterTask(handle, controller);
		new Thread(igniter).start();
	}
}
