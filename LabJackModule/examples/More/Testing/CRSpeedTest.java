/***
Performs LabJack operations in a loop and reports the timing statistics for the
operations.

 ***/
import com.sun.jna.ptr.IntByReference;
import com.labjack.LJM;
import com.labjack.LJMException;

public class CRSpeedTest {

	public static void main(final String[] args) {
		try {
			IntByReference handleRef = new IntByReference(0);
			int handle = 0;
			int deviceType = 0;
			int i = 0;

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

			//Number of iterations to perform in the loop
			final int numIterations = 1000;

			//Analog input settings
			final int numAIN = 1;  //Number of analog inputs to read
			final double rangeAIN = 10.0;  //T7 AIN range
			final double rangeAINHV = 10.0;  //T4 HV channels range
			final double rangeAINLV = 2.5;  //T4 LV channels range
			final double resolutionAIN = 1.0;

			//Digital settings
			final boolean readDigital = false;
			final boolean writeDigital = false;

			//Analog output settings
			final boolean writeDACs = false;

			//Use eAddresses (true) or eNames (false) in the operations loop.
			//eAddresses is faster than eNames.
			final boolean useAddresses = true;

			//Variables for LJM library calls
			int numFrames = 0;
			String[] aNames;
			int[] aAddresses;
			int[] aTypes;
			int[] aWrites;
			int[] aNumValues;
			double[] aValues;
			IntByReference errAddrRef = new IntByReference(-1);

			if(deviceType == LJM.Constants.dtT4) {
				//For the T4, configure the channels to analog input or
				//digital I/O.

				//Update all digital I/O channels.
				//b1 = Ignored. b0 = Affected.
				double dioInhibit = 0x00000;  //b00000000000000000000
				//Set AIN 0 to numAIN-1 as analog inputs (b1), the rest as
				//digital I/O (b0).
				double dioAnalogEnable = Math.pow(2, numAIN) - 1;
				aNames = new String[] { "DIO_INHIBIT", "DIO_ANALOG_ENABLE" };
				aValues = new double[] { dioInhibit, dioAnalogEnable };
				LJM.eWriteNames(handle, 2, aNames, aValues, errAddrRef);
				if(writeDigital)
				{
					//Update only digital I/O channels in future digital
					//write calls. b1 = Ignored. b0 = Affected.
					dioInhibit = dioAnalogEnable;
					LJM.eWriteName(handle, "DIO_INHIBIT", dioInhibit);
				}
			}

			if(numAIN > 0) {
				//Configure analog input settings
				numFrames = Math.max(0, numAIN*2);
				aNames = new String[numFrames];
				aValues = new double[numFrames];
				for(i = 0; i < numAIN; i++) {
					aNames[i*2] = "AIN" + i + "_RANGE";
					if(deviceType == LJM.Constants.dtT4) {
						//T4 range
						if (i < 4) {
							aValues[i*2] = rangeAINHV;  //HV line
						}
						else {
							aValues[i*2] = rangeAINLV;  //LV line
						}
					}
					else {
						//T7 range
						aValues[i*2] = rangeAIN;
					}
					aNames[i*2+1] = "AIN" + i + "_RESOLUTION_INDEX";
					aValues[i*2+1] = resolutionAIN;
				}
				LJM.eWriteNames(handle, numFrames, aNames, aValues,
						errAddrRef);
			}

			//Initialize and configure eNames parameters for loop's eNames call
			numFrames = Math.max(0, numAIN) + (readDigital?1:0)
					+ (writeDigital?1:0) + (writeDACs?1:0)*2;
			aNames = new String[numFrames];
			aWrites = new int[numFrames];
			aNumValues = new int[numFrames];
			aValues = new double[numFrames];  //numFrames in size in this case

			//Add analog input reads (AIN 0 to numAIN-1)
			for(i = 0; i < numAIN; i++) {
				aNames[i] = "AIN" + i;
				aWrites[i] = LJM.Constants.READ;
				aNumValues[i] = 1;
				aValues[i] = 0;
			}

			if(readDigital) {
				//Add digital read
				aNames[i] = "DIO_STATE";
				aWrites[i] = LJM.Constants.READ;
				aNumValues[i] = 1;
				aValues[i] = 0;
				i++;
			}

			if(writeDigital) {
				//Add digital write
				aNames[i] = "DIO_STATE";
				aWrites[i] = LJM.Constants.WRITE;
				aNumValues[i] = 1;
				aValues[i] = 0; //output-low
				i++;
			}

			if(writeDACs) {
				//Add analog output writes (DAC0-1)
				for(int j = 0; j < 2; j++, i++) {
					aNames[i] = "DAC" + j;
					aWrites[i] = LJM.Constants.WRITE;
					aNumValues[i] = 1;
					aValues[i] = 0.0; //0.0 V
				}
			}

			//Make arrays of addresses and data types for eAddresses.
			aAddresses = new int[numFrames];
			aTypes = new int[numFrames];
			LJM.namesToAddresses(numFrames, aNames, aAddresses, aTypes);

			System.out.println("\nTest frames:");

			String wrStr = "";
			for(i = 0; i < numFrames; i++) {
				if(aWrites[i] == LJM.Constants.READ) {
					wrStr = "READ";
				}
				else {
					wrStr = "WRITE";
				}
				System.out.println("    " + wrStr + " " + aNames[i] + " ("
						+ aAddresses[i] + ")");
			}
			System.out.println("\nBeginning " + numIterations
					+ " iterations...");

			//Initialize time variables
			double maxMS = 0.0;
			double minMS = 0.0;
			double totalMS = 0.0;
			double curMS = 0.0;
			long stTime = 0;
			long enTime = 0;

			//eAddresses or eNames operations loop
			for(i = 0; i < numIterations; i++) {
				stTime = System.nanoTime();
				if(useAddresses) {
					LJM.eAddresses(handle, numFrames, aAddresses, aTypes, aWrites,
							aNumValues, aValues, errAddrRef);
				}
				else {
					LJM.eNames(handle, numFrames, aNames, aWrites, aNumValues,
							aValues, errAddrRef);
				}
				enTime = System.nanoTime();
				curMS = (enTime - stTime)/1000000.0;
				if(minMS == 0)
					minMS = curMS;
				minMS = Math.min(curMS, minMS);
				maxMS = Math.max(curMS, maxMS);
				totalMS += curMS;
			}

			System.out.println("\n" + numIterations + " iterations performed:");
			System.out.println("    Time taken: "
					+ String.format("%.3f", totalMS) + " ms");
			System.out.println("    Average time per iteration: "
					+ String.format("%.3f", totalMS/numIterations) + " ms");
			System.out.println("    Min / Max time for one iteration: "
					+ String.format("%.3f", minMS) + " ms / "
					+ String.format("%.3f", maxMS) + " ms");

			if(useAddresses) {
				System.out.println("\nLast eAddresses results: ");
			}
			else {
				System.out.println("\nLast eNames results: ");
			}
			for(i = 0; i < numFrames; i++) {
				if(aWrites[i] == LJM.Constants.READ) {
					wrStr = "READ";
				}
				else {
					wrStr = "WRITE";
				}
				System.out.println("    " + aNames[i] + " (" + aAddresses[i]
						+ ") " + wrStr + " value : "
						+ String.format("%.4f", aValues[i]));
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
