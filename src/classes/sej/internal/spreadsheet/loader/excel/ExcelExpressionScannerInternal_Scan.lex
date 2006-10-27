"_FOLD"					{ return new Symbol( GeneratedSymbols.FOLD ); }
"_FOLD_1ST"				{ return new Symbol( GeneratedSymbols.FOLD1ST ); }
"_FOLD_1STOK"			{ return new Symbol( GeneratedSymbols.FOLD1STOK ); }
"_LET"					{ return new Symbol( GeneratedSymbols.LET ); }
"`" {NAME}				{ return new Symbol( GeneratedSymbols.LETVAR, yytext() ); }
