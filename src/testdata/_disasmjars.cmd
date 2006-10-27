setlocal

call :disasm %~dpn1
call :disasm %~dpn1-actual
goto :EOF


:disasm
"%JAVA_HOME%\bin\javap" -c -private -classpath %1.jar sej/gen/$Root > %1.txt
"%JAVA_HOME%\bin\javap" -c -private -classpath %1.jar sej/gen/$Sect0 >> %1.txt
"%JAVA_HOME%\bin\javap" -c -private -classpath %1.jar sej/gen/$Sect1 >> %1.txt
"%JAVA_HOME%\bin\javap" -c -private -classpath %1.jar sej/gen/$Sect2 >> %1.txt
goto:EOF
