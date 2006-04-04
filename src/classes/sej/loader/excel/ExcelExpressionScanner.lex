package sej.loader.excel;

import java_cup.runtime.Symbol;
%%
%cup
%%

"+"		{ return new Symbol(sym.PLUS); }
"-"		{ return new Symbol(sym.MINUS); }
"*"		{ return new Symbol(sym.TIMES); }
"/"		{ return new Symbol(sym.DIV); }
"^"		{ return new Symbol(sym.EXP); }
"%"		{ return new Symbol(sym.PERCENT); }
"&"		{ return new Symbol(sym.CONCAT); }
"("		{ return new Symbol(sym.LPAREN); }
")"		{ return new Symbol(sym.RPAREN); }
"["		{ return new Symbol(sym.LBRACK); }
"]"		{ return new Symbol(sym.RBRACK); }
","		{ return new Symbol(sym.COMMA); }
":"		{ return new Symbol(sym.COLON); }
"$"		{ return new Symbol(sym.DOLLAR); }

"="		{ return new Symbol(sym.EQUAL); }
">"		{ return new Symbol(sym.GREATER); }
">="	{ return new Symbol(sym.GREATEROREQUAL); }
"<"		{ return new Symbol(sym.LESS); }
"<="	{ return new Symbol(sym.LESSOREQUAL); }
"<>"	{ return new Symbol(sym.NOTEQUAL); }

"IF"		{ return new Symbol(sym.IF); }
"AND"		{ return new Symbol(sym.AND); }
"OR"		{ return new Symbol(sym.OR); }
"NOT"		{ return new Symbol(sym.NOT); }
"SUM"		{ return new Symbol(sym.SUM); }
"PRODUCT"	{ return new Symbol(sym.PRODUCT); }
"AVERAGE"	{ return new Symbol(sym.AVERAGE); }
"MIN"		{ return new Symbol(sym.MIN); }
"MAX"		{ return new Symbol(sym.MAX); }
"MATCH"		{ return new Symbol(sym.MATCH); }
"INDEX"		{ return new Symbol(sym.INDEX); }
"ROUND"		{ return new Symbol(sym.ROUND); }
"TRUE"		{ return new Symbol(sym.INT, new Integer(1)); }
"FALSE"		{ return new Symbol(sym.INT, new Integer(0)); }

[0-9]+ { return new Symbol(sym.INT, new Integer(yytext())); }
[0-9]+ ("." [0-9]+)? { return new Symbol(sym.DBL, new Double(yytext())); }

[A-Za-z][A-Za-z0-9_]* { return new Symbol(sym.IDENT, yytext()); }

\"[^\"]*\" { return new Symbol(sym.STRING, yytext()); }

[ \t\r\n\f] { /* ignore white space. */ }

. { throw new ExcelExpressionError("Illegal character: "+yytext()); }
