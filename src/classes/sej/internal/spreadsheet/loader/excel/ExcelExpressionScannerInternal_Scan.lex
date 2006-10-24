"_FOLDL"	{ return new Symbol( GeneratedSymbols.FOLDL ); }
"_LET"		{ return new Symbol( GeneratedSymbols.LET ); }
"`" {NAME}	{ return new Symbol( GeneratedSymbols.LETVAR, yytext() ); }
