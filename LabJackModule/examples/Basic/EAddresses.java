/***
Demonstrates how to use the eAddresses (LJM_eAddresses) function.

***/
import java.text.DecimalFormat;
import com.sun.jna.ptr.IntByReference;
import com.labjack.LJM;
import com.labjack.LJMException;

public class EAddresses {

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

			//Setup and call eAddresses to write/read values to/from the
			//LabJack.
			//DAC0, TEST_UINT16, TEST_UINT16
			int[] aAddresses = { 1000, 55110, 55110 };
			int[] aTypes = { LJM.Constants.FLOAT32, LJM.Constants.UINT16,
					LJM.Constants.UINT16 };
			int[] aWrites = { LJM.Constants.WRITE, LJM.Constants.WRITE,
					LJM.Constants.READ };
			int[] aNumValues = { 1, 1, 1 };
			double[] aValues = { 2.5, 12345, 0 };  //write 2.5 V, 12345, read
			int numFrames = aAddresses.length;
			IntByReference errAddr = new IntByReference(0);

			LJM.eAddresses(handle, numFrames, aAddresses, aTypes, aWrites,
					aNumValues, aValues, errAddr);

			System.out.println("\neAddresses results: ");
			for(int i = 0; i < numFrames; i++) {
				System.out.println("    Address = " + aAddresses[i]
						+ ", type = " + aTypes[i] + ", write = " + aWrites[i]
						+ ", value = "
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
