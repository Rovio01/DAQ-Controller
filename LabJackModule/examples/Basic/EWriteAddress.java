/***
Demonstrates how to use the eWriteAddress (LJM_eWriteAddress) function.

***/
import java.text.DecimalFormat;
import com.sun.jna.ptr.IntByReference;
import com.labjack.LJM;
import com.labjack.LJMException;

public class EWriteAddress {

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

			//Setup and call eWriteAddress to write a value to the LabJack.
			int address = 1000;  //DAC0
			int type = LJM.Constants.FLOAT32;
			double value = 2.5;  //2.5 V

			LJM.eWriteAddress(handle, address, type, value);

			System.out.println("\neWriteAddress: ");
			System.out.println("    Address = " + address + ", type = " + type
					+ ", value = "
					+  new DecimalFormat("#.#").format(value));

			//Close handle
			LJM.close(handle);
		}
		catch (LJMException le) {
			le.printStackTrace();
			LJM.closeAll();
		}
	}
}
