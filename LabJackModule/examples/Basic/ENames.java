/***
Demonstrates how to use the eNames (LJM_eNames) function.

***/
import java.text.DecimalFormat;
import com.sun.jna.ptr.IntByReference;
import com.labjack.LJM;
import com.labjack.LJMException;

public class ENames {

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

			//Setup and call eNames to write/read values to/from the LabJack.
			String[] aNames = { "DAC0", "TEST_UINT16", "TEST_UINT16" };
			int[] aWrites = { LJM.Constants.WRITE, LJM.Constants.WRITE,
					LJM.Constants.READ};
			int[] aNumValues = { 1, 1, 1 };
			double[] aValues = { 2.5, 12345, 0 }; //write 2.5 V, 12345, read
			int numFrames = aNames.length;
			IntByReference errAddr = new IntByReference(0);

			LJM.eNames(handle, numFrames, aNames, aWrites, aNumValues, aValues,
					errAddr);

			System.out.println("\neNames results: ");
			for(int i = 0; i < numFrames; i++) {
				System.out.println("    Name = " + aNames[i] + ", write = "
						+ aWrites[i] + ", value = "
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
