/*
 * Copyright (c) 2006-2009 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * This file is part of the Abacus Formula Compiler (AFC).
 *
 * For commercial licensing, please contact sales(at)formulacompiler.com.
 *
 * AFC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AFC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AFC.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.formulacompiler.spreadsheet.internal.loader;

import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.parser.CellRefFormat;
import org.formulacompiler.spreadsheet.internal.CellIndex;
import org.formulacompiler.spreadsheet.internal.CellRange;
import org.formulacompiler.spreadsheet.internal.CellRefParseException;
import org.formulacompiler.spreadsheet.internal.ExpressionNodeForCell;
import org.formulacompiler.spreadsheet.internal.SheetImpl;
import org.formulacompiler.spreadsheet.internal.SpreadsheetImpl;
import org.formulacompiler.spreadsheet.internal.parser.SpreadsheetExpressionParser;

import junit.framework.TestCase;

public class SpreadsheetExpressionParserTest extends TestCase
{
	private final SpreadsheetImpl workbook = new SpreadsheetImpl();
	@SuppressWarnings( "unused" ) // used to create the sheet into which we place cells
	private final SheetImpl sheet = new SheetImpl( this.workbook, "One" );
	private CellIndex parseRelativeTo = new CellIndex( this.workbook, 0, 1, 1 );


	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		CellIndex cellA1 = new CellIndex( this.workbook, 0, 0, 0 );
		CellIndex cellB1 = new CellIndex( this.workbook, 0, 1, 0 );
		CellIndex cellA2 = new CellIndex( this.workbook, 0, 0, 1 );
		CellIndex cellB2 = new CellIndex( this.workbook, 0, 1, 1 );
		this.workbook.defineModelRangeName( "_A1_", cellA1 );
		this.workbook.defineModelRangeName( "_B1_", cellB1 );
		this.workbook.defineModelRangeName( "_A2_", cellA2 );
		this.workbook.defineModelRangeName( "_B2_", cellB2 );
		this.workbook.defineModelRangeName( "_A_", CellRange.getCellRange( cellA1, cellA2 ) );
		this.workbook.defineModelRangeName( "_B_", CellRange.getCellRange( cellB1, cellB2 ) );
		this.workbook.defineModelRangeName( "_1_", CellRange.getCellRange( cellA1, cellB1 ) );
		this.workbook.defineModelRangeName( "_2_", CellRange.getCellRange( cellA2, cellB2 ) );
		this.workbook.defineModelRangeName( "_ALL_", CellRange.getCellRange( cellA1, cellB2 ) );
	}


	public void testRC() throws Exception
	{
		assertRefR1C1( "One!B2", "RC" );
		assertRefR1C1( "One!$A$1", "R1C1" );
		assertRefR1C1( "One!$A2", "RC1" );
		assertRefR1C1( "One!B$1", "R1C" );
		assertRefR1C1( "One!C2", "RC[1]" );
		assertRefR1C1( "One!A2", "RC[-1]" );
		assertRefR1C1( "One!A1", "R[-1]C[-1]" );
		assertRefR1C1( "One!B1", "R[-1]C" );
	}


	public void testA1() throws Exception
	{
		assertRefA1( "One!B2", "B2" );
		assertRefA1( "One!D4", "D4" );
		assertRefA1( "One!$D$4", "$D$4" );
		assertRefA1( "One!$D4", "$D4" );
		assertRefA1( "One!D$4", "D$4" );
		assertRefA1( "One!AD564", "AD564" );
		assertRefA1( "One!$FZ$4", "$FZ$4" );
		/*
		 * This tests the special case where an R1C1-style reference is a valid A1-style reference. It
		 * explains the need for the CellRefFormat parser option.
		 */
		assertRefA1( "One!RC1", "RC1" );
		try {
			assertRefA1( "One!R11", "R1C1" );
		} catch (CellRefParseException e) {
			assertEquals( "Invalid A1-style range or cell reference: R1C1", e.getMessage() );
		}
	}

	public void testRangesA1() throws Exception
	{
		new SheetImpl( this.workbook, "Two" );
		assertRangeRef( "One!A1:B1", "A1:B1", CellRefFormat.A1 );
		assertRangeRef( "One!$A$1:$B$1", "$A$1:$B$1", CellRefFormat.A1 );
		assertRangeRef( "Two!A1:B1", "Two!A1:B1", CellRefFormat.A1 );
		assertRangeRef( "One!A1:Two!B1", "One:Two!A1:B1", CellRefFormat.A1 );
	}

	public void testA1OOXML() throws Exception
	{
		assertRefA1OOXML( "One!B2", "B2" );
		assertRefA1OOXML( "One!D4", "D4" );
		assertRefA1OOXML( "One!$D$4", "$D$4" );
		assertRefA1OOXML( "One!$D4", "$D4" );
		assertRefA1OOXML( "One!D$4", "D$4" );
		assertRefA1OOXML( "One!AD564", "AD564" );
		assertRefA1OOXML( "One!$FZ$4", "$FZ$4" );
		/*
		 * This tests the special case where an R1C1-style reference is a valid A1-style reference. It
		 * explains the need for the CellRefFormat parser option.
		 */
		assertRefA1OOXML( "One!RC1", "RC1" );
		try {
			assertRefA1OOXML( "One!R11", "R1C1" );
		} catch (CellRefParseException e) {
			assertEquals( "Invalid OOXML A1-style range or cell reference: R1C1", e.getMessage() );
		}
	}

	public void testRangesA1OOXML() throws Exception
	{
		new SheetImpl( this.workbook, "Two" );
		assertRangeRef( "Two!A1:B1", "Two!A1:B1", CellRefFormat.A1_OOXML );
		assertRangeRef( "One!A1:Two!B1", "One:Two!A1:B1", CellRefFormat.A1_OOXML );
		assertRangeRef( "One!A$1:A$2147483647", "A:A", CellRefFormat.A1_OOXML );
		assertRangeRef( "One!A$1:B$2147483647", "A:B", CellRefFormat.A1_OOXML );
		assertRangeRef( "One!$A$1:A$2147483647", "$A:A", CellRefFormat.A1_OOXML );
		assertRangeRef( "One!A$1:$B$2147483647", "A:$B", CellRefFormat.A1_OOXML );
		assertRangeRef( "One!$A$1:$B$2147483647", "$A:$B", CellRefFormat.A1_OOXML );
		assertRangeRef( "One!$A1:$FXSHRXW1", "1:1", CellRefFormat.A1_OOXML );
		assertRangeRef( "One!$A1:$FXSHRXW2", "1:2", CellRefFormat.A1_OOXML );
		assertRangeRef( "One!$A$1:$FXSHRXW2", "$1:2", CellRefFormat.A1_OOXML );
		assertRangeRef( "One!$A1:$FXSHRXW$2", "1:$2", CellRefFormat.A1_OOXML );
		assertRangeRef( "One!$A$1:$FXSHRXW$2", "$1:$2", CellRefFormat.A1_OOXML );
	}

	public void testBrokenRefsOOXML() throws Exception
	{
		assertRefA1OOXML( "One!#REF!#REF!", "One!#REF!" );
		assertRefA1OOXML( "#REF!A1", "#REF!A1" );
		assertRefA1OOXML( "#REF!$A$1", "#REF!$A$1" );
		assertRefA1OOXML( "#REF!#REF!#REF!", "#REF!#REF!" );
	}


	public void testA1ByIndex() throws Exception
	{
		assertRef( 1, false, 1, false, "B2", CellRefFormat.A1 );
		assertRef( 3, false, 3, false, "D4", CellRefFormat.A1 );
		assertRef( 3, true, 3, true, "$D$4", CellRefFormat.A1 );
		assertRef( 3, true, 3, false, "$D4", CellRefFormat.A1 );
		assertRef( 3, false, 3, true, "D$4", CellRefFormat.A1 );
		assertRef( 29, false, 563, false, "AD564", CellRefFormat.A1 );
		assertRef( 181, true, 3, true, "$FZ$4", CellRefFormat.A1 );
	}


	public void testA1ODF() throws Exception
	{
		assertRefA1ODF( "One!B2", "[.B2]" );
		assertRefA1ODF( "One!D4", "[.D4]" );
		assertRefA1ODF( "One!$D$4", "[.$D$4]" );
		assertRefA1ODF( "One!$D4", "[.$D4]" );
		assertRefA1ODF( "One!D$4", "[.D$4]" );
		assertRefA1ODF( "One!AD564", "[.AD564]" );
		assertRefA1ODF( "One!$FZ$4", "[.$FZ$4]" );
		assertRefA1ODF( "One!RC1", "[.RC1]" );
		assertRefA1ODF( "One!#REF!#REF!", "[.#REF!#REF!]" );
		assertRefA1ODF( "One!$#REF!$#REF!", "[.$#REF!$#REF!]" );
		assertRefA1ODF( "One!A#REF!", "[.A#REF!]" );
		assertRefA1ODF( "One!#REF!1", "[.#REF!1]" );
	}


	public void testCellRefs() throws Exception
	{
		assertParseableA1( "((((One!$A$1 + _1_) + _2_) + _A_) + _B_)", "$A$1 + _1_ + _2_ + _A_ + _B_" );
		assertParseableA1ODF( "((((One!$A$1 + _1_) + _2_) + _A_) + _B_)", "[.$A$1] + _1_ + _2_ + _A_ + _B_" );
		assertParseableR1C1( "((((One!$A$1 + _1_) + _2_) + _A_) + _B_)", "R1C1 + _1_ + _2_ + _A_ + _B_" );
		assertParseableR1C1( "((((One!$A$1 + One!C3) + One!$A2) + One!B$1) + One!A1)", "R1C1 + R[1]C[1] + RC1 + R1C + R[-1]C[-1]" );
	}


	public void testOperators() throws Exception
	{
		assertParseableA1( "((((One!A1 + (-One!B1)) + One!B2) - One!B3) - (-One!B4))", "A1 + -B1 + +B2 - +B3 - -B4" );
		assertParseableA1OOXML( "((((One!A1 + (-One!B1)) + One!B2) - One!B3) - (-One!B4))", "A1 + -B1 + +B2 - +B3 - -B4" );
		assertParseableA1ODF( "((((One!A1 + (-One!B1)) + One!B2) - One!B3) - (-One!B4))", "[.A1] + -[.B1] + +[.B2] - +[.B3] - -[.B4]" );
		assertParseableA1( "(One!A1 + (One!A2 * One!A3))", "A1 + A2 * A3" );
		assertParseableA1OOXML( "(One!A1 + (One!A2 * One!A3))", "A1 + A2 * A3" );
		assertParseableA1ODF( "(One!A1 + (One!A2 * One!A3))", "[.A1] + [.A2] * [.A3]" );
	}


	public void testConcat() throws Exception
	{
		assertParseableAll( "(1.0 & 2.0 & 3.0 & 4.0)", "1 & 2 & 3 & 4" );
		assertParseableAll( "(1.0 & (2.0 & 3.0) & 4.0)", "1 & (2 & 3) & 4" );
		assertParseableA1( "(1.0 & 2.0 & 3.0 & 4.0)", "CONCATENATE( 1, 2, 3, 4 )" );
		assertParseableA1OOXML( "(1.0 & 2.0 & 3.0 & 4.0)", "CONCATENATE( 1, 2, 3, 4 )" );
		assertParseableA1ODF( "(1.0 & 2.0 & 3.0 & 4.0)", "CONCATENATE( 1; 2; 3; 4 )" );
		assertParseableR1C1( "(1.0 & 2.0 & 3.0 & 4.0)", "CONCATENATE( 1, 2, 3, 4 )" );
	}


	public void testCellAndRangeMixes() throws Exception
	{
		assertParseableA1( "SUM( One!A1:B2, One!C5, One!A1:B5 One!A2:E8, (One!A1 + One!A2) )", "SUM( A1:B2, C5, A1:B5 A2:E8, A1+A2 )" );
		assertParseableA1( "(SUM( _1_, _2_ _A_, _B_, _ALL_ ) + _2_)", "SUM( _1_, _2_ _A_, _B_, _ALL_ ) + _2_" );

		// LATER assertParseableA1( "(SUM( A1:A2, A1:A2 A1:A2 ))", "SUM( A1:_A2_, _A1_:A2 _A1_:_A2_ )"
		// );
	}


	public void testTrueFalse() throws Exception
	{
		assertParseableAll( "AND( true, false, true, false, true, false, true, false )",
				"AND( TRUE, FALSE, true, false, @TRUE, @FALSE, @true, @false )" );
	}


	public void testNames() throws Exception
	{
		final String[] names = { "_A1", "A.1", "A1.", "A_1", "A.B", "_A", "_1", "_.", "A.B.C", "R1C.", "R1C3A" };

		final CellIndex a1 = new CellIndex( this.workbook, 0, 0, 0 );
		for (String n : names) {
			this.workbook.defineModelRangeName( n, a1 );
		}

		for (String n : names) {
			assertParseableAll( "SUM( " + n + " )", "SUM( " + n + " )" );
		}
	}


	public void testOldStyleFunctions() throws Exception
	{
		assertParseableA1( "((1.0 + ROUND( One!A1, 2.0 )) + 2.0)", "1 + @ROUND(A1,2) + 2" );
		assertParseableA1( "((1.0 + ROUND( One!A1, 2.0 )) + 2.0)", "1 + ROUND(A1,2) + 2" );
	}


	public void testUnsupported() throws Exception
	{
		assertErr(
				"2 + SOMEFUNC(A2)",
				"Unsupported function SOMEFUNC encountered in expression 2 + SOMEFUNC( <<? A2); error location indicated by <<?.",
				CellRefFormat.A1 );
		assertErr(
				"2 + SOMEFUNC([.A2])",
				"Unsupported function SOMEFUNC encountered in expression 2 + SOMEFUNC( <<? [.A2]); error location indicated by <<?.",
				CellRefFormat.A1_ODF );
	}


	public void testReferenceToSecondarySheet() throws Exception
	{
		new SheetImpl( this.workbook, "Two" );
		new SheetImpl( this.workbook, "*()__ '123'!\"yes\"" );
		assertRefA1( "Two!A2", "Two!A2" );
		assertRefA1ODF( "Two!A2", "[Two.A2]" );
		assertRefR1C1( "Two!$A$2", "Two!R2C1" );
		assertRefA1( "'*()__ ''123''!\"yes\"'!A1", "'*()__ ''123''!\"yes\"'!A1" );
		assertRefA1ODF( "'*()__ ''123''!\"yes\"'!A1", "['*()__ ''123''!\"yes\"'.A1]" );
		assertRefR1C1( "'*()__ ''123''!\"yes\"'!$A$1", "'*()__ ''123''!\"yes\"'!R1C1" );
	}


	public void testReferenceFromSecondarySheet() throws Exception
	{
		new SheetImpl( this.workbook, "Two" );
		this.parseRelativeTo = new CellIndex( this.workbook, 1, 0, 0 );
		assertRefA1( "Two!A2", "A2" );
		assertRefA1( "One!A2", "One!A2" );
		assertRefA1ODF( "Two!A2", "[.A2]" );
		assertRefA1ODF( "One!A2", "[One.A2]" );
		assertRefR1C1( "Two!$A$2", "R2C1" );
		assertRefR1C1( "One!$A$2", "One!R2C1" );
	}


	public void testReferencesToSecondarySheet() throws Exception
	{
		new SheetImpl( this.workbook, "Two" );
		assertParseableA1( "(Two!A2 + Two!$B$1)", "Two!A2+Two!$B$1" );
		assertParseableA1ODF( "(Two!A2 + Two!$B$1)", "[Two.A2]+[$Two.$B$1]" );
		assertParseableA1OOXML( "(Two!A2 + Two!$B$1)", "Two!A2+Two!$B$1" );
		assertParseableR1C1( "(Two!A2 + Two!$B$1)", "Two!RC[-1]+Two!R1C2" );
	}


	private void assertParseableAll( String _expected, String _string ) throws Exception
	{
		assertParseableA1( _expected, _string );
		assertParseableA1ODF( _expected, _string );
		assertParseableA1OOXML( _expected, _string );
		assertParseableR1C1( _expected, _string );
	}


	private void assertParseableA1( String _expected, String _string ) throws Exception
	{
		assertParseable( _expected, _string, CellRefFormat.A1 );
	}


	private void assertParseableA1ODF( String _expected, String _string ) throws Exception
	{
		assertParseable( _expected, _string, CellRefFormat.A1_ODF );
	}


	private void assertParseableA1OOXML( String _expected, String _string ) throws Exception
	{
		assertParseable( _expected, _string, CellRefFormat.A1_OOXML );
	}


	private void assertParseableR1C1( String _expected, String _string ) throws Exception
	{
		assertParseable( _expected, _string, CellRefFormat.R1C1 );
	}


	private void assertParseable( String _expected, String _expr, CellRefFormat _format ) throws Exception
	{
		final ExpressionNode node = newParser( _expr, _format ).parse();
		final String actual = node.describe();
		assertEquals( _expected, actual );
	}


	private void assertRefA1( String _canonicalName, String _ref ) throws Exception
	{
		assertRef( _canonicalName, _ref, CellRefFormat.A1 );
	}


	private void assertRefA1OOXML( String _canonicalName, String _ref ) throws Exception
	{
		assertRef( _canonicalName, _ref, CellRefFormat.A1_OOXML );
	}


	private void assertRefA1ODF( String _canonicalName, String _ref ) throws Exception
	{
		assertRef( _canonicalName, _ref, CellRefFormat.A1_ODF );
	}


	private void assertRefR1C1( String _canonicalName, String _ref ) throws Exception
	{
		assertRef( _canonicalName, _ref, CellRefFormat.R1C1 );
	}


	private void assertRef( String _canonicalName, String _ref, CellRefFormat _format ) throws Exception
	{
		SpreadsheetExpressionParser parser = newParser( _ref, _format );
		ExpressionNode parsed = parser.parse();
		ExpressionNodeForCell node = (ExpressionNodeForCell) parsed;
		CellIndex ref = node.getCellIndex();
		String actual = ref.toString();
		assertEquals( _canonicalName, actual );
	}

	private void assertRangeRef( String _canonicalName, String _ref, CellRefFormat _format ) throws Exception
	{
		SpreadsheetExpressionParser parser = newParser( _ref, _format );
		CellRange parsed = (CellRange) parser.rangeOrCellRef();
		assertEquals( _canonicalName, parsed.toString() );
	}

	private void assertRef( int _columnIndex, boolean _columnAbsolute, int _rowIndex, boolean _rowAbsolute, String _ref, CellRefFormat _format ) throws Exception
	{
		SpreadsheetExpressionParser parser = newParser( _ref, _format );
		ExpressionNode parsed = parser.parse();
		ExpressionNodeForCell node = (ExpressionNodeForCell) parsed;
		CellIndex ref = node.getCellIndex();
		assertEquals( _columnIndex, ref.getColumnIndex() );
		assertEquals( _columnAbsolute, ref.isColumnIndexAbsolute );
		assertEquals( _rowIndex, ref.getRowIndex() );
		assertEquals( _rowAbsolute, ref.isRowIndexAbsolute );
	}


	private void assertErr( String _ref, String _msg, final CellRefFormat _format ) throws Exception
	{
		try {
			newParser( _ref, _format ).parse();
			fail( "Expected exception: " + _msg );
		}
		catch (Exception e) {
			assertEquals( _msg, e.getMessage() );
		}
	}


	private SpreadsheetExpressionParser newParser( String _ref, CellRefFormat _format )
	{
		return SpreadsheetExpressionParser.newParser( _ref, this.parseRelativeTo, _format );
	}

}
