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

package org.formulacompiler.compiler.internal.expressions.parser;

import java.util.Collection;
import java.util.Set;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.DescriptionBuilder;
import org.formulacompiler.compiler.internal.expressions.ExpressionDescriptionConfig;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.formulacompiler.runtime.New;

import junit.framework.TestCase;

public class ExpressionParserTest extends TestCase
{
	private final Set<String> definedNames = New.set();


	public void testIntConst() throws Exception
	{
		assertInteger( "123", 123 );
		assertInteger( "1234567890", 1234567890 );
	}

	private void assertInteger( String _expr, int _expected ) throws Exception
	{
		ExpressionNodeForConstantValue parsed = (ExpressionNodeForConstantValue) parse( _expr, null );
		assertEquals( _expected, ((Number) parsed.value()).intValue() );
	}


	public void testDblConst() throws Exception
	{
		assertDouble( "123", 123 );
		assertDouble( "123", 123 );
		assertDouble( "123.45", 123.45 );
		assertDouble( "123.45E10", 123.45E10 );
		assertDouble( "123.45E-10", 123.45E-10 );
		assertDouble( "123.45E+10", 123.45E10 );
	}

	private void assertDouble( String _expr, double _expected ) throws Exception
	{
		ExpressionNodeForConstantValue parsed = (ExpressionNodeForConstantValue) parse( _expr, null );
		assertEquals( _expected, ((Number) parsed.value()).doubleValue(), 0.000001 );
	}


	public void testStrConst() throws Exception
	{
		assertString( "\"Hello world, this is a test!\"", "Hello world, this is a test!" );
		assertString( "\"Quote: \"\" \"", "Quote: \" " );
	}

	private void assertString( String _expr, String _expected ) throws Exception
	{
		ExpressionNodeForConstantValue parsed = (ExpressionNodeForConstantValue) parse( _expr, null );
		assertEquals( _expected, (String) parsed.value() );
	}


	public void testCellA1() throws Exception
	{
		assertParseA1( "\"R<R_A1>A1>\"", "A1" );
		assertParseA1( "\"R<R_A1>AA1>\"", "AA1" );
		assertParseA1( "\"R<R_A1>ZAZ987>\"", "ZAZ987" );
		assertParseA1( "\"R<R_A1>$A$1>\"", "$A$1" );
		assertParseA1( "\"R<R_A1>A$1>\"", "A$1" );
		assertParseA1( "\"R<R_A1>$A1>\"", "$A1" );
		assertParseA1( "\"R<R_A1>Sheet1!A1>\"", "Sheet1!A1" );
		assertParseA1( "\"R<R_A1>'Sheet 1'!A1>\"", "'Sheet 1'!A1" );
		assertParseA1( "\"R<R_A1>RC1>\"", "RC1" );
	}

	public void testRangesA1() throws Exception
	{
		assertParseA1( "SUM( \"R<R_A1>Sheet2!A1:B1>\" )", "SUM(Sheet2!A1:B1)" );
		assertParseA1( "SUM( \"R<R_A1>A1:Sheet2!B1>\" )", "SUM(A1:Sheet2!B1)" );
		assertParseA1( "SUM( \"R<R_A1>Sheet1!A1:Sheet2!B1>\" )", "SUM(Sheet1!A1:Sheet2!B1)" );
	}

	public void testCellA1ODF() throws Exception
	{
		assertParseA1ODF( "\"R<A1>.A1>\"", "[.A1]" );
		assertParseA1ODF( "\"R<A1>.AA1>\"", "[.AA1]" );
		assertParseA1ODF( "\"R<A1>.ZAZ987>\"", "[.ZAZ987]" );
		assertParseA1ODF( "\"R<A1>.$A$1>\"", "[.$A$1]" );
		assertParseA1ODF( "\"R<A1>.A$1>\"", "[.A$1]" );
		assertParseA1ODF( "\"R<A1>.$A1>\"", "[.$A1]" );
		assertParseA1ODF( "\"R<A1>.RC1>\"", "[.RC1]" );
	}

	public void testRangesODF() throws Exception
	{
		assertParseA1ODF( "SUM( \"R<A1>.A1:A1>(A1>.A1).B1>\" )", "SUM( [.A1:.B1] )" );
		assertParseA1ODF( "SUM( \"R<A1>Sheet1.A1:A1>(A1>Sheet1.A1).B1>\" )", "SUM( [Sheet1.A1:.B1] )" );
		assertParseA1ODF( "SUM( \"R<A1>Sheet1.A1:A1>(A1>Sheet1.A1)Sheet2.B1>\" )", "SUM( [Sheet1.A1:Sheet2.B1] )" );
	}

	public void testRangesOOXML() throws Exception
	{
		assertRangeOrCellRefOOXML( "R_A1>A1", "A1" );
		assertRangeOrCellRefOOXML( "R_A1>A1:B2", "A1:B2" );
		assertRangeOrCellRefOOXML( "R_A1>1:1", "1:1" );
		assertRangeOrCellRefOOXML( "R_A1>A:A", "A:A" );
		assertRangeOrCellRefOOXML( "R_A1>Sheet1:Sheet2!A1:B1", "Sheet1:Sheet2!A1:B1" );
		assertRangeOrCellRefOOXML( "R_A1>Sheet1:Sheet2!A1", "Sheet1:Sheet2!A1" );
		assertRangeOrCellRefOOXML( "R_A1>'Sheet 1':'Sheet 2'!A1:B1", "'Sheet 1':'Sheet 2'!A1:B1" );
		assertRangeOrCellRefOOXML( "R_A1>'Sheet 1:Sheet 2'!A1:B1", "'Sheet 1:Sheet 2'!A1:B1" );
	}

	public void testBrokenRefsOOXML() throws Exception
	{
		assertRangeOrCellRefOOXML( "R_A1>Sheet1!#REF!", "Sheet1!#REF!" );
		assertRangeOrCellRefOOXML( "R_A1>#REF!A1", "#REF!A1" );
		assertRangeOrCellRefOOXML( "R_A1>#REF!$A$1", "#REF!$A$1" );
		assertRangeOrCellRefOOXML( "R_A1>#REF!#REF!", "#REF!#REF!" );
	}

	public void testExprWithRangesOOXML() throws Exception
	{
		assertParseA1( "SUM( \"R<R_A1>1:1>\" )", "SUM(1:1)" );
		assertParseA1( "SUM( \"R<R_A1>A:A>\" )", "SUM(A:A)" );
		assertParseA1( "SUM( \"R<R_A1>Sheet1:Sheet2!A1:B1>\" )", "SUM(Sheet1:Sheet2!A1:B1)" );
		assertParseA1( "SUM( \"R<R_A1>Sheet1:Sheet2!A1>\" )", "SUM(Sheet1:Sheet2!A1)" );
		assertParseA1( "SUM( \"R<R_A1>'Sheet 1':'Sheet 2'!A1:B1>\" )", "SUM('Sheet 1':'Sheet 2'!A1:B1)" );
	}

	public void testCellR1C1() throws Exception
	{
		assertParseR1C1( "\"R<A1>R1C1>\"", "R1C1" );
		assertParseR1C1( "\"R<A1>RC1>\"", "RC1" );
	}

	public void testNamedCellRef() throws Exception
	{
		this.definedNames.add( "Hello" );
		this.definedNames.add( "World" );
		this.definedNames.add( "_foo_13" );
		assertParseA1( "(\"NR>Hello\" + \"NR>World\")", "Hello + World" );
		assertParseA1( "(1.0 + \"NR>_foo_13\")", "1 + _foo_13" );
	}


	public void testPercentTerm() throws Exception
	{
		assertParseA1( "(-(1.2345E-8%))", "-123.45E-10%" );
	}

	public void testSignedTerm() throws Exception
	{
		assertParseA1( "(-3.0)", "-3" );
		assertParseA1( "(-3.0)", "-(3)" );
		assertParseA1( "(-(-3.0))", "--3" );
		assertParseA1( "(-(-3.0))", "--(3)" );
		assertParseA1( "(-3.0)", "+-3" );
		assertParseA1( "(-3.0)", "-+3" );
	}

	public void testExpTerm() throws Exception
	{
		assertParseA1( "(3.0 ^ 4.0)", "3^4" );
		assertParseA1( "((3.0%) ^ 4.0)", "3%^4" );
		assertParseA1( "(3.0 ^ (4.0%))", "3^4%" );
		assertParseA1( "((-(3.0%)) ^ (-4.0))", "-3%^-4" );
		assertParseA1( "((-3.0) ^ (-(4.0%)))", "-3^-4%" );
		assertParseA1( "(((-(1.2345E-8%)) ^ (-(1.2345E-7%))) ^ (-(1.2345E-6%)))", "-123.45E-10%^-123.45E-9%^-123.45E-8%" );
		assertParseA1( "((-(1.2345E-8%)) ^ (((-(1.2345E-7%)) ^ (-1.2345E-6))%))", "-123.45E-10%^(-123.45E-9%^-123.45E-8)%" );
	}

	public void testMulTerm() throws Exception
	{
		assertParseA1( "(3.0 * 4.0)", "3 * 4" );
		assertParseA1( "(3.0 * (4.0 ^ 5.0))", "3 * 4 ^ 5" );
		assertParseA1( "(3.0 / 4.0)", "3 / 4" );
		assertParseA1( "(3.0 / (4.0 ^ 5.0))", "3 / 4 ^ 5" );
		assertParseA1( "((3.0 * 4.0) / 5.0)", "3 * 4 / 5" );
		assertParseA1( "((3.0 / 4.0) * 5.0)", "3 / 4 * 5" );
	}

	public void testAddTerm() throws Exception
	{
		assertParseA1( "(3.0 - 4.0)", "3-4" ); // Tests that -4 is not lexed as the number -4
		assertParseA1( "(3.0 + 4.0)", "3 + 4" );
		assertParseA1( "(3.0 + (4.0 * 5.0))", "3 + 4 * 5" );
		assertParseA1( "(3.0 - 4.0)", "3 - 4" );
		assertParseA1( "(3.0 - (4.0 * 5.0))", "3 - 4 * 5" );
		assertParseA1( "((3.0 + 4.0) - 5.0)", "3 + 4 - 5" );
		assertParseA1( "((3.0 - 4.0) + 5.0)", "3 - 4 + 5" );
		assertParseA1( "(3.0 + (4.0 * (5.0 ^ 6.0)))", "3 + 4 * 5 ^ 6" );
		assertParseA1( "((((-3.0) - (-3.0)) - 3.0) + 3.0)", "-3 - -3 - +3 + +3" );
		assertParseA1( "(((-(3.0 + 4.0)) * (-(4.0 + 5.0))) / (4.0 - 5.0))", "-(3 + 4) * -(4 + 5) / +(+4 - +5)" );
	}

	public void testConcatTerm() throws Exception
	{
		assertParseA1( "(3.0 & 4.0)", "3 & 4" );
		assertParseA1( "(3.0 & (4.0 + 5.0))", "3 & 4+ 5" );
		assertParseA1( "(3.0 & 4.0 & 5.0)", "3 & 4 & 5" );
		assertParseA1( "(3.0 & (4.0 & 5.0))", "3 & (4 & 5)" );
	}

	public void testComparisonTerm() throws Exception
	{
		assertParseA1( "(3.0 = 4.0)", "3 = 4" );
		assertParseA1( "(3.0 <> 4.0)", "3 <> 4" );
		assertParseA1( "(3.0 > 4.0)", "3 > 4" );
		assertParseA1( "(3.0 >= 4.0)", "3 >= 4" );
		assertParseA1( "(3.0 < 4.0)", "3 < 4" );
		assertParseA1( "(3.0 <= 4.0)", "3 <= 4" );
		assertParseA1( "(3.0 < (4.0 & 5.0))", "3 < 4 & 5" );
	}

	public void testMinMaxTerm() throws Exception
	{
		assertParseA1( "(3.0 _min_ 4.0)", "3 _min_ 4" );
		assertParseA1( "(3.0 _max_ 4.0)", "3 _max_ 4" );
		assertParseA1( "(3.0 _max_ (4.0 = 5.0))", "3 _max_ 4 = 5" );
	}

	public void testFun() throws Exception
	{
		assertParseA1( "ABS( (-12.0) )", "ABS(-12)" );
		assertParseA1( "ABS( (-12.0) )", "@ABS(-12)" );

		assertParseA1( "(3.0 ^ ABS( (4.0 + 5.0) ))", "3 ^ ABS(4+5)" );
		assertParseA1( "(3.0 ^ ABS( 4.0 ))", "3 ^ @ABS(4)" );

		assertParseA1( "MATCH( 3.0, \"R<R_A1>A1:A2>\" )", "MATCH(3, A1:A2)" );
		assertParseA1( "MATCH( 3.0, \"R<R_A1>A1:A2>\" )", "MATCH(3, A1:A2, )" );
		assertParseA1( "MATCH( 3.0, \"R<R_A1>A1:A2>\", 1.0 )", "MATCH(3, A1:A2, 1)" );
	}

	public void testAgg() throws Exception
	{
		this.definedNames.add( "MyRange" );
		this.definedNames.add( "MyCell" );
		assertParseA1( "SUM( 1.0 )", "SUM(1)" );
		assertParseA1( "SUM( 1.0 )", "@SUM(1)" );

		assertParseA1( "SUM( 1.0, 2.0, 3.0 )", "SUM(1, 2, 3)" );
//		assertParseA1( "SUM( 1.0 2.0, 3.0 )", "SUM( 1 2, 3 )" );
		assertParseA1( "SUM( \"R<R_A1>A1>\", \"R<R_A1>A5>\" )", "SUM( A1, A5 )" );
		assertParseA1( "SUM( \"R<R_A1>A1:A5>\" )", "SUM( A1:A5 )" );
		assertParseA1( "SUM( \"NR>MyRange\", \"NR>MyCell\" )", "SUM( MyRange, MyCell )" );

		assertParseA1( "SUM( \"NR>MyRange\", SUM( \"NR>MyRange\" ) )", "SUM( MyRange, SUM(MyRange))" );

		assertParseA1( "(SUM( 1.0 ) - 1.0)", "SUM(1)-1" );
	}


	private ExpressionNode parse( String _expr, CellRefFormat _cellRefFormat ) throws CompilerException
	{
		return new TestExpressionParser( _expr, _cellRefFormat ).parse();
	}

	private void assertParseA1( String _expected, String _expr ) throws Exception
	{
		ExpressionNode parsed = parse( _expr, CellRefFormat.A1 );
		assertEquals( _expected, parsed.toString() );
	}

	private void assertParseA1ODF( String _expected, String _expr ) throws Exception
	{
		ExpressionNode parsed = parse( _expr, CellRefFormat.A1_ODF );
		assertEquals( _expected, parsed.toString() );
	}

	private void assertParseR1C1( String _expected, String _expr ) throws Exception
	{
		ExpressionNode parsed = parse( _expr, CellRefFormat.R1C1 );
		assertEquals( _expected, parsed.toString() );
	}


	private Object rangeOrCellRefA1( String _expr ) throws ParseException
	{
		return new TestExpressionParser( _expr, CellRefFormat.A1_OOXML ).rangeOrCellRefA1();
	}

	private void assertRangeOrCellRefOOXML( String _expected, String _expr ) throws Exception
	{
		Object parsed = rangeOrCellRefA1( _expr );
		assertEquals( _expected, parsed.toString() );
	}


	private final class TestExpressionParser extends ExpressionParser
	{

		public TestExpressionParser( String _exprText, CellRefFormat _cellRefFormat )
		{
			super( _exprText, _cellRefFormat );
		}

		@Override
		protected Object makeCell( Token _cell, Object _baseCell )
		{
			final StringBuilder sb = new StringBuilder( "A1>" );
			if (_baseCell != null) {
				sb.append( "(" ).append( _baseCell ).append( ")" );
			}
			sb.append( _cell.image );
			return sb.toString();
		}

		@Override
		protected ExpressionNode makeNamedRangeRef( Token _name )
		{
			return new ExpressionNodeForConstantValue( "NR>" + _name.image );
		}

		@Override
		protected boolean isRangeName( Token _name )
		{
			return ExpressionParserTest.this.definedNames.contains( _name.image );
		}

		@Override
		protected ExpressionNode makeRangeUnion( Collection<ExpressionNode> _firstTwoElements )
		{
			return new TestExpressionNode( _firstTwoElements )
			{

				@Override
				protected void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg )
				{
					describeArgumentTo( _to, _cfg, 0 );
					for (int iArg = 1; iArg < arguments().size(); iArg++) {
						_to.append( ", " );
						describeArgumentTo( _to, _cfg, iArg );
					}
				}

			};
		}

		@Override
		protected ExpressionNode makeRangeIntersection( Collection<ExpressionNode> _firstTwoElements )
		{
			return new TestExpressionNode( _firstTwoElements )
			{

				@Override
				protected void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg )
				{
					describeArgumentTo( _to, _cfg, 0 );
					for (int iArg = 1; iArg < arguments().size(); iArg++) {
						_to.append( " " );
						describeArgumentTo( _to, _cfg, iArg );
					}
				}

			};
		}

		@Override
		protected String makeCellRange( Object _from, Object _to )
		{
			return _from + ":" + _to;
		}

		@Override
		protected Object makeCellRange( final Token _range )
		{
			return "R_A1>" + _range.image;
		}

		@Override
		protected void convertRangesToCells( final boolean _allowRanges )
		{
			// Nothing to do here.
		}

		@Override
		protected ExpressionNode makeNodeForReference( Object _reference )
		{
			return new ExpressionNodeForConstantValue( "R<" + _reference + ">" );
		}

		@Override
		protected ExpressionNode makeShapedRange( ExpressionNode _range )
		{
			return _range;
		}


		private abstract class TestExpressionNode extends ExpressionNode
		{

			protected TestExpressionNode( Collection<ExpressionNode> _args )
			{
				super( _args );
			}

			@Override
			protected int countValuesCore( Collection<ExpressionNode> _uncountables )
			{
				throw new UnsupportedOperationException();
			}

			@Override
			protected ExpressionNode innerCloneWithoutArguments()
			{
				throw new UnsupportedOperationException();
			}

		}

	}

}
