LJM Java
Dec. 04, 2017
support@labjack.com


The cross-platform Java wrapper and examples for the LJM library. Please report
bugs to support@labjack.com.


Requirements:

* LabJack T7 and T4
* Windows, Linux, Mac OS X
* LJM library:
    https://labjack.com/support/software/installers/ljm
* Java 5.0 (1.5) or newer.
* JNA library (version 4.5 included in the jar directory)
    https://github.com/java-native-access/jna


Contents:

The "doc" directory contains Javadoc for the LJM wrapper's classes.

The "examples" directory contains all Java code examples.

The "jar" directory contains the packaged LJM Java classes, ljm.jar, and the
jna.jar library for convenience.

The "src" directory provides the LJM Java wrapper's source code.


Build and Run

To build and run applications with the LJM wrapper you need to add the ljm.jar
and jna.jar libraries to your build/compile path and classpath.

Alternatively, you can build the LJM wrapper into your project from the source
code provided. You will still need jna.jar.

Using the javac compiler and java runtime you can add the JAR files/libraries
with the "-classpath" option, and in IDEs you add the JARs in your project's
settings. Refer to your IDE's documention on adding JAR libraries to your build
and runtime.

Here's an example of compiling and running an example by command line using
javac and java:

Windows:
    cd examples\Basic
    javac -classpath .;..;..\..\jar\jna.jar;..\..\jar\ljm.jar EReadName.java
    java -classpath .;..;..\..\jar\jna.jar;..\..\jar\ljm.jar EReadName

Linux/Mac OS X:
    cd examples/Basic
    javac -classpath .:..:../../jar/jna.jar:../../jar/ljm.jar EReadName.java
    java -classpath .:..:../../jar/jna.jar:../../jar/ljm.jar EReadName

Here's how to build the LJM classes from the source code (optional) by command
line using javac:

Windows:
    cd src
    javac -classpath .;..\jar\jna.jar com\labjack\*.java

Linux/Mac OS X:
    cd src
    javac -classpath .:../jar/jna.jar com/labjack/*.java

The LJM wrapper loads the LJM library (LabJackM) from the default JNA library
search paths which should work for standard LJM library installations. If for
some reason the LJM library cannot be found, try one of the following:

- Set the jna.library.path system property to the LJM library's directory. When
  running your code you can set this with the -D option. For example:

    java -Djna.library.path=<path_to_ljm_library> -classpath <classpaths> MyApp

- Add the LJM library's directory to your operating system's library access
  environment variable. On Windows it is PATH, on Linux it is LD_LIBRARY_PATH,
  and on Mac OS X it is DYLD_LIBRARY_PATH.


Documentation:

Java specific documention can be found in the "doc" directory. Full LJM function
documention, constants and error codes can be found here:

    https://labjack.com/support/software/api/ljm

All Modbus register addresses are documented here:

    https://labjack.com/support/software/api/modbus/modbus-map

T7 and T4 documention can be found here:

    https://labjack.com/support/datasheets/t-series


Licenses:

Provided LabJack source code and software in this package are licensed under MIT
X11. See the LICENSE.txt file for details.

JNA is dual-licensed under two alternative Open Source/Free licenses: LGPL 2.1
and Apache License 2.0. For additional license information, source code,
documentation and builds visit the JNA github page:

    https://github.com/java-native-access/jna


Changes:

Dec. 04, 2017
  - Updated functions, constants and error codes to LJM v1.16.
  - Changed the Examples directory structure.
  - Added new examples.
  - Updated all examples and utility code with T4 support.
  - Updated jna.jar to version 4.5.

May 21, 2015
  - Updated functions, constants and error codes to LJM v1.8.
  - Added and updated some examples.

Jan. 02, 2014
  - Updated functions, constants and error codes to LJM v1.2.
 
Dec. 19, 2013
  - Added more examples.

Nov. 11, 2013
  - Added Linux and Mac OS X support.
  - Fixed classes in ljm.jar to work down to Java 5.0 (1.5).

Oct. 29, 2013
  - Initial release tested with LJM library v1.1.1.
