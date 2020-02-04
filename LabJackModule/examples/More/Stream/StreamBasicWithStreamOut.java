/***
Demonstrates setting up stream-in and stream-out together, then reading
stream-in values.

Connect a wire from AIN0 to DAC0 to see the effect of stream-out on stream-in
channel 0.

 ***/
import java.io.DataInputStream;
import java.util.Arrays;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;
import com.labjack.LJM;
import com.labjack.LJMException;

public class StreamBasicWithStreamOut {

	public static void main(final String[] args) {
		try {
			final int MAX_REQUESTS = 20;  //The number of eStreamRead calls that will be performed.

			IntByReference handleRef = new IntByReference(0);
			int handle = 0;
			int deviceType = 0;
			String[] aNames;
			double[] aValues;

			//Any device, Any connection, Any identifier
			LJM.openS("ANY", "ANY", "ANY", handleRef);

			//T7 device, Any connection, Any identifier
			//LJM.openS("T7", "ANY", "ANY", handleRef);

			//T4 device, Any connection, Any identifier
			//LJM.openS("T4", "ANY", "ANY", handleRef);

			//Any device, Any connection, Any identifier
			//LJM.open(LJM.Constants.dtANY, LJM.Constants.ctANY, "ANY",
			//		handleRef);

			handle = handleRef.getValue();

			LJMUtilities.printDeviceInfo(handle);

			deviceType = LJMUtilities.getDeviceType(handle);

			//Setup Stream Out
			String[] outNames = new String[] { "DAC0" };
			int numAddressesOut = outNames.length;
			int[] outAddresses = new int[numAddressesOut];
			int[] aTypes = new int[numAddressesOut];  //Dummy
			LJM.namesToAddresses(numAddressesOut, outNames, outAddresses,
					aTypes);

			//Allocate memory for the stream-out buffer
			LJM.eWriteName(handle, "STREAM_OUT0_TARGET", outAddresses[0]);
			LJM.eWriteName(handle, "STREAM_OUT0_BUFFER_SIZE", 512);
			LJM.eWriteName(handle, "STREAM_OUT0_ENABLE", 1);

			//Write values to the stream-out buffer
			LJM.eWriteName(handle, "STREAM_OUT0_LOOP_SIZE", 6);
			LJM.eWriteName(handle, "STREAM_OUT0_BUFFER_F32", 0.0);  //0.0 V
			LJM.eWriteName(handle, "STREAM_OUT0_BUFFER_F32", 1.0);  //1.0 V
			LJM.eWriteName(handle, "STREAM_OUT0_BUFFER_F32", 2.0);  //2.0 V
			LJM.eWriteName(handle, "STREAM_OUT0_BUFFER_F32", 3.0);  //3.0 V
			LJM.eWriteName(handle, "STREAM_OUT0_BUFFER_F32", 4.0);  //4.0 V
			LJM.eWriteName(handle, "STREAM_OUT0_BUFFER_F32", 5.0);  //5.0 V

			LJM.eWriteName(handle, "STREAM_OUT0_SET_LOOP", 1);

			DoubleByReference valueRef = new DoubleByReference(0);
			LJM.eReadName(handle, "STREAM_OUT0_BUFFER_STATUS", valueRef);
			System.out.println("\nSTREAM_OUT0_BUFFER_STATUS = "
					+ valueRef.getValue());

			//Stream Configuration
			double scanRate = 2000;  //Scans per second
			int scansPerRead = 60;  //# scans returned by eStreamRead call

			//Input scan list names to stream.
			String[] aScanListNames = { "AIN0", "AIN1" };
			int numAddressesIn = aScanListNames.length;
			aTypes = new int[numAddressesIn];  //Dummy
			//Scan list addresses to stream. eStreamStart uses Modbus addresses.
			int[] aScanList = new int[numAddressesIn+numAddressesOut];
			//Get the scan list's inputs
			LJM.namesToAddresses(numAddressesIn, aScanListNames, aScanList,
					aTypes);

			//Add the scan list outputs to the end of the scan list.
			//STREAM_OUT0 = 4800, STREAM_OUT1 = 4801, etc.
			aScanList[numAddressesIn] = 4800; //STREAM_OUT0
			//If we had more STREAM_OUTs
			//aScanList[numAddressesIn+1] = 4801;  //STREAM_OUT1
			//aScanList[numAddressesIn+2] = 4802;  //STREAM_OUT2
			//aScanList[numAddressesIn+3] = 4803;  //STREAM_OUT3

			try {
				//When streaming, negative channels and ranges can be
				//configured for individual analog inputs, but the stream has
				//only one settling time and resolution.

				if(deviceType == LJM.Constants.dtT4) {
					//LabJack T4 configuration

					//AIN0 and AIN1 ranges are +/-10 V, stream settling is
					//0 (default) and stream resolution index is 0 (default).
					aNames = new String[] { "AIN0_RANGE", "AIN1_RANGE",
							"STREAM_SETTLING_US", "STREAM_RESOLUTION_INDEX" };
					aValues = new double[] { 10.0, 10.0, 0 };
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
					aValues = new double[] { LJM.Constants.GND, 10.0,
							10.0, 0 };
				}
				//Write the analog inputs' negative channels (when applicable),
				//ranges, stream settling time and stream resolution
				//configuration.
				IntByReference errAddrRef = new IntByReference(-1);
				LJM.eWriteNames(handle, aNames.length, aNames, aValues,
						errAddrRef);

				DataInputStream ins = new DataInputStream(System.in);
				System.out.println("Scan List = "
						+ Arrays.toString(aScanList));

				//Configure and start stream
				DoubleByReference scanRateRef = new DoubleByReference(scanRate);
				LJM.eStreamStart(handle, scansPerRead, aScanList.length,
						aScanList, scanRateRef);
				scanRate = scanRateRef.getValue();
				System.out.println("Stream started with a scan rate of "
						+ scanRate + " Hz.");
				System.out.println("Performing " + MAX_REQUESTS
						+ " stream reads.");

				long totScans = 0;
				//# of samples per eStreamRead is scansPerRead * numAddressesIn
				double[] aData = new double[scansPerRead*numAddressesIn];
				long skippedTotal = 0;
				int skippedCur = 0;
				int deviceScanBacklog = 0;
				int ljmScanBacklog = 0;
				IntByReference deviceScanBacklogRef = new IntByReference(0);
				IntByReference ljmScanBacklogRef = new IntByReference(0);

				long stTime = System.nanoTime();

				for(int loop = 0; loop < MAX_REQUESTS; loop++) {
					LJM.eStreamRead(handle, aData, deviceScanBacklogRef,
							ljmScanBacklogRef);
					deviceScanBacklog = deviceScanBacklogRef.getValue();
					ljmScanBacklog = ljmScanBacklogRef.getValue();

					totScans += scansPerRead;

					//Count the skipped samples which are indicated by -9999
					//values. Missed samples occur after a device's stream
					//buffer overflows and are reported after auto-recover mode
					//ends.
					skippedCur = 0;
					for(int i = 0; i < aData.length; i++) {
						if(aData[i] == -9999.00) {
							skippedCur++;
						}
					}
					skippedTotal += skippedCur;
					System.out.println("\neStreamRead #" + (loop+1));
					for(int j = 0; j < scansPerRead; j++) {
						for(int k = 0; k < numAddressesIn; k++) {
							System.out.print("  " + aScanListNames[k] + " = "
									+ String.format("%.4f",
											aData[j*numAddressesIn + k]) + ",");
						}
						System.out.println("");
					}
					System.out.println("  Skipped Scans = "
							+ skippedCur/numAddressesIn
							+ ", Scan Backlogs: Device = "
							+ deviceScanBacklog + ", LJM = " + ljmScanBacklog);
				}

				long enTime = System.nanoTime();

				System.out.println("\nTotal scans: " + totScans);
				System.out.println("Skipped scans: "
						+ skippedTotal/numAddressesIn);
				double time = (enTime - stTime)/1000000000.0;  //in seconds
				System.out.println("Time taken: " + String.format("%.3f", time)
						+ " seconds");
				System.out.println("LJM Scan Rate: " + scanRate
						+ " scans/second");
				System.out.println("Timed Scan Rate: "
						+ String.format("%.3f", (totScans/time))
						+ " scans/second");
				System.out.println("Sample Rate: "
						+ String.format("%.3f", (totScans*numAddressesIn/time))
						+ " samples/second");
			}
			catch(LJMException le) {
				le.printStackTrace();
			}
			catch(Exception e) {
				e.printStackTrace();
			}

			System.out.println("\nStop Stream");
			LJM.eStreamStop(handle);

			//Close handle
			LJM.close(handle);
		}
		catch (LJMException le) {
			le.printStackTrace();
			LJM.closeAll();
		}
		catch(Exception e) {
			e.printStackTrace();
			LJM.closeAll();
		}
	}
}
