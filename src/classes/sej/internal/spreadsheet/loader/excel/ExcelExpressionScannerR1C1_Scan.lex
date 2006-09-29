{CELL}		{ return new Symbol( GeneratedSymbols.CELLR1C1, yytext() ); }
{COL}		{ return new Symbol( GeneratedSymbols.COL, yytext() ); }
{ROW}		{ return new Symbol( GeneratedSymbols.ROW, yytext() ); }
