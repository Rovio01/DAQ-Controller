/***
Demonstrates how to read the WiFi MAC from a LabJack.

 ***/
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import com.labjack.LJM;
import com.labjack.LJMException;

public class ReadWiFiMac {

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

			//Call eReadAddressByteArray to read the WiFi MAC (address 60024)
			//from the LabJack. We are reading a byte array which is the big
			//endian binary representation of the 64-bit MAC.
			byte[] aBytes = new byte[8];
			IntByReference errAddrRef = new IntByReference(-1);
			LJM.eReadAddressByteArray(handle, 60024, 8, aBytes, errAddrRef);

			//Convert big endian byte array to a 64-bit unsigned integer value.
			ByteBuffer bb = ByteBuffer.wrap(aBytes);
			bb.order(ByteOrder.BIG_ENDIAN);
			long macNumber = bb.getLong();

			//Convert the MAC value/number to its string representation
			Pointer macStringPtr = new Memory(LJM.Constants.MAC_STRING_SIZE);
			LJM.numberToMAC(macNumber, macStringPtr);

			System.out.println("\nWiFi MAC : " + macNumber + " - "
					+ macStringPtr.getString(0));

			//Close handle
			LJM.close(handle);
		}
		catch (LJMException le) {
			le.printStackTrace();
			LJM.closeAll();
		}
	}
}
