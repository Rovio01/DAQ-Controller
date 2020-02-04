/***
Demonstrates how to read the ethernet MAC from a LabJack.

 ***/
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import com.labjack.LJM;
import com.labjack.LJMException;

public class ReadEthernetMac {

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

			//Call eReadAddressByteArray to read the ethernet MAC (address
			//60020). We are reading a byte array which is the big endian
			//binary representation of the 64-bit MAC.
			byte[] aBytes = new byte[8];
			IntByReference errAddrRef = new IntByReference(-1);
			LJM.eReadAddressByteArray(handle, 60020, 8, aBytes, errAddrRef);

			//Convert big endian byte array to a 64-bit unsigned integer value.
			ByteBuffer bb = ByteBuffer.wrap(aBytes);
			bb.order(ByteOrder.BIG_ENDIAN);
			long macNumber = bb.getLong();

			//Convert the MAC value/number to its string representation.
			Pointer macStringPtr = new Memory(LJM.Constants.MAC_STRING_SIZE);
			LJM.numberToMAC(macNumber, macStringPtr);

			System.out.println("\nEthernet MAC : " + macNumber + " - "
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
