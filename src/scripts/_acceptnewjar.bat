setlocal
set exp=%~dpn1.jar
set act=%~dpn1-actual.jar
set exp_out=%~dpn1.txt
set act_out=%~dpn1-actual.txt

if not exist %exp_out% goto :EOF

del %exp_out%
del %act_out%
del %exp%
ren %act% %~n1.jar

