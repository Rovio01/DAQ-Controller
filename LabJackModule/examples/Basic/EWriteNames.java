/***
Demonstrates how to use the eWritesNames (LJM_eWriteNames) function.

***/
import java.text.DecimalFormat;
import com.sun.jna.ptr.IntByReference;
import com.labjack.LJM;
import com.labjack.LJMException;

public class EWriteNames {

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

			//Setup and call eWriteNames to write values to the LabJack.
			String[] aNames = {"DAC0", "TEST_UINT16"};
			double[] aValues = { 2.5, 12345 };  //2.5 V, 12345
			int numFrames = aNames.length;
			IntByReference errAddr = new IntByReference(0);

			LJM.eWriteNames(handle, numFrames, aNames, aValues, errAddr);

			System.out.println("\neWriteNames results: ");
			for(int i = 0; i < numFrames; i++) {
				System.out.println("    Name = " + aNames[i] + ", value = "
						+  new DecimalFormat("#.###").format(aValues[i]));
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
