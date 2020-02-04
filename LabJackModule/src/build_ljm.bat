@echo off

echo Compile ljm Java wrapper/interface in src folder (javac).
javac -source 1.5 -target 1.5 -classpath .;..\jar\jna.jar com\labjack\*.java || goto :done

echo.
echo Create ljm.jar in jar folder (jar).
jar cfm ..\jar\ljm.jar Manifest.txt com\labjack\* || goto :done

echo.
echo Generate javadoc in doc folder (javadoc).
javadoc -package -version -author -classpath .;..\jar\jna.jar -d ..\doc com.labjack || goto :done
:done
echo.
pause