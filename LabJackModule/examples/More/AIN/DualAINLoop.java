/***
Demonstrates reading 2 analog inputs (AINs) in a loop from a LabJack.

 ***/
import java.io.DataInputStream;
import com.sun.jna.ptr.IntByReference;
import com.labjack.LJM;
import com.labjack.LJMException;

public class DualAINLoop {

	public static void main(final String[] args) {
		try {
			IntByReference handleRef = new IntByReference(0);
			int handle = 0;
			int deviceType = 0;
			String[] aNames;
			double[] aValues;

			//If arguments are passed to the application, the first argument
			//specifies how many times to loop. If an argument is not passed,
			//will loop until the Enter key is pressed.
			int loopAmount = 0;
			boolean infLoop = true;
			String loopStr = "\nStarting read loop. Press Enter to stop.";
			if(args.length > 0) {
				try {
					loopAmount = new Double(args[0]).intValue();
					infLoop = false;
					loopStr = "\nStarting " + loopAmount + " read loops.";
				}
				catch(Exception ex) {
					throw new Exception("Invalid first argument \"" + args[0]
							+ "\". This specifies how many times to loop and "
							+ "needs to be a number.");
				}
			}

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

			deviceType = LJMUtilities.getDeviceType(handle);

			//Setup and call eWriteNames to configure AINs.
			if(deviceType == LJM.Constants.dtT4) {
				//LabJack T4 configuration

				//AIN0 and AIN1:
				//    Range = +/-10 V. Only AIN0-AIN3 support the +/-10 V range.
				//    Resolution index = 0 (default).
				//    Settling = 0 (auto)
				aNames = new String[] { "AIN0_RANGE", "AIN0_RESOLUTION_INDEX",
						"AIN0_SETTLING_US", "AIN1_RANGE",
						"AIN1_RESOLUTION_INDEX", "AIN1_SETTLING_US" };
				aValues = new double[] { 10, 0, 0, 10, 0, 0 };
			}
			else {
				//LabJack T7 and other devices configuration

				//AIN0 and AIN1:
				//    Negative Channel = 199 (Single-ended)
				//    Range = +/-10 V
				//    Resolution index = 0 (default).
				//    Settling = 0 (auto)
				aNames = new String[] { "AIN0_NEGATIVE_CH", "AIN0_RANGE",
						"AIN0_RESOLUTION_INDEX", "AIN0_SETTLING_US",
						"AIN1_NEGATIVE_CH", "AIN1_RANGE",
						"AIN1_RESOLUTION_INDEX", "AIN1_SETTLING_US"};
				aValues = new double[] { 199, 10, 0, 0, 199, 10, 0, 0 };
			}
			int numFrames = aNames.length;
			IntByReference errAddr = new IntByReference(-1);
			LJM.eWriteNames(handle, numFrames, aNames, aValues, errAddr);

			System.out.println("\nSet configuration:");
			for(int i = 0; i < numFrames; i++) {
				System.out.println("  " + aNames[i] + " : " + aValues[i]);
			}

			//Setup and call eReadNames to read AINs.
			aNames = new String[] { "AIN0", "AIN1" };
			aValues = new double[] { 0, 0 };
			numFrames = aNames.length;
			
			System.out.println(loopStr);
			DataInputStream ins = new DataInputStream(System.in);
			int it = 0;
			while(ins.available() <= 0) {
				LJM.eReadNames(handle, numFrames, aNames, aValues, errAddr);
				System.out.print("\n");
				for(int i = 0; i < numFrames; i++) {
					System.out.print(aNames[i] + " = "
							+ String.format("%.4f", aValues[i]) + " V");
					if(i < numFrames - 1) {
						System.out.print(", ");
					}
				}
				System.out.print("\n");

				//Stop after the user specified number of loops.
				it++;
				if(!infLoop && it >= loopAmount) {
					break;
				}

				Thread.sleep(1000); //Wait 1 second
			}

			//Close handle
			LJM.close(handle);
		}
		catch(LJMException le) {
			le.printStackTrace();
			LJM.closeAll();
		}
		catch(Exception e) {
			e.printStackTrace();
			LJM.closeAll();
		}
	}
}
