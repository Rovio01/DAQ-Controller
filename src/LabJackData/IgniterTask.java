package LabJackData;

import com.labjack.LJM;
import javafx.application.Platform;
import javafx.concurrent.Task;

public class IgniterTask extends Task<Void> {

    private int handle;
    private Controller controller;

    IgniterTask(int handle, Controller controller) {
        this.handle=handle;
        this.controller=controller;
    }

    @Override
    protected Void call() throws Exception {
        System.out.println("Placeholder ignition sequence");
        LJM.eWriteAddress(handle, 2015, 0, 1);
        updateLog("Ignition initiated");
        Thread.sleep(10000);
        LJM.eWriteAddress(handle, 2015, 0, 0);
        updateLog("Ignition finished");
        Platform.runLater(() -> controller.updateAfterFiring());
        return null;
    }

    private void updateLog(String message) {
        Platform.runLater(() -> controller.updateLog(message));
    }
}
