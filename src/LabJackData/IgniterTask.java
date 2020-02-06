package LabJackData;

import javafx.concurrent.Task;

public class IgniterTask extends Task<Void> {

    private int handle;

    IgniterTask(int handle) {
        this.handle=handle;
    }

    @Override
    protected Void call() throws Exception {
        System.out.println("Placeholder ignition sequence");
        return null;
    }
}
