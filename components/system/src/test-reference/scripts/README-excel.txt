
To install a macro for Microsoft Excel:

	* Create a new file @Macros.xls@ (or any other name) in @%APPDATA%\Microsoft\Excel\XLSTART@. (On Windows XP, @%APPDATA%@ is usually @C:\Documents and Settings\%USERNAME%\Application Data@.)
	
	* With the new file open, start the Visual Basic Editor (Tools -> Macro -> Visual Basic Editor).
	
	* Import the macro source file (File -> Import File), for example @refTestSheets.bas@.
	
	* Save the new .xls file you created.

This file will now be opened automatically whenever you run Excel and make the macros available. To run such a macro:

	* Open a workbook with reference test data.
	* Run @ForceAllFormatsAndColumns@ macro (Tools -> Macro -> Macros).


