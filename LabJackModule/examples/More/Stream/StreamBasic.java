/***
Demonstrates how to stream using the eStream functions.

 ***/
import java.io.DataInputStream;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;
import com.labjack.LJM;
import com.labjack.LJMException;

public class StreamBasic {

	public static void main(final String[] args) {
		try {
			IntByReference handleRef = new IntByReference(0);
			int handle = 0;
			int deviceType = 0;
			String[] aNames;
			double[] aValues;

			//If arguments are passed to the application, the first argument
			//specifies how many times to loop. If an argument is not passed,
			//will loop until the Enter key is pressed.
			long loopAmount = 0;
			boolean infLoop = true;
			if(args.length > 0) {
				try {
					loopAmount = new Double(args[0]).longValue();
					infLoop = false;
				}
				catch(Exception ex) {
					throw new Exception("Invalid first argument \"" + args[0]
							+ "\". This specifies how many times to loop and "
							+ "needs to be a number.");
				}
			}

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

			//Stream Configuration

			//# scans returned by eStreamRead call
			int scansPerRead = 1000;
			//Scan list names to stream.
			String[] aScanListNames = { "AIN0", "AIN1" };
			int numAddresses = aScanListNames.length;
			int[] aTypes = new int[numAddresses];  //Dummy
			//Scan list addresses to stream. eStreamStart uses Modbus addresses.
			int[] aScanList = new int[numAddresses];
			LJM.namesToAddresses(numAddresses, aScanListNames, aScanList, aTypes);
			double scanRate = 1000;  //Scans per second

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
				IntByReference errAddrRef = new IntByReference(-1);
				LJM.eWriteNames(handle, aNames.length, aNames, aValues,
						errAddrRef);

				System.out.println("\nStarting stream."
						+ " Press Enter to stop streaming.");
				DataInputStream ins = new DataInputStream(System.in);
				Thread.sleep(1000);  //Delay so users can read message

				//Configure and start stream
				DoubleByReference scanRateRef = new DoubleByReference(scanRate);
				LJM.eStreamStart(handle, scansPerRead, numAddresses, aScanList,
						scanRateRef);
				scanRate = scanRateRef.getValue();

				long loop = 0;
				long totScans = 0;
				//# of samples per eStreamRead is scansPerRead * numAddresses
				double[] aData = new double[scansPerRead*numAddresses];
				long skippedTotal = 0;
				int skippedCur = 0;
				int deviceScanBacklog = 0;
				int ljmScanBacklog = 0;
				IntByReference deviceScanBacklogRef = new IntByReference(0);
				IntByReference ljmScanBacklogRef = new IntByReference(0);

				System.out.println("Starting read loop.");
				long stTime = System.nanoTime();

				while(ins.available() <= 0) {
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
					loop++;
					System.out.println("\neStreamRead " + loop);
					System.out.print("  First scan out of " + scansPerRead
							+ ": ");
					for(int j = 0; j < numAddresses; j++) {
						System.out.print(aScanListNames[j] + " = "
								+ String.format("%.4f", aData[j]) + ", ");
					}
					System.out.println("\n  numSkippedScans: "
							+ skippedCur/numAddresses + ", deviceScanBacklog: "
							+ deviceScanBacklog + ", ljmScanBacklog: "
							+ ljmScanBacklog);

					//Stop after the user specified number of loops
					if(!infLoop && loop >= loopAmount) {
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
