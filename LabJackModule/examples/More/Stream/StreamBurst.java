/***
Demonstrates how to use the StreamBurst function for streaming.

 ***/
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;
import com.labjack.LJM;
import com.labjack.LJMException;

public class StreamBurst {

	public static void main(final String[] args) {
		try {
			IntByReference handleRef = new IntByReference(0);
			int handle = 0;
			int deviceType = 0;
			String[] aNames;
			double[] aValues;

			//Open first found LabJack

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

			//Number of scans to perform
			int numScans = 20000;
			//Scan list names to stream.
			String[] aScanListNames = new String[] { "AIN0", "AIN1" };
			int numAddresses = aScanListNames.length;
			int[] aTypes = new int[numAddresses];  //Dummy
			//Scan list addresses to stream. StreamBurst uses Modbus addresses.
			int[] aScanList = new int[numAddresses];
			LJM.namesToAddresses(numAddresses, aScanListNames, aScanList,
					aTypes);
			double scanRate = 10000;  //Scans per second
			double[] aData = new double[numScans*numAddresses];

			//When streaming, negative channels and ranges can be configured
			//for individual analog inputs, but the stream has only one
			//settling time and resolution.

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

			System.out.println("\nScan list:");
			for(int i = 0; i < numAddresses; i++) {
				System.out.println("  " + aScanListNames[i]);
			}
			System.out.println("Scan rate = " + scanRate + " Hz");
			System.out.println("Sample rate = " + (scanRate * numAddresses)
					+ " Hz");
			System.out.println("Total number of scans = " + numScans);
			System.out.println("Total number of samples = "
					+ (numScans * numAddresses));
			System.out.println("Seconds of samples = " + (numScans / scanRate)
					+ " seconds");

			System.out.println("\nStreaming with StreamBurst...");

			long stTime = System.nanoTime();

			//Stream data using StreamBurst
			DoubleByReference scanRateRef = new DoubleByReference(scanRate);
			LJM.streamBurst(handle, numAddresses, aScanList, scanRateRef,
					numScans, aData);

			long enTime = System.nanoTime();

			System.out.println("Done");

			//Count the skipped samples which are indicated by -9999 values.
			//Missed samples occur after a device's stream buffer overflows
			//and are reported after auto-recover mode ends.
			long skippedTotal = 0;
			for(int i = 0; i < aData.length; i++) {
				if(aData[i] == -9999.00) {
					skippedTotal++;
				}
			}

			System.out.println("\nSkipped scans = "
					+ (skippedTotal / numAddresses));
			double time = (enTime - stTime)/1000000000.0;  //in seconds
			System.out.println("Time taken = " + time + " seconds");

			System.out.println("\nLast scan:");
			for (int i = 0; i < numAddresses; i++) {
				System.out.println("  " + aScanListNames[i] + " = "
						+ aData[(numScans-1) * numAddresses + i]);
			}

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
