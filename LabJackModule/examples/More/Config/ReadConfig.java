/***
Demonstrates how to read configuration settings on a LabJack.

 ***/
import com.sun.jna.ptr.IntByReference;
import com.labjack.LJM;
import com.labjack.LJMException;

public class ReadConfig {

	public static void main(final String[] args) {
		try {
			IntByReference handleRef = new IntByReference(0);
			int handle = 0;
			int deviceType = 0;
			String[] aNames;
			double[] aValues;
			int numFrames = 0;
			IntByReference errAddr;

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

			//Setup and call eReadNames to read config. values from the
			//LabJack.
			if (deviceType == LJM.Constants.dtT4)
			{
				//LabJack T4 configuration to read.
				aNames = new String[] { "PRODUCT_ID", "HARDWARE_VERSION",
					"FIRMWARE_VERSION", "BOOTLOADER_VERSION",
					"SERIAL_NUMBER", "POWER_ETHERNET_DEFAULT",
					"POWER_AIN_DEFAULT", "POWER_LED_DEFAULT" };
			}
			else
			{
				//LabJack T7 and other devices configuration to read.
				aNames = new String[] { "PRODUCT_ID", "HARDWARE_VERSION",
					"FIRMWARE_VERSION", "BOOTLOADER_VERSION",
					"WIFI_VERSION", "SERIAL_NUMBER",
					"POWER_ETHERNET_DEFAULT", "POWER_WIFI_DEFAULT",
					"POWER_AIN_DEFAULT", "POWER_LED_DEFAULT" };
			}
			aValues = new double[aNames.length];
			numFrames = aNames.length;
			errAddr = new IntByReference(-1);
			LJM.eReadNames(handle, numFrames, aNames, aValues, errAddr);

			System.out.println("\nConfiguration settings:");
			for(int i = 0; i < numFrames; i++) {
				System.out.println("    " + aNames[i] + " : "
						+ String.format("%.4f", aValues[i]));
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
