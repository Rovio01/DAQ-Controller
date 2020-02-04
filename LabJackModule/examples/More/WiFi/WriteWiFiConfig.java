/***
Demonstrates how to configure the WiFi settings on a LabJack.

 ***/
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.labjack.LJM;
import com.labjack.LJMException;

public class WriteWiFiConfig {

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

			//Setup and call eWriteNames to configure WiFi default settings on
			//the LabJack.
			String[] aNames = { "WIFI_IP_DEFAULT", "WIFI_SUBNET_DEFAULT",
					"WIFI_GATEWAY_DEFAULT" };
			IntByReference ipRef = new IntByReference(0);
			IntByReference subnetRef = new IntByReference(0);
			IntByReference gatewayRef = new IntByReference(0);
			LJM.ipToNumber("192.168.1.207", ipRef);
			LJM.ipToNumber("255.255.255.0", subnetRef);
			LJM.ipToNumber("192.168.1.1", gatewayRef);
			double[] aValues = { WriteWiFiConfig.intToDouble(ipRef.getValue()),
					WriteWiFiConfig.intToDouble(subnetRef.getValue()),
					WriteWiFiConfig.intToDouble(gatewayRef.getValue()) };
			int numFrames = aNames.length;
			IntByReference errAddrRef = new IntByReference(-1);
			LJM.eWriteNames(handle, numFrames, aNames, aValues, errAddrRef);

			System.out.println("\nSet WiFi configuration:");
			int intValue = 0;
			Pointer strPtr;
			for(int i = 0; i < numFrames; i++) {
				intValue = (int)(new Double(aValues[i]).longValue()
						& 0xFFFFFFFF);
				strPtr = new Memory(LJM.Constants.IPv4_STRING_SIZE);
				LJM.numberToIP(intValue, strPtr);
				System.out.println("    " + aNames[i] + " : "
						+ String.format("%.0f", aValues[i]) + " - "
						+ strPtr.getString(0));
			}

			//Setup and call eWriteNameString to configure the default WiFi SSID
			//on the LabJack.
			String name = "WIFI_SSID_DEFAULT";
			String str = "LJOpen";
			LJM.eWriteNameString(handle, name, str);
			System.out.println("    " + name + " : " + str);

			//Setup and call eWriteNameString to configure the default WiFi
			//password on the LabJack.
			name = "WIFI_PASSWORD_DEFAULT";
			str = "none";
			LJM.eWriteNameString(handle, name, str);
			System.out.println("    " + name + " : " + str);

			//Setup and call eWriteName to apply the new WiFi configuration
			//on the LabJack.
			name = "WIFI_APPLY_SETTINGS";
			double value = 1;  //1 = apply
			LJM.eWriteName(handle, name, value);
			System.out.println("    " + name + " : " + value);

			//Close handle
			LJM.close(handle);
		}
		catch (LJMException le) {
			le.printStackTrace();
			LJM.closeAll();
		}
	}
}
