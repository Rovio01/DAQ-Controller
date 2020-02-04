/***
Demonstrates configuring and reading a single analog input (AIN) with a LabJack.

 ***/
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;
import com.labjack.LJM;
import com.labjack.LJMException;

public class SingleAINWithConfig {

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

			//Setup and call eWriteNames to configure the AIN on the LabJack.
			if(deviceType == LJM.Constants.dtT4) {
				//LabJack T4 configuration

				//AIN0:
				//    Range = +/-10 V. Only AIN0-AIN3 support the +/-10 V range.
				//    Resolution index = 0 (default).
				//    Settling = 0 (auto)
				aNames = new String[] { "AIN0_RANGE", "AIN0_RESOLUTION_INDEX",
						"AIN0_SETTLING_US" };
				aValues = new double[] { 10, 0, 0 };
			}
			else {
				//LabJack T7 and other devices configuration

				//AIN0:
				//    Negative Channel = 199 (Single-ended)
				//    Range = +/-10 V
				//    Resolution index = 0 (default).
				//    Settling = 0 (auto)
				aNames = new String[] { "AIN0_NEGATIVE_CH", "AIN0_RANGE",
						"AIN0_RESOLUTION_INDEX", "AIN0_SETTLING_US" };
				aValues = new double[] { 199, 10, 0, 0 };
			}
			int numFrames = aNames.length;
			IntByReference errAddr = new IntByReference(-1);
			LJM.eWriteNames(handle, numFrames, aNames, aValues, errAddr);

			System.out.println("\nSet configuration:");
			for (int i = 0; i < numFrames; i++) {
				System.out.println("    " + aNames[i] + " : " + aValues[i]
						+ " ");
			}

			//Setup and call eReadName to read an AIN from the LabJack.
			String name = "AIN0";
			DoubleByReference valueRef = new DoubleByReference(0);
			LJM.eReadName(handle, name, valueRef);

			System.out.println("\n" + name + " reading : "
					+ String.format("%.4f", valueRef.getValue()) + " V");

			//Close handle
			LJM.close(handle);
		} catch (LJMException le) {
			le.printStackTrace();
			LJM.closeAll();
		}
	}
}
