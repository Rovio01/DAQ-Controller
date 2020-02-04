/***
Demonstrates how to read the WiFi configuration from a LabJack.

 ***/
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.labjack.LJM;
import com.labjack.LJMException;

public class ReadWiFiConfig {

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

			//Setup and call eReadNames to read WiFi configuration from the
			//LabJack.
			String[] aNames = { "WIFI_IP", "WIFI_SUBNET",
					"WIFI_GATEWAY", "WIFI_DHCP_ENABLE",
					"WIFI_IP_DEFAULT", 	"WIFI_SUBNET_DEFAULT",
					"WIFI_GATEWAY_DEFAULT", "WIFI_DHCP_ENABLE_DEFAULT",
					"WIFI_STATUS" };
			double[] aValues = new double[aNames.length];
			int numFrames = aNames.length;
			int errAddr = -1;
			IntByReference errAddrRef = new IntByReference(-1);
			LJM.eReadNames(handle, numFrames, aNames, aValues, errAddrRef);

			System.out.println("\nWiFi configuration: ");
			Pointer strPtr;
			int intValue = 0;
			for(int i = 0; i < numFrames; i++) {
				if(aNames[i].startsWith("WIFI_STATUS")
						|| aNames[i].startsWith("WIFI_DHCP_ENABLE")) {
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

			//Setup and call eReadNameString to read the WiFi SSID string from
			//the LabJack.
			String name = "WIFI_SSID";
			strPtr = new Memory(LJM.Constants.STRING_ALLOCATION_SIZE);
			LJM.eReadNameString(handle, name, strPtr);

			System.out.println("    " + name + " : " + strPtr.getString(0));

			//Close handle
			LJM.close(handle);
		}
		catch (LJMException le) {
			le.printStackTrace();
			LJM.closeAll();
		}
	}
}
