setlocal
set exp=%~dpn1.jar
set act=%~dpn1-actual.jar
set exp_out=%~dpn1.txt
set act_out=%~dpn1-actual.txt

"%JAVA_HOME%\bin\javap" -c -private -classpath %exp% sej/gen/$Root > %exp_out%
"%JAVA_HOME%\bin\javap" -c -private -classpath %act% sej/gen/$Root > %act_out%
