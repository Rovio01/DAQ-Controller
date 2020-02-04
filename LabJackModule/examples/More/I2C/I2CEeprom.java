/***
Demonstrates I2C communication using a LabJack. The demonstration uses a
LJTick-DAC connected to FIO0/FIO1 for the T7 or FIO4/FIO5 for the T4, and
configures the I2C settings. Then a read, write and again a read are
performed on the LJTick-DAC EEPROM.

 ***/
import java.util.Arrays;
import java.util.Random;
import com.sun.jna.ptr.IntByReference;
import com.labjack.LJM;
import com.labjack.LJMException;

public class I2CEeprom {

	public static void main(final String[] args) {
		try {
			IntByReference handleRef = new IntByReference(0);
			int handle = 0;
			int numBytes = 0;
			byte[] aBytes = new byte[5];  //For TX/RX bytes. 5 bytes max.
			IntByReference errAddrRef = new IntByReference(-1);

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

			//Configure the I2C communication.
			if(LJMUtilities.getDeviceType(handle) == LJM.Constants.dtT4) {
				//Configure FIO4 and FIO5 as digital I/O.
				LJM.eWriteName(handle, "DIO_INHIBIT", 0xFFFCF);
				LJM.eWriteName(handle, "DIO_ANALOG_ENABLE", 0x00000);

				//For the T4, using FIO4 and FIO5 for SCL and SDA pins. FIO0 to
				//FIO3 are reserved for analog inputs, and digital lines are
				//required.
				LJM.eWriteName(handle, "I2C_SDA_DIONUM", 5);  //FIO5
				LJM.eWriteName(handle, "I2C_SCL_DIONUM", 4);  //FIO4
			}
			else {
				//For the T7 and other devices, using FIO0 and FIO1 for the SCL
				//and SDA pins.
				LJM.eWriteName(handle, "I2C_SDA_DIONUM", 1);  //FIO1
				LJM.eWriteName(handle, "I2C_SCL_DIONUM", 0);  //FIO0
			}

			//Speed throttle is inversely proportional to clock frequency.
			//0 = max.

			//Speed throttle = 65516 (~100 kHz)
			LJM.eWriteName(handle, "I2C_SPEED_THROTTLE", 65516);

			//Options bits:
			//  bit0: Reset the I2C bus.
			//  bit1: Restart w/o stop
			//  bit2: Disable clock stretching.
			LJM.eWriteName(handle, "I2C_OPTIONS", 0);  //Options = 0

			//Slave Address of the I2C chip = 80 (0x50)
			LJM.eWriteName(handle, "I2C_SLAVE_ADDRESS", 80);

			//Initial read of EEPROM bytes 0-3 in the user memory area. We need
			//a single I2C transmission that writes the chip's memory pointer
			//and then reads the data.

			//Number of bytes to transmit
			LJM.eWriteName(handle, "I2C_NUM_BYTES_TX", 1);
			//Number of bytes to receive
			LJM.eWriteName(handle, "I2C_NUM_BYTES_RX", 4);

			//Set the TX bytes. We are sending 1 byte for the address.
			numBytes = 1;
			aBytes[0] = 0;  //Byte 0: Memory pointer = 0
			LJM.eWriteNameByteArray(handle, "I2C_DATA_TX", numBytes, aBytes,
					errAddrRef);

			LJM.eWriteName(handle, "I2C_GO", 1);  //Do the I2C communications

			//Read the RX bytes
			numBytes = 4;
			//aBytes[0] to aBytes[3] will contain the data
			Arrays.fill(aBytes, (byte)0);
			LJM.eReadNameByteArray(handle, "I2C_DATA_RX", numBytes, aBytes,
					errAddrRef);

			System.out.print("\nRead User Memory [0-3] = ");
			for(int i = 0; i < numBytes; i++) {
				System.out.print((0xFF & aBytes[i]) + " ");
			}
			System.out.println("");

			//Write EEPROM bytes 0-3 in the user memory area, using the page
			//write technique.  Note that page writes are limited to 16 bytes
			//max, and must be aligned with the 16-byte page intervals. For
			//instance, if you start writing at address 14, you can only write
			//two bytes because byte 16 is the start of a new page.

			//Number of bytes to transmit
			LJM.eWriteName(handle, "I2C_NUM_BYTES_TX", 5);
			//Number of bytes to receive
			LJM.eWriteName(handle, "I2C_NUM_BYTES_RX", 0); 

			//Set the TX bytes
			numBytes = 5;
			//Create 4 new random numbers to write (aBytes[1-4]).
			new Random().nextBytes(aBytes);
			aBytes[0] = 0;  //Byte 0: Memory pointer = 0
			LJM.eWriteNameByteArray(handle, "I2C_DATA_TX", numBytes, aBytes,
					errAddrRef);

			LJM.eWriteName(handle, "I2C_GO", 1); //Do the I2C communications

			System.out.print("Write User Memory [0-3] = ");
			for(int i = 1; i < numBytes; i++) {
				System.out.print((0xFF & aBytes[i]) + " ");
			}
			System.out.println("");

			//Final read of EEPROM bytes 0-3 in the user memory area. We
			//need a single I2C transmission that writes the address and
			//then reads the data.

			//Number of bytes to transmit
			LJM.eWriteName(handle, "I2C_NUM_BYTES_TX", 1);
			//Number of bytes to receive
			LJM.eWriteName(handle, "I2C_NUM_BYTES_RX", 4);

			//Set the TX bytes. We are sending 1 byte for the address.
			numBytes = 1;
			aBytes[0] = 0;  //Byte 0: Memory pointer = 0
			LJM.eWriteNameByteArray(handle, "I2C_DATA_TX", numBytes, aBytes,
					errAddrRef);

			LJM.eWriteName(handle, "I2C_GO", 1);  //Do the I2C communications

			//Read the RX bytes.
			numBytes = 4;
			//aBytes[0] to aBytes[3] will contain the data
			Arrays.fill(aBytes, (byte)0);
			LJM.eReadNameByteArray(handle, "I2C_DATA_RX", numBytes, aBytes,
					errAddrRef);

			System.out.print("Read User Memory [0-3] = ");
			for(int i = 0; i < numBytes; i++) {
				System.out.print((0xFF & aBytes[i]) + " ");
			}
			System.out.println("");

			//Close handle
			LJM.close(handle);
		}
		catch (LJMException le) {
			le.printStackTrace();
			LJM.closeAll();
		}
	}
}
