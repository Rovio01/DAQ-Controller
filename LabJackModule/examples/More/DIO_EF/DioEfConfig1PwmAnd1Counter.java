/***
Enables a 10 kHz PWM output on FIO0 for the T7 or FIO6 for the T4, enables a
high-speed counter on CIO2 (DIO18), waits 1 second and reads the counter.
Jumper FIO0/FIO6 to CIO2 and the read value. Value should be close to 10000.

DIO extended features, PWM output and high-speed counter documented here:

https://labjack.com/support/datasheets/t-series/digital-io/extended-features
https://labjack.com/support/datasheets/t-series/digital-io/extended-features/pwm-out
https://labjack.com/support/datasheets/t-series/digital-io/extended-features/high-speed-counter

 ***/
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;
import com.labjack.LJM;
import com.labjack.LJMException;

public class DioEfConfig1PwmAnd1Counter {

	public static void main(final String[] args) {
		try {
			IntByReference handleRef = new IntByReference(0);
			int handle = 0;
			int deviceType = 0;
			int numFrames = 0;
			String[] aNames;
			double[] aValues;
			IntByReference errAddrRef = new IntByReference(-1);
			int pwmDIO = 0;

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

			//Configure the PWM output and counter.
			if (deviceType == LJM.Constants.dtT4) {
				//For the T4, use FIO6 (DIO6) for the PWM output
				pwmDIO = 6;

				//Set FIO and EIO lines to digital I/O.
				aNames = new String[] { "DIO_INHIBIT", "DIO_ANALOG_ENABLE" };
				aValues = new double[] { 0xFBF, 0x000 };
				numFrames = aNames.length;
				LJM.eWriteNames(handle, numFrames, aNames, aValues,
						errAddrRef);
			}
			else {
				//For the T7 and other devices, use FIO0 (DIO0) for the PWM
				//output
				pwmDIO = 0;
			}
			aNames = new String[] { "DIO_EF_CLOCK0_DIVISOR",
					"DIO_EF_CLOCK0_ROLL_VALUE", "DIO_EF_CLOCK0_ENABLE",
					"DIO" + pwmDIO + "_EF_INDEX",
					"DIO" + pwmDIO + "_EF_CONFIG_A",
					"DIO" + pwmDIO + "_EF_ENABLE", "DIO18_EF_INDEX",
					"DIO18_EF_ENABLE"};
			aValues = new double[] {1, 8000, 1, 0, 2000, 1, 7, 1};
			numFrames = aNames.length;
			LJM.eWriteNames(handle, numFrames, aNames, aValues, errAddrRef);

			//Wait 1 second.
			try {
				Thread.sleep(1000);
			} 
			catch (InterruptedException e) {
				e.printStackTrace();
			}

			//Read from the counter.
			DoubleByReference valueRef = new DoubleByReference(0);
			LJM.eReadName(handle, "DIO18_EF_READ_A", valueRef);

			System.out.println("\nCounter = " + valueRef.getValue());

			//Turn off PWM output and counter
			aNames = new String[] { "DIO_EF_CLOCK0_ENABLE",
					"DIO" + pwmDIO + "_EF_ENABLE" };
			aValues = new double[] { 0, 0 };
			numFrames = aNames.length;
			LJM.eWriteNames(handle, numFrames, aNames, aValues, errAddrRef);

			// Close handle
			LJM.close(handle);
		}
		catch (LJMException le) {
			le.printStackTrace();
			LJM.closeAll();
		}
	}
}
