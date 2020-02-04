/***
Demonstrates how to read the ethernet configuration settings from a LabJack.

 ***/
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.labjack.LJM;
import com.labjack.LJMException;

public class ReadEthernetConfig {

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

			//Setup and call eReadNames to read ethernet configuration from
			//the LabJack.
			String[] aNames = {"ETHERNET_IP", "ETHERNET_SUBNET",
					"ETHERNET_GATEWAY", "ETHERNET_IP_DEFAULT",
					"ETHERNET_SUBNET_DEFAULT", "ETHERNET_GATEWAY_DEFAULT",
					"ETHERNET_DHCP_ENABLE", "ETHERNET_DHCP_ENABLE_DEFAULT"};
			double[] aValues = new double[aNames.length];
			int numFrames = aNames.length;
			IntByReference errAddrRef = new IntByReference(-1);
			LJM.eReadNames(handle, numFrames, aNames, aValues, errAddrRef);

			System.out.println("\nEthernet configuration: ");
			int intValue = 0;
			Pointer strPtr;
			for(int i = 0; i < numFrames; i++) {
				if(aNames[i].startsWith("ETHERNET_DHCP_ENABLE")) {
					System.out.println("    " + aNames[i] + " : "
							+ String.format("%.0f", aValues[i]));
				}
				else {
					intValue = (int)(new Double(aValues[i]).longValue()
							& 0xFFFFFFFF);
					strPtr = new Memory(LJM.Constants.IPv4_STRING_SIZE);
					LJM.numberToIP(intValue, strPtr);
					System.out.println("    " + aNames[i] + " : "
							+ String.format("%.0f", aValues[i]) + " - "
							+ strPtr.getString(0));
				}
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
