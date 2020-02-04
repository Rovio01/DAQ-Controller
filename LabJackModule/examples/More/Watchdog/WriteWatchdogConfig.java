/***
Demonstrates how to configure the Watchdog on a LabJack.

 ***/
import com.sun.jna.ptr.IntByReference;
import com.labjack.LJM;
import com.labjack.LJMException;

public class WriteWatchdogConfig {

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

			//Setup and call eWriteNames to configure the Watchdog on a
			//LabJack. Disable the Watchdog first before any other
			//configuration.
			String[] aNames = { "WATCHDOG_ENABLE_DEFAULT",
					"WATCHDOG_ADVANCED_DEFAULT",
					"WATCHDOG_TIMEOUT_S_DEFAULT",
					"WATCHDOG_STARTUP_DELAY_S_DEFAULT",
					"WATCHDOG_STRICT_ENABLE_DEFAULT",
					"WATCHDOG_STRICT_KEY_DEFAULT",
					"WATCHDOG_RESET_ENABLE_DEFAULT",
					"WATCHDOG_DIO_ENABLE_DEFAULT",
					"WATCHDOG_DIO_STATE_DEFAULT",
					"WATCHDOG_DIO_DIRECTION_DEFAULT",
					"WATCHDOG_DIO_INHIBIT_DEFAULT",
					"WATCHDOG_DAC0_ENABLE_DEFAULT",
					"WATCHDOG_DAC0_DEFAULT",
					"WATCHDOG_DAC1_ENABLE_DEFAULT",
					"WATCHDOG_DAC1_DEFAULT",
					"WATCHDOG_ENABLE_DEFAULT" };
			double[] aValues = { 0,
					0,
					20,
					0,
					0,
					0,
					1,
					0,
					0,
					0,
					0,
					0,
					0,
					0,
					0,
					0};  //Set WATCHDOG_ENABLE_DEFAULT to 1 to enable
			int numFrames = aNames.length;
			IntByReference errAddrRef = new IntByReference(-1);
			LJM.eWriteNames(handle, numFrames, aNames, aValues, errAddrRef);

			System.out.println("\nSet Watchdog configuration:");
			for(int i = 0; i < numFrames; i++) {
				System.out.println("    " + aNames[i] +" : " + aValues[i]);
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
