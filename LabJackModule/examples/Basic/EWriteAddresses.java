/***
Demonstrates how to use the eWriteAddresses (LJM_eWriteAddresses) function.

***/
import java.text.DecimalFormat;
import com.sun.jna.ptr.IntByReference;
import com.labjack.LJM;
import com.labjack.LJMException;

public class EWriteAddresses {

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

			//Setup and call eWriteAddresses to write values to the LabJack.
			int[] aAddresses = { 1000, 55110 };  //DAC0, TEST_UINT16
			int[] aTypes = { LJM.Constants.FLOAT32, LJM.Constants.UINT16 };
			double[] aValues = { 2.5, 12345 };  //2.5 V, 12345
			int numFrames = aAddresses.length;
			IntByReference errAddr = new IntByReference(0);

			LJM.eWriteAddresses(handle, numFrames, aAddresses, aTypes, aValues,
					errAddr);

			System.out.println("\neWriteAddresses: ");
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
