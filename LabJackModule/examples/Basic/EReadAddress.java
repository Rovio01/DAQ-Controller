/***
Demonstrates how to use the eReadAddress (LJM_eReadAddress) function.

***/
import java.text.DecimalFormat;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;
import com.labjack.LJM;
import com.labjack.LJMException;

public class EReadAddress {

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

			//Setup and call eReadAddress to read a value from the LabJack.
			int address = 60028;  //SERIAL_NUMBER
			int type = LJM.Constants.UINT32;
			DoubleByReference valueRef = new DoubleByReference(0);

			LJM.eReadAddress(handle, address, type, valueRef);

			System.out.println("\neReadAddress result: ");
			System.out.println("    Address = " + address + ", type = " + type
					+ ", value = "
					+  new DecimalFormat("#.#").format(valueRef.getValue()));

			//Close handle
			LJM.close(handle);
		}
		catch (LJMException le) {
			le.printStackTrace();
			LJM.closeAll();
		}
	}
}
