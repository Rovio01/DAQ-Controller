/***
Demonstrates SPI communication.

You can short MOSI to MISO for testing.

T7:
	MOSI    FIO2
	MISO    FIO3
	CLK     FIO0
	CS      FIO1

T4:
	MOSI    FIO6
	MISO    FIO7
	CLK     FIO4
	CS      FIO5

If you short MISO to MOSI, then you will read back the same bytes that you
write.  If you short MISO to GND, then you will read back zeros.  If you
short MISO to VS or leave it unconnected, you will read back 255s.

 ***/
import java.util.Arrays;
import java.util.Random;
import com.sun.jna.ptr.IntByReference;
import com.labjack.LJM;
import com.labjack.LJMException;

public class SPI {

	public static void main(final String[] args) {
		try {
			IntByReference handleRef = new IntByReference(0);
			int handle = 0;
			IntByReference errAddrRef = new IntByReference(-1);
			String[] aNames;
			double[] aValues;

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

			if(LJMUtilities.getDeviceType(handle) == LJM.Constants.dtT4) {
				//Configure FIO4 to FIO7 as digital I/O.
				LJM.eWriteName(handle, "DIO_INHIBIT", 0xFFF0F);
				LJM.eWriteName(handle, "DIO_ANALOG_ENABLE", 0x00000);

				//Setting CS, CLK, MISO, and MOSI lines for the T4. FIO0 to
				//FIO3 are reserved for analog inputs, and SPI requires digital
				//lines.
				LJM.eWriteName(handle, "SPI_CS_DIONUM", 5);  //CS is FIO5
				LJM.eWriteName(handle, "SPI_CLK_DIONUM", 4);  //CLK is FIO4
				LJM.eWriteName(handle, "SPI_MISO_DIONUM", 7);  //MISO is FIO7
				LJM.eWriteName(handle, "SPI_MOSI_DIONUM", 6);  //MOSI is FIO6
			}
			else {
				//Setting CS, CLK, MISO, and MOSI lines for the T7 and other
				//devices.
				LJM.eWriteName(handle, "SPI_CS_DIONUM", 1);  //CS is FIO1
				LJM.eWriteName(handle, "SPI_CLK_DIONUM", 0);  //CLK is FIO0
				LJM.eWriteName(handle, "SPI_MISO_DIONUM", 3);  //MISO is FIO3
				LJM.eWriteName(handle, "SPI_MOSI_DIONUM", 2);  //MOSI is FIO2
			}

			//Selecting Mode CPHA=1 (bit 0), CPOL=1 (bit 1)
			LJM.eWriteName(handle, "SPI_MODE", 3);

			//Speed Throttle:
			//Valid speed throttle values are 1 to 65536 where 0 = 65536.
			//Configuring Max. Speed (~800 kHz) = 0
			LJM.eWriteName(handle, "SPI_SPEED_THROTTLE", 0);

			//Options
			//bit 0:
			//    0 = Active low clock select enabled
			//    1 = Active low clock select disabled.
			//bit 1:
			//    0 = DIO directions are automatically changed
			//    1 = DIO directions are not automatically changed.
			//bits 2-3: Reserved
			//bits 4-7: Number of bits in the last byte. 0 = 8.
			//bits 8-15: Reserved

			//Enabling active low clock select pin
			LJM.eWriteName(handle, "SPI_OPTIONS", 0);

			//Read back and display the SPI settings
			aNames = new String[] { "SPI_CS_DIONUM", "SPI_CLK_DIONUM",
					"SPI_MISO_DIONUM", "SPI_MOSI_DIONUM",
					"SPI_MODE", "SPI_SPEED_THROTTLE",
					"SPI_OPTIONS" };
			aValues = new double[aNames.length];
			LJM.eReadNames(handle, aNames.length, aNames, aValues, errAddrRef);

			System.out.println("\nSPI Configuration:");
			for(int i = 0; i < aNames.length; i++) {
				System.out.println("  " + aNames[i] + " = " + aValues[i]);
			}

			//Write(TX)/Read(RX) 4 bytes
			int numBytes = 4;
			LJM.eWriteName(handle, "SPI_NUM_BYTES", numBytes);

			//Write the bytes
			byte[] aBytes = new byte[numBytes];
			new Random().nextBytes(aBytes);  //TX data of random values
			LJM.eWriteNameByteArray(handle, "SPI_DATA_TX", numBytes, aBytes,
					errAddrRef);
			LJM.eWriteName(handle, "SPI_GO", 1);  //Do the SPI communications

			//Display the bytes written
			System.out.println("");
			for(int i = 0; i < numBytes; i++) {
				System.out.println("dataWrite[" + i + "] = "
						+ (0xFF & aBytes[i]));
			}

			//Read the bytes
			Arrays.fill(aBytes, (byte)0);
			LJM.eReadNameByteArray(handle, "SPI_DATA_RX", numBytes, aBytes,
					errAddrRef);
			LJM.eWriteName(handle, "SPI_GO", 1);  //Do the SPI communications

			//Display the bytes read
			System.out.println("");
			for(int i = 0; i < numBytes; i++) {
				System.out.println("dataRead[" + i + "] = "
						+ (0xFF & aBytes[i]));
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
