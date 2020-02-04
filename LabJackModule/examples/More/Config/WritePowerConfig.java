/***
Demonstrates how to configure default power settings on a LabJack.

 ***/
import com.sun.jna.ptr.IntByReference;
import com.labjack.LJM;
import com.labjack.LJMException;

public class WritePowerConfig {

	public static void main(final String[] args) {
		try {
			IntByReference handleRef = new IntByReference(0);
			int handle = 0;

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

			//Setup and call eWriteNames to write configuration values to the
			//LabJack.
			String[] aNames = {"POWER_ETHERNET_DEFAULT", "POWER_WIFI_DEFAULT",
					"POWER_AIN_DEFAULT", "POWER_LED_DEFAULT"};
			//Eth. On, WiFi Off, AIN On, LED On
			double[] aValues = { 1, 0, 1, 1 };
			int numFrames = aNames.length;
			IntByReference errAddr = new IntByReference(-1);
			LJM.eWriteNames(handle, numFrames, aNames, aValues, errAddr);

			System.out.println("\nSet configuration settings:");

			for (int i = 0; i < numFrames; i++) {
				System.out.println("    " + aNames[i] + " : "
						+ String.format("%.0f", aValues[i]));
			}

			//Close handle
			LJM.close(handle);
		}
		catch (LJMException le) {
			le.printStackTrace();
			LJM.closeAll();
		}
	}
}
