/***
Demonstrates how to set ethernet configuration settings on a LabJack.

 ***/
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.labjack.LJM;
import com.labjack.LJMException;

public class WriteEthernetConfig {

	public static double intToDouble(int integer) {
		return Long.parseLong(Integer.toBinaryString(integer), 2);
	}

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

			//Setup and call eWriteNames to set the ethernet configuration
			//on the LabJack.
			String[] aNames = {
					"ETHERNET_IP_DEFAULT",
					"ETHERNET_SUBNET_DEFAULT",
					"ETHERNET_GATEWAY_DEFAULT",
					"ETHERNET_DHCP_ENABLE_DEFAULT" };
			IntByReference ipRef = new IntByReference(0);
			IntByReference subnetRef = new IntByReference(0);
			IntByReference gatewayRef = new IntByReference(0);
			double dhcpEnable = 0;
			LJM.ipToNumber("192.168.1.207", ipRef);
			LJM.ipToNumber("255.255.255.0", subnetRef);
			LJM.ipToNumber("192.168.1.1", gatewayRef);
			dhcpEnable = 1; //1 = Enable, 0 = Disable
			double[] aValues = {
					WriteEthernetConfig.intToDouble(ipRef.getValue()),
					WriteEthernetConfig.intToDouble(subnetRef.getValue()),
					WriteEthernetConfig.intToDouble(gatewayRef.getValue()),
					dhcpEnable };
			int numFrames = aNames.length;
			IntByReference errAddrRef = new IntByReference(-1);
			LJM.eWriteNames(handle, numFrames, aNames, aValues, errAddrRef);

			System.out.println("\nSet ethernet configuration: ");
			int intValue = 0;
			Pointer strPtr;
			for(int i = 0; i < numFrames; i++) {
				if(aNames[i].startsWith("ETHERNET_DHCP_ENABLE_DEFAULT")) {
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
