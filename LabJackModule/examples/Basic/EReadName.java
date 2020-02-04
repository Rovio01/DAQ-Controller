/***
Demonstrates how to use the eReadName (LJM_eReadName) function.

***/
import java.text.DecimalFormat;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;
import com.labjack.LJM;
import com.labjack.LJMException;

public class EReadName {

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

			//Setup and call eReadName to read a value from the LabJack.
			String name = "SERIAL_NUMBER";
			DoubleByReference valueRef = new DoubleByReference(0);

			LJM.eReadName(handle, name, valueRef);

			System.out.println("\neReadName result: ");
			System.out.println("    Name = " + name + ", value = " 
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
