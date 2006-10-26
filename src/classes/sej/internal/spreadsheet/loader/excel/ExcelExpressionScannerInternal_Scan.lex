"_FOLDL"				{ return new Symbol( GeneratedSymbols.FOLDL ); }
"_FOLDL_1ST"			{ return new Symbol( GeneratedSymbols.FOLDL1ST ); }
"_FOLDL_1ST_OK"			{ return new Symbol( GeneratedSymbols.FOLDL1STOK ); }
"_LET"					{ return new Symbol( GeneratedSymbols.LET ); }
"`" {NAME}				{ return new Symbol( GeneratedSymbols.LETVAR, yytext() ); }
