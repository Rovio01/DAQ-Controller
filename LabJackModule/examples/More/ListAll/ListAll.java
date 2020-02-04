/***
Demonstrates usage of the listAll functions (LJM_ListAll) which scans for
LabJack devices and returns information describing the found devices. This will
only find LabJack devices supported by the LJM library.

 ***/
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import com.labjack.LJM;
import com.labjack.LJMException;

public class ListAll {

	public static void main(final String[] args) {
		Map<Integer, String> DEVICE_NAMES = new HashMap<Integer, String>();
		DEVICE_NAMES.put(LJM.Constants.dtT7, "T7");
		DEVICE_NAMES.put(LJM.Constants.dtT4, "T4");
		DEVICE_NAMES.put(LJM.Constants.dtDIGIT, "Digit");

		Map<Integer, String> CONN_NAMES = new HashMap<Integer, String>();
		CONN_NAMES.put(LJM.Constants.ctUSB, "USB");
		CONN_NAMES.put(LJM.Constants.ctTCP, "TCP");
		CONN_NAMES.put(LJM.Constants.ctETHERNET, "Ethernet");
		CONN_NAMES.put(LJM.Constants.ctWIFI, "WiFi");

		try {
			int MAX_SIZE = LJM.Constants.LIST_ALL_SIZE;
			IntByReference numFoundRef = new IntByReference(0);
			int[] aDeviceTypes = new int[MAX_SIZE];
			int[] aConnectionTypes = new int[MAX_SIZE];
			int[] aSerialNumbers = new int[MAX_SIZE]; 
			int[] aIPAddresses = new int[MAX_SIZE];

			//Find and display LabJack devices with listAllS.
			LJM.listAllS("ANY", "ANY", numFoundRef, aDeviceTypes,
					aConnectionTypes, aSerialNumbers, aIPAddresses);
			System.out.println("ListAllS found " + numFoundRef.getValue()
					+ " LabJacks:\n");

			/*
			//Find and display LabJack devices with listAll.
			LJM.listAll(LJM.Constants.dtANY, LJM.Constants.ctANY, numFoundRef,
					aDeviceTypes, aConnectionTypes, aSerialNumbers,
					aIPAddresses);
			System.out.println("ListAll found " + numFoundRef.getValue()
					+ " LabJacks:\n");
			*/

			System.out.format("%-18s%-18s%-18s%-18s\n", "Device Type",
					"Connection Type", "Serial Number", "IP Address");
			for(int i = 0; i < numFoundRef.getValue(); i++) {
				Pointer ipPtr = new Memory(LJM.Constants.IPv4_STRING_SIZE);
				LJM.numberToIP(aIPAddresses[i], ipPtr);
				String dev = DEVICE_NAMES.get(aDeviceTypes[i]);
				String con = CONN_NAMES.get(aConnectionTypes[i]);
				System.out.format("%-18s%-18s%-18d%-18s\n",
						((dev != null)
								? dev : String.valueOf(aDeviceTypes[i])),
						((con != null)
								? con : String.valueOf(aConnectionTypes[i])),
						aSerialNumbers[i],
						ipPtr.getString(0));
			}
		}
		catch(LJMException le) {
			le.printStackTrace();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
