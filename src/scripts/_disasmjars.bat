setlocal

call :disasm %~dpn1
call :disasm %~dpn1-actual
goto :EOF


:disasm
"%JAVA_HOME%\bin\javap" -c -private -verbose -classpath %1.jar org/formulacompiler/gen/$Root > %1.txt
"%JAVA_HOME%\bin\javap" -c -private -verbose -classpath %1.jar org/formulacompiler/gen/$Sect0 >> %1.txt
"%JAVA_HOME%\bin\javap" -c -private -verbose -classpath %1.jar org/formulacompiler/gen/$Sect1 >> %1.txt
"%JAVA_HOME%\bin\javap" -c -private -verbose -classpath %1.jar org/formulacompiler/gen/$Sect2 >> %1.txt
goto:EOF
