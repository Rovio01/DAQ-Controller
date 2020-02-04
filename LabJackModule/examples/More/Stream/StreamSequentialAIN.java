/***
Demonstrates how to stream a range of sequential analog inputs using the
eStream functions. Useful when streaming many analog inputs. AIN channel scan
list is FIRST_AIN_CHANNEL to FIRST_AIN_CHANNEL + NUMBER_OF_AINS - 1.

 ***/
import java.io.DataInputStream;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.DoubleByReference;
import com.labjack.LJM;
import com.labjack.LJMException;

public class StreamSequentialAIN {

	public static void main(final String[] args) {
		try {
			final int FIRST_AIN_CHANNEL = 0;  //0 = AIN0
			final int NUMBER_OF_AINS = 8;

			IntByReference handleRef = new IntByReference(0);
			int handle = 0;
			int deviceType = 0;
			String[] aNames;
			double[] aValues;
			IntByReference errAddrRef = new IntByReference(-1);

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

			//When streaming, negative channels and ranges can be
			//configured for individual analog inputs, but the stream has
			//only one settling time and resolution.

			if(LJMUtilities.getDeviceType(handle) == LJM.Constants.dtT4) {
				//T4 configuration

				//Configure the channels to analog input or digital I/O
				//Update all digital I/O channels.
				//b1 = Ignored. b0 = Affected.
				double dioInhibit = (double)0x00000;  //b00000000000000000000
				//Set AIN0-AIN3 and AIN FIRST_AIN_CHANNEL to
				//FIRST_AIN_CHANNEL+NUMBER_OF_AINS-1 as analog inputs (b1),
				//the rest as digital I/O (b0).
				double dioAnalogEnable = ((int)(Math.pow(2, NUMBER_OF_AINS) - 1)
						<< FIRST_AIN_CHANNEL) | 0x0000F;
				aNames = new String[] { "DIO_INHIBIT", "DIO_ANALOG_ENABLE" };
				aValues = new double[] { dioInhibit, dioAnalogEnable };
				LJM.eWriteNames(handle, aNames.length, aNames, aValues,
						errAddrRef);

				//Configure the analog input ranges.
				double rangeAINHV = 10.0;  //HV channels range (AIN0-AIN3)
				double rangeAINLV = 2.5;  //LV channels range (AIN4+)
				aNames = new String[NUMBER_OF_AINS];
				aValues = new double[NUMBER_OF_AINS];
				for(int i = 0; i < NUMBER_OF_AINS; i++) {
					aNames[i] = "AIN" + (FIRST_AIN_CHANNEL + i) + "_RANGE";
					aValues[i] = ((FIRST_AIN_CHANNEL + i) < 4)
							? rangeAINHV : rangeAINLV;
				}
				LJM.eWriteNames(handle, aNames.length, aNames, aValues,
						errAddrRef);

				//Configure the stream settling times and stream resolution
				//index.
				aNames = new String[] { "STREAM_SETTLING_US",
						"STREAM_RESOLUTION_INDEX" };
				//0 (default), 0 (default)
				aValues = new double[] { 0, 0 };
				LJM.eWriteNames(handle, aNames.length, aNames, aValues,
						errAddrRef);
			}
			else {
				//T7 and other devices configuration

				//Ensure triggered stream is disabled.
				LJM.eWriteName(handle, "STREAM_TRIGGER_INDEX", 0);

				//Enabling internally-clocked stream.
				LJM.eWriteName(handle, "STREAM_CLOCK_SOURCE", 0);

				//Configure the analog input negative channels, ranges,
				//stream settling times and stream resolution index.
				aNames = new String[] { "AIN_ALL_NEGATIVE_CH", "AIN_ALL_RANGE",
						"STREAM_SETTLING_US", "STREAM_RESOLUTION_INDEX" };
				//Single-ended, +/-10V, 0 (default), 0 (default)
				aValues = new double[] { LJM.Constants.GND, 10.0,
						0, 0 };
				LJM.eWriteNames(handle, aNames.length, aNames, aValues,
						errAddrRef);
			}

			//Stream Configuration
			int scansPerRead = 1000;  //# scans returned by eStreamRead call
			//Scan list names to stream. AIN(FIRST_AIN_CHANNEL) to
			//AIN(NUMBER_OF_AINS-1).
			String[] aScanListNames = new String[NUMBER_OF_AINS];
			for(int i = 0; i < aScanListNames.length; i++) {
				aScanListNames[i] = "AIN" + String.valueOf(FIRST_AIN_CHANNEL+i);
			}
			int numAddresses = aScanListNames.length;
			int[] aTypes = new int[numAddresses];  //Dummy
			//Scan list addresses to stream.
			int[] aScanList = new int[numAddresses];
			LJM.namesToAddresses(numAddresses, aScanListNames, aScanList,
					aTypes);
			double scanRate = 1000;  //Scans per second

			try {
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
					System.out.print("  First scan out of "
							+ scansPerRead + ": ");
					for(int j = 0; j < numAddresses; j++) {
						System.out.print(aScanListNames[j] + " = "
								+ String.format("%.4f", aData[j]) + ", ");
					}
					System.out.println("\n  numSkippedScans: "
							+ skippedCur/numAddresses + ", deviceScanBacklog: "
							+ deviceScanBacklog + ", ljmScanBacklog: "
							+ ljmScanBacklog);
					
					//Stop after the user specified number of loops.
					if(!infLoop && loop >= loopAmount) {
						break;
					}
				}

				long enTime = System.nanoTime();

				System.out.println("\nTotal scans: " + totScans);
				System.out.println("Skipped scans: "
						+ skippedTotal/numAddresses);
				double time = (enTime - stTime)/1000000000.0; //in seconds
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
