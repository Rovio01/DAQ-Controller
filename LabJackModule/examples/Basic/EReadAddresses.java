/***
Demonstrates how to use the eReadAddresses (LJM_eReadAddresses) function.

***/
import java.text.DecimalFormat;
import com.sun.jna.ptr.IntByReference;
import com.labjack.LJM;
import com.labjack.LJMException;

public class EReadAddresses {

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

			//Setup and call eReadAddresses to read values from the LabJack.
			//SERIAL_NUMBER, PRODUCT_ID, FIRMWARE_VERSION
			int[] aAddresses = { 60028, 60000, 60004 };
			int[] aTypes = { LJM.Constants.UINT32, LJM.Constants.FLOAT32,
					LJM.Constants.FLOAT32 };
			double[] aValues = { 0, 0, 0 };
			int numFrames = aAddresses.length;
			IntByReference errAddr = new IntByReference(0);

			LJM.eReadAddresses(handle, numFrames, aAddresses, aTypes, aValues,
					errAddr);

			System.out.println("\neReadAddresses results: ");
			for(int i = 0; i < numFrames; i++) {
				System.out.println("    Address = " + aAddresses[i]
						+ ", type = "  + aTypes[i] + ", value = "
						+  new DecimalFormat("#.####").format(aValues[i]));
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
