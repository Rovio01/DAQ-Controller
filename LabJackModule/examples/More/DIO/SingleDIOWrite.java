/***
Demonstrates how to set a single digital state on a LabJack.

 ***/
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;
import com.labjack.LJM;
import com.labjack.LJMException;

public class SingleDIOWrite {

	public static void main(final String[] args) {
		try {
			IntByReference handleRef = new IntByReference(0);
			int handle = 0;
			int deviceType = 0;
			String name = "";
			double state = 0;

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

			//Setup and call eWriteName to set the DIO state.
			if(deviceType == LJM.Constants.dtT4) {
				//Setting FIO4 on the LabJack T4. FIO0-FIO3 are reserved for
				//AIN0-AIN3.
				name = "FIO4";

				//If the FIO/EIO line is an analog input, it needs to first be
				//changed to a digital I/O by reading from the line or setting
				//it to digital I/O with the DIO_ANALOG_ENABLE register.

				//Reading from the digital line in case it was previously an
				//analog input.
				DoubleByReference stateRef = new DoubleByReference(0);
				LJM.eReadName(handle, name, stateRef);
			}
			else {
				//Setting FIO0 on the LabJack T7 and other devices.
				name = "FIO0";
			}
			state = 0;  //Output-low = 0, Output-high = 1
			LJM.eWriteName(handle, name, state);

			System.out.println("\nSet " + name + " state : " + state);

			//Close handle
			LJM.close(handle);
		}
		catch (LJMException le) {
			le.printStackTrace();
			LJM.closeAll();
		}
	}
}
