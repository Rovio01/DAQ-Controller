package LabJackData;

import com.labjack.LJM;
import com.labjack.LJMException;
import com.sun.jna.ptr.IntByReference;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LabJackData extends Application {
    static int handle = 0;

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("Initializing...");
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("view.fxml"));
        Scene scene = new Scene(loader.load());
        Controller controller = loader.getController();

        // Check if there was no error connection
        if (initializeDAQ()) {
            controller.setConnectionStatus(true);
        } else {
            controller.setConnectionStatus(false);
        }

        System.out.println("Initialized successfully");
        primaryStage.setTitle("Data Acquisition Device Monitor");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        System.out.println("Shutting down...");
    }

    public boolean initializeDAQ() {
        IntByReference handleRef = new IntByReference(0);
        int status = LJM.openS("ANY", "ANY", "ANY", handleRef);
        handle = handleRef.getValue();

        return status == 0;
    }
}
