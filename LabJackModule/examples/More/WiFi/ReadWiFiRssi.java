/***
Demonstrates how to read the WiFi RSSI from a LabJack.

***/
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;
import com.labjack.LJM;
import com.labjack.LJMException;

public class ReadWiFiRssi {

	public static void main(final String[] args) {
		try {
			IntByReference handleRef = new IntByReference(0);
			int handle = 0;

			//Open first found LabJack

			//Any device, Any connection, Any identifier
			LJM.openS("ANY", "ANY", "ANY", handleRef);

			//T7 device, Any connection, Any identifier
			//LJM.openS("T7", "ANY", "ANY", handleRef);

			//Any device, Any connection, Any identifier
			//LJM.open(LJM.Constants.dtANY, LJM.Constants.ctANY, "ANY",
			//		handleRef);

			handle = handleRef.getValue();
			
			LJMUtilities.printDeviceInfo(handle);

			if(LJMUtilities.getDeviceType(handle) == LJM.Constants.dtT4) {
				System.out.println("\nThe LabJack T4 does not support WiFi.");
				LJM.close(handle);
				return;
			}

			//Setup and call eReadName to read the WiFi RSSI from the
			//LabJack.
			String name = "WIFI_RSSI";
			DoubleByReference valueRef = new DoubleByReference(0);
			LJM.eReadName(handle, name, valueRef);

			System.out.println("\n" + name + " : " + valueRef.getValue());

			//Close handle
			LJM.close(handle);
		}
		catch (LJMException le) {
			le.printStackTrace();
			LJM.closeAll();
		}
	}
}
