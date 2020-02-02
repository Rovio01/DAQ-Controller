package LabJackData;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LabJackData extends Application {

    @Override
    public void init() throws Exception {
        System.out.println("Initializing...");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("view.fxml"));
        Scene scene = new Scene(loader.load());
        System.out.println("Initialized successfully");
        primaryStage.setTitle("Data Acquisition Device Monitor");
        primaryStage.setScene(scene);
        primaryStage.show();

        Controller controller = loader.getController();
        controller.setConnectionStatus(true);
    }

    @Override
    public void stop() throws Exception {
        System.out.println("Shutting down...");
    }

}
