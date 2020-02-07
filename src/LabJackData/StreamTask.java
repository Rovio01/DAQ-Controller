package LabJackData;

import com.labjack.LJM;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.DataInputStream;
import java.util.ArrayList;

public class StreamTask extends Task<Void> {
    private int handle;
    private boolean stream = true;

    //Stream configuration variables
    //Intellij warnings suppressed since I want to have the important stuff at the top of the class
    @SuppressWarnings("FieldCanBeLocal")
    private int scansPerRead = 2000;
    private double scanRate = 10000;
    private int numAddresses;
    private int[] aScanList;
    private String[] aScanListNames = { "CORE_TIMER", "AIN0", "AIN8", "AIN9" };
    private DataInputStream ins;
    private Controller controller;

    private CSVWriter writer;

    StreamTask(int handle, Controller controller)  {
        this.handle=handle;
        this.controller=controller;
    }

    void stopStream() {
        System.out.println("Stopping stream");
        stream = false;
    }

    private void startStream(){
        stream = true;
    }

    @Override
    protected Void call() throws Exception {
        System.out.println("Starting stream");
        configureFile();
        configureStream();
        startStream();
        stream();
        return null;
    }

    private void configureFile() throws Exception{
        writer = new CSVWriter();
    }

    private void configureStream() {
        System.out.println("Configuring stream");
        try {
            numAddresses = aScanListNames.length;
            int[] aTypes = new int[numAddresses];  //Dummy
            //Scan list addresses to stream. eStreamStart uses Modbus addresses.
            aScanList = new int[numAddresses];
            LJM.namesToAddresses(numAddresses, aScanListNames, aScanList, aTypes);

            try {
                //When streaming, negative channels and ranges can be
                //configured for individual analog inputs, but the stream has
                //only one settling time and resolution.

                String[] aNames;
                double[] aValues;
                //LabJack T4 configuration

                //AIN0 and AIN1 ranges are +/-10 V, stream settling is
                //0 (default) and stream resolution index is 0 (default).
                aNames = new String[] { "AIN0_RANGE", "AIN1_RANGE",
                        "STREAM_SETTLING_US", "STREAM_RESOLUTION_INDEX" };
                aValues = new double[] { 10.0, 10.0, 0, 0 };

                //Write the analog inputs' negative channels (when applicable),
                //ranges, stream settling time and stream resolution
                //configuration.
                IntByReference errAddressRef = new IntByReference(-1);
                LJM.eWriteNames(handle, aNames.length, aNames, aValues,
                        errAddressRef);
                ins = new DataInputStream(System.in);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            LJM.closeAll();
        }
    }

    private void stream() throws Exception {
        DoubleByReference scanRateRef = new DoubleByReference(scanRate);
        LJM.eStreamStart(handle, scansPerRead, numAddresses, aScanList,
                scanRateRef);
        updateLog("Stream started");
        scanRate = scanRateRef.getValue();
        double[] aData = new double[scansPerRead*numAddresses];
        IntByReference deviceScanBacklogRef = new IntByReference(0);
        IntByReference ljmScanBacklogRef = new IntByReference(0);
        long stTime = System.currentTimeMillis();

        while (stream) {
            LJM.eStreamRead(handle, aData, deviceScanBacklogRef,
                    ljmScanBacklogRef);
            System.out.println(aData.length);
            writer.dataLines.clear();
            for (int line = 0; line < aData.length / numAddresses; line++) {
                ArrayList<String> dataLine = new ArrayList<>();
                for (int item = 0; item < numAddresses; item++) {
                    dataLine.add("" + aData[line * numAddresses + item]);
                }
                String[] arrayThing = new String[numAddresses];
                dataLine.toArray(arrayThing);
                writer.dataLines.add(arrayThing);
            }
            writer.writeLine();
            if (deviceScanBacklogRef.getValue() > 5000) {
                updateLog("Device scan backlog is growing unexpectedly fast, backlog is " + deviceScanBacklogRef.getValue());
            }
            if (ljmScanBacklogRef.getValue() > 5000) {
                updateLog("LJM scan backlog is growing unexpectedly fast, backlog is " + ljmScanBacklogRef.getValue());
            }
            Platform.runLater(() -> controller.updateGraph(aData));
        }
        LJM.eStreamStop(handle);
        updateLog("Stream stopped");
    }

    private void updateLog(String message) {
        Platform.runLater(() -> controller.updateLog(message));
    }
}
