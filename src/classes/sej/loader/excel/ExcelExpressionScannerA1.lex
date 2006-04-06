/*
 * Copyright © 2006 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are prohibited, unless you have been explicitly granted 
 * more rights by Abacus Research AG.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS 
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND 
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR 
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package sej.loader.excel;

/* Lexer for Excel formulas with A1-style cell references. Based on the one found in JExcelAPI.
*/

import java_cup.runtime.Symbol;
%%
%cupsym GeneratedSymbols
%cup
%char
%class GeneratedScannerA1
%implements ExcelExpressionScanner
%final
%apiprivate
%{
	public int charsRead() { return yychar; }
	private String source;
	public void setSource( String _source ) { this.source = _source; }
%}

%state YYSTRING

ALPHA	= [A-Za-z\_]
DIGIT	= [0-9]
SYMBOL	= [\$\!\%\^\&\*\#\_\=\+\;\#\~\@\/\?\.\,\<\>\|\:\-\[\]\\]
SPACE	= [\n\ \t\b\012]
QUOTE	= "'"

INT		= {DIGIT}+
DBL		= {DIGIT}+ "." {DIGIT}+

ABS		= "$"
CELL	= {ABS}? {ALPHA} {ALPHA}? {ABS}? {INT}
SHEET	= {QUOTE}? ({ALPHA} | {DIGIT} | {SPACE} | {SYMBOL})+ {QUOTE}? "!" 
		| {QUOTE} ({ALPHA} | {DIGIT} | {SPACE} | {SYMBOL} | "(" | ")")+ {QUOTE} "!"

NAME	= {ALPHA} {ALPHA} ({ALPHA} | {DIGIT})*


%%


"+"			{ return new Symbol( GeneratedSymbols.PLUS, "+" ); }
"-"			{ return new Symbol( GeneratedSymbols.MINUS, "-" ); }
"*"			{ return new Symbol( GeneratedSymbols.TIMES, "*" ); }
"/"			{ return new Symbol( GeneratedSymbols.DIV, "/" ); }
"^"			{ return new Symbol( GeneratedSymbols.EXP, "^" ); }
"%"			{ return new Symbol( GeneratedSymbols.PERCENT, "%" ); }
"&"			{ return new Symbol( GeneratedSymbols.CONCAT, "&" ); }
"("			{ return new Symbol( GeneratedSymbols.LPAREN, "(" ); }
")"			{ return new Symbol( GeneratedSymbols.RPAREN, " )" ); }
"["			{ return new Symbol( GeneratedSymbols.LBRACK, "[" ); }
"]"			{ return new Symbol( GeneratedSymbols.RBRACK, "]" ); }
","			{ return new Symbol( GeneratedSymbols.COMMA, "," ); }
":"			{ return new Symbol( GeneratedSymbols.COLON, ":" ); }

"="			{ return new Symbol( GeneratedSymbols.EQUAL, "=" ); }
">"			{ return new Symbol( GeneratedSymbols.GREATER, ">" ); }
">="		{ return new Symbol( GeneratedSymbols.GREATEROREQUAL, ">=" ); }
"<"			{ return new Symbol( GeneratedSymbols.LESS, "<" ); }
"<="		{ return new Symbol( GeneratedSymbols.LESSOREQUAL, "<=" ); }
"<>"		{ return new Symbol( GeneratedSymbols.NOTEQUAL, "<>" ); }

"IF"		{ return new Symbol( GeneratedSymbols.IF, "IF" ); }
"AND"		{ return new Symbol( GeneratedSymbols.AND, "AND" ); }
"OR"		{ return new Symbol( GeneratedSymbols.OR, "OR" ); }
"NOT"		{ return new Symbol( GeneratedSymbols.NOT, "NOT" ); }
"SUM"		{ return new Symbol( GeneratedSymbols.SUM, "SUM" ); }
"PRODUCT"	{ return new Symbol( GeneratedSymbols.PRODUCT, "PRODUCT" ); }
"AVERAGE"	{ return new Symbol( GeneratedSymbols.AVERAGE, "AVERAGE" ); }
"MIN"		{ return new Symbol( GeneratedSymbols.MIN, "MIN" ); }
"MAX"		{ return new Symbol( GeneratedSymbols.MAX, "MAX" ); }
"MATCH"		{ return new Symbol( GeneratedSymbols.MATCH, "MATCH" ); }
"INDEX"		{ return new Symbol( GeneratedSymbols.INDEX, "INDEX" ); }
"ROUND"		{ return new Symbol( GeneratedSymbols.ROUND, "ROUND" ); }
"TRUE"		{ return new Symbol( GeneratedSymbols.INT, new Integer(1) ); }
"FALSE"		{ return new Symbol( GeneratedSymbols.INT, new Integer(0) ); }

{CELL}		{ return new Symbol( GeneratedSymbols.CELLA1, yytext() ); }
{SHEET}		{ return new Symbol( GeneratedSymbols.SHEET, yytext() ); }

{INT}		{ return new Symbol( GeneratedSymbols.INT, new Integer( yytext() ) ); }
{DBL}		{ return new Symbol( GeneratedSymbols.DBL, new Double( yytext() ) ); }

{NAME}		{ return new Symbol( GeneratedSymbols.NAME, yytext() ); }

\"[^\"]*\"	{ return new Symbol( GeneratedSymbols.STRING, yytext() ); }

{SPACE}		{}

. { throw new ExcelExpressionError( "Unexpected character: '" + yytext() + "'", source, yychar ); }
