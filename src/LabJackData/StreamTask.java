package LabJackData;

import com.labjack.LJM;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;
import javafx.concurrent.Task;

import java.io.DataInputStream;
import java.util.ArrayList;

public class StreamTask extends Task<Void> {
    private int handle;
    private boolean stream = true;

    private int scansPerRead;
    private double scanRate;
    private int numAddresses;
    private int[] aScanList;
    private String[] aScanListNames = { "CORE_TIMER", "AIN0", "AIN8", "AIN9" };
    private DataInputStream ins;

    private CSVWriter writer;

    StreamTask(int handle)  {
        this.handle=handle;
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
            int deviceType;

            LJMUtilities.printDeviceInfo(handle);

            deviceType = LJMUtilities.getDeviceType(handle);

            //Stream Configuration

            //# scans returned by eStreamRead call
            scansPerRead = 2000;
            //Scan list names to stream.

            numAddresses = aScanListNames.length;
            int[] aTypes = new int[numAddresses];  //Dummy
            //Scan list addresses to stream. eStreamStart uses Modbus addresses.
            aScanList = new int[numAddresses];
            LJM.namesToAddresses(numAddresses, aScanListNames, aScanList, aTypes);
            scanRate = 10000;  //Scans per second

            try {
                //When streaming, negative channels and ranges can be
                //configured for individual analog inputs, but the stream has
                //only one settling time and resolution.

                String[] aNames;
                double[] aValues;
                if(deviceType == LJM.Constants.dtT4) {
                    //LabJack T4 configuration

                    //AIN0 and AIN1 ranges are +/-10 V, stream settling is
                    //0 (default) and stream resolution index is 0 (default).
                    aNames = new String[] { "AIN0_RANGE", "AIN1_RANGE",
                            "STREAM_SETTLING_US", "STREAM_RESOLUTION_INDEX" };
                    aValues = new double[] { 10.0, 10.0, 0, 0 };
                }
                else {
                    //LabJack T7 and other devices configuration

                    //Ensure triggered stream is disabled.
                    LJM.eWriteName(handle, "STREAM_TRIGGER_INDEX", 0);

                    //Enabling internally-clocked stream.
                    LJM.eWriteName(handle, "STREAM_CLOCK_SOURCE", 0);

                    //All negative channels are single-ended, AIN0 and AIN1
                    //ranges are +/-10 V, stream settling is 0 (default) and
                    //stream resolution index is 0 (default).
                    aNames = new String[] { "AIN_ALL_NEGATIVE_CH",
                            "AIN0_RANGE", "AIN1_RANGE", "STREAM_SETTLING_US",
                            "STREAM_RESOLUTION_INDEX" };
                    aValues = new double[] { LJM.Constants.GND, 10.0, 10.0, 0,
                            0 };
                }
                //Write the analog inputs' negative channels (when applicable),
                //ranges, stream settling time and stream resolution
                //configuration.
                IntByReference errAddressRef = new IntByReference(-1);
                LJM.eWriteNames(handle, aNames.length, aNames, aValues,
                        errAddressRef);

                //System.out.println("\nStarting stream."
                //        + " Press Enter to stop streaming.");
                ins = new DataInputStream(System.in);
                Thread.sleep(1000);  //Delay so users can read message

                //Configure and start stream
                System.out.println("Finished configuring stream");

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
        System.out.println("Beginning stream...");
        System.out.println("Handle: "+handle);
        DoubleByReference scanRateRef = new DoubleByReference(scanRate);
        LJM.eStreamStart(handle, scansPerRead, numAddresses, aScanList,
                scanRateRef);
        System.out.println("Stream successfully started");
        scanRate = scanRateRef.getValue();

        long loop = 0;
        long totScans = 0;
        //# of samples per eStreamRead is scansPerRead * numAddresses
        double[] aData = new double[scansPerRead*numAddresses];
        long skippedTotal = 0;
        int skippedCur = 0;
        int deviceScanBacklog;
        int ljmScanBacklog;
        IntByReference deviceScanBacklogRef = new IntByReference(0);
        IntByReference ljmScanBacklogRef = new IntByReference(0);

        System.out.println("Starting read loop.");
        long stTime = System.nanoTime();

        while(ins.available() <= 0) {
            System.out.println("Main stream loop looping");

                LJM.eStreamRead(handle, aData, deviceScanBacklogRef,
                        ljmScanBacklogRef);
                deviceScanBacklog = deviceScanBacklogRef.getValue();
                ljmScanBacklog = ljmScanBacklogRef.getValue();

                totScans += scansPerRead;

                //Count the skipped samples which are indicated by -9999
                //values. Missed samples occur after a device's stream
                //buffer overflows and are reported after auto-recover mode
                //ends.
                System.out.println(aData.length);
                writer.dataLines.clear();
                for (int line = 0; line < aData.length / numAddresses; line++) {
                    ArrayList<String> dataLine = new ArrayList<>();
                    for (int item = 0; item < numAddresses; item++) {
                        dataLine.add("" + aData[line * numAddresses + item]);
                    }
                    //System.out.println("Writing line:");
                    String[] arrayThing = new String[numAddresses];
                    dataLine.toArray(arrayThing);
                    writer.dataLines.add(arrayThing);
                }
                writer.writeLine();

                skippedTotal += skippedCur;
                loop++;
                System.out.println("\neStreamRead " + loop);
                System.out.print("  First scan out of " + scansPerRead
                        + ": ");
                for (int j = 0; j < numAddresses; j++) {
                    System.out.print(aScanListNames[j] + " = "
                            + String.format("%.4f", aData[j]) + ", ");
                }
                System.out.println("\n  numSkippedScans: "
                        + skippedCur / numAddresses + ", deviceScanBacklog: "
                        + deviceScanBacklog + ", ljmScanBacklog: "
                        + ljmScanBacklog);

                //Stop after the user specified number of loops
                if (!stream){
                    break;
                }

        }

        long enTime = System.nanoTime();

        System.out.println("\nTotal scans: " + totScans);
        System.out.println("Skipped scans: "
                + skippedTotal/numAddresses);
        double time = (enTime - stTime)/1000000000.0;  //in seconds
        System.out.println("Time taken: " + String.format("%.3f", time)
                + " seconds");
        System.out.println("LJM Scan Rate: " + scanRate
                + " scans/second");
        System.out.println("Timed Scan Rate: "
                + String.format("%.3f", (totScans/time))
                + " scans/second");
        System.out.println("Sample Rate: "
                + String.format("%.3f", (totScans*numAddresses/time))
                + " samples/second");
        System.out.println("\nStop Stream");
        LJM.eStreamStop(handle);
    }
}
