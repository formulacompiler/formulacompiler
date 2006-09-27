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
package sej.internal.spreadsheet.loader.excel;

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
	public int charsRead() { return zzMarkedPos; }
	private String source;
	public void setSource( String _source ) { this.source = _source; }
%}

%state YYSTRING

ALPHA	= [A-Za-z]
IDENT	= {ALPHA} | {DIGIT} | "_" | "."
DIGIT	= [0-9]
SYMBOL	= [^\']
SPACE	= [\n\ \t\b\012]
QUOTE	= "'"

INT		= {DIGIT}+
DBL		= {DIGIT}+ "." {DIGIT}+

ABS		= "$"
CELL	= {ABS}? {ALPHA} {ALPHA}? {ABS}? {INT}
SHEET	= ({IDENT})+ "!" 
		| {QUOTE} ({IDENT} | {SPACE} | {SYMBOL})+ {QUOTE} "!"

NAME	= ({ALPHA} | "_") {IDENT}*

FN		= "@"?


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

{FN} "IF"		{ return new Symbol( GeneratedSymbols.IF, "IF" ); }
{FN} "AND"		{ return new Symbol( GeneratedSymbols.AND, "AND" ); }
{FN} "OR"		{ return new Symbol( GeneratedSymbols.OR, "OR" ); }
{FN} "NOT"		{ return new Symbol( GeneratedSymbols.NOT, "NOT" ); }
{FN} "SUM"		{ return new Symbol( GeneratedSymbols.SUM, "SUM" ); }
{FN} "PRODUCT"	{ return new Symbol( GeneratedSymbols.PRODUCT, "PRODUCT" ); }
{FN} "COUNT"	{ return new Symbol( GeneratedSymbols.COUNT, "COUNT" ); }
{FN} "AVERAGE"	{ return new Symbol( GeneratedSymbols.AVERAGE, "AVERAGE" ); }
{FN} "MIN"		{ return new Symbol( GeneratedSymbols.MIN, "MIN" ); }
{FN} "MAX"		{ return new Symbol( GeneratedSymbols.MAX, "MAX" ); }
{FN} "MATCH"	{ return new Symbol( GeneratedSymbols.MATCH, "MATCH" ); }
{FN} "INDEX"	{ return new Symbol( GeneratedSymbols.INDEX, "INDEX" ); }
{FN} "ROUND"	{ return new Symbol( GeneratedSymbols.ROUND, "ROUND" ); }
{FN} "TODAY"	{ return new Symbol( GeneratedSymbols.TODAY, "TODAY" ); }
{FN} "CONCATENATE"	{ return new Symbol( GeneratedSymbols.CONCATENATE, "CONCATENATE" ); }
{FN} "LEN"		{ return new Symbol( GeneratedSymbols.LEN, "LEN" ); }
{FN} "MID"		{ return new Symbol( GeneratedSymbols.MID, "MID" ); }
{FN} "LEFT"		{ return new Symbol( GeneratedSymbols.LEFT, "LEFT" ); }
{FN} "RIGHT"	{ return new Symbol( GeneratedSymbols.RIGHT, "RIGHT" ); }
{FN} "SUBSTITUTE"	{ return new Symbol( GeneratedSymbols.SUBSTITUTE, "SUBSTITUTE" ); }
{FN} "REPLACE"	{ return new Symbol( GeneratedSymbols.REPLACE, "REPLACE" ); }
{FN} "SEARCH"	{ return new Symbol( GeneratedSymbols.SEARCH, "SEARCH" ); }
{FN} "FIND"		{ return new Symbol( GeneratedSymbols.FIND, "FIND" ); }
{FN} "EXACT"	{ return new Symbol( GeneratedSymbols.EXACT, "EXACT" ); }
{FN} "LOWER"	{ return new Symbol( GeneratedSymbols.LOWER, "LOWER" ); }
{FN} "UPPER"	{ return new Symbol( GeneratedSymbols.UPPER, "UPPER" ); }
{FN} "PROPER"	{ return new Symbol( GeneratedSymbols.PROPER, "PROPER" ); }

{FN} "TRUE"		{ return new Symbol( GeneratedSymbols.INT, new Integer(1) ); }
{FN} "true"		{ return new Symbol( GeneratedSymbols.INT, new Integer(1) ); }
{FN} "FALSE"	{ return new Symbol( GeneratedSymbols.INT, new Integer(0) ); }
{FN} "false"	{ return new Symbol( GeneratedSymbols.INT, new Integer(0) ); }

{CELL}		{ return new Symbol( GeneratedSymbols.CELLA1, yytext() ); }
{SHEET}		{ return new Symbol( GeneratedSymbols.SHEET, yytext() ); }

{INT}		{ return new Symbol( GeneratedSymbols.INT, new Integer( yytext() ) ); }
{DBL}		{ return new Symbol( GeneratedSymbols.DBL, new Double( yytext() ) ); }

{NAME}		{ return new Symbol( GeneratedSymbols.NAME, yytext() ); }

\"[^\"]*\"	{ return new Symbol( GeneratedSymbols.STRING, yytext() ); }

{SPACE}		{}

. { throw new ExcelExpressionParserError( "Unexpected character: '" + yytext() + "'", source, yychar ); }
