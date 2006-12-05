"_FOLD"					{ return new Symbol( GeneratedSymbols.FOLD ); }
"_REDUCE"				{ return new Symbol( GeneratedSymbols.REDUCE ); }
"_FOLD_OR_REDUCE"	{ return new Symbol( GeneratedSymbols.FOLDORREDUCE ); }
"_FOLD_ARRAY"			{ return new Symbol( GeneratedSymbols.FOLDARRAY ); }
"_LET"					{ return new Symbol( GeneratedSymbols.LET ); }
"`" {NAME}				{ return new Symbol( GeneratedSymbols.LETVAR, yytext() ); }
