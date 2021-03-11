package LabJackData;

import com.labjack.LJM;
import javafx.application.Platform;
import javafx.concurrent.Task;

public class IgniterTask extends Task<Void> {

	private int handle, address;
	private Controller controller;

	IgniterTask(int handle, Controller controller, int address) {
		this.handle = handle;
		this.controller = controller;
		this.address = address;
	}

	@Override
	protected Void call() throws Exception {
		System.out.println("Ignition sequence");
		LJM.eWriteAddress(handle, address, 0, 1);
		updateLog("Ignition initiated");
		Thread.sleep(10000);
		LJM.eWriteAddress(handle, address, 0, 0);
		updateLog("Ignition finished");
		Platform.runLater(() -> controller.updateAfterFiring());
		return null;
	}

	private void updateLog(String message) {
		Platform.runLater(() -> controller.updateLog(message));
	}
}
