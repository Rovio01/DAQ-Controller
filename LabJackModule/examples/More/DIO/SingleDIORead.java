/***
Demonstrates how to read a single digital input/output.

 ***/
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;
import com.labjack.LJM;
import com.labjack.LJMException;

public class SingleDIORead {

	public static void main(final String[] args) {
		try {
			IntByReference handleRef = new IntByReference(0);
			int handle = 0;
			int deviceType = 0;
			String name = "";
			DoubleByReference stateRef;

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

			//Setup and call eReadName to read the DIO state.
			if(deviceType == LJM.Constants.dtT4) {
				//Reading FIO4 on the LabJack T4. FIO0-FIO3 are reserved for
				//AIN0-AIN3.
				name = "FIO4";
			}
			else {
				//Reading FIO0 on the LabJack T7 and other devices.
				name = "FIO0";
			}
			stateRef = new DoubleByReference(0);
			LJM.eReadName(handle, name, stateRef);

			System.out.println("\n" + name + " state : "
					+ stateRef.getValue());

			//Close handle
			LJM.close(handle);
		}
		catch (LJMException le) {
			le.printStackTrace();
			LJM.closeAll();
		}
	}
}
