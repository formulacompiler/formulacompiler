/*
 * Copyright (c) 2006, 2008 by Abacus Research AG, Switzerland.
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
import java.util.Iterator;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.DescriptionBuilder;
import org.formulacompiler.compiler.internal.expressions.ExpressionDescriptionConfig;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;

import junit.framework.TestCase;

public class ExpressionParserTest extends TestCase
{


	public void testIntConst() throws Exception
	{
		assertInteger( "123", 123 );
		assertInteger( "1234567890", 1234567890 );
	}

	private void assertInteger( String _expr, int _expected ) throws Exception
	{
		ExpressionNodeForConstantValue parsed = (ExpressionNodeForConstantValue) parse( _expr );
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
		ExpressionNodeForConstantValue parsed = (ExpressionNodeForConstantValue) parse( _expr );
		assertEquals( _expected, ((Number) parsed.value()).doubleValue(), 0.000001 );
	}


	public void testStrConst() throws Exception
	{
		assertString( "\"Hello world, this is a test!\"", "Hello world, this is a test!" );
		assertString( "\"Quote: \"\" \"", "Quote: \" " );
	}

	private void assertString( String _expr, String _expected ) throws Exception
	{
		ExpressionNodeForConstantValue parsed = (ExpressionNodeForConstantValue) parse( _expr );
		assertEquals( _expected, (String) parsed.value() );
	}


	public void testCellA1() throws Exception
	{
		assertParse( "\"A1>A1\"", "A1" );
		assertParse( "\"A1>AA1\"", "AA1" );
		assertParse( "\"A1>ZAZ987\"", "ZAZ987" );
		assertParse( "\"A1>$A$1\"", "$A$1" );
		assertParse( "\"A1>A$1\"", "A$1" );
		assertParse( "\"A1>$A1\"", "$A1" );
		/*
		 * The following is an anomaly which will have to be handled by the proper Excel A1 parser.
		 */
		assertParse( "\"RC>RC1\"", "RC1" );
	}

	public void testCellA1ODF() throws Exception
	{
		assertParse( "\"A1ODF>.A1\"", "[.A1]" );
		assertParse( "\"A1ODF>.AA1\"", "[.AA1]" );
		assertParse( "\"A1ODF>.ZAZ987\"", "[.ZAZ987]" );
		assertParse( "\"A1ODF>.$A$1\"", "[.$A$1]" );
		assertParse( "\"A1ODF>.A$1\"", "[.A$1]" );
		assertParse( "\"A1ODF>.$A1\"", "[.$A1]" );
		assertParse( "\"A1ODF>.RC1\"", "[.RC1]" );
	}

	public void testRangesODF() throws Exception
	{
		assertParse( "SUM( \"R<A1ODF>.A1:A1ODF>(\"A1ODF>.A1\").B1>\" )", "SUM( [.A1:.B1] )" );
		assertParse( "SUM( \"R<A1ODF>Sheet1.A1:A1ODF>(\"A1ODF>Sheet1.A1\").B1>\" )", "SUM( [Sheet1.A1:.B1] )" );
		assertParse( "SUM( \"R<A1ODF>Sheet1.A1:A1ODF>(\"A1ODF>Sheet1.A1\")Sheet2.B1>\" )", "SUM( [Sheet1.A1:Sheet2.B1] )" );
	}

	public void testCellR1C1() throws Exception
	{
		assertParse( "\"RC>R1C1\"", "R1C1" );
		assertParse( "\"RC>RC1\"", "RC1" );
	}

	public void testNamedCellRef() throws Exception
	{
		assertParse( "(\"NC>Hello\" + \"NC>World\")", "Hello + World" );
		assertParse( "(1.0 + \"NC>_foo_13\")", "1 + _foo_13" );
	}


	public void testPercentTerm() throws Exception
	{
		assertParse( "(-(1.2345E-8%))", "-123.45E-10%" );
	}

	public void testSignedTerm() throws Exception
	{
		assertParse( "(-3.0)", "-3" );
		assertParse( "(-3.0)", "-(3)" );
		assertParse( "(-(-3.0))", "--3" );
		assertParse( "(-(-3.0))", "--(3)" );
		assertParse( "(-3.0)", "+-3" );
		assertParse( "(-3.0)", "-+3" );
	}

	public void testExpTerm() throws Exception
	{
		assertParse( "(3.0 ^ 4.0)", "3^4" );
		assertParse( "((3.0%) ^ 4.0)", "3%^4" );
		assertParse( "(3.0 ^ (4.0%))", "3^4%" );
		assertParse( "((-(3.0%)) ^ (-4.0))", "-3%^-4" );
		assertParse( "((-3.0) ^ (-(4.0%)))", "-3^-4%" );
		assertParse( "(((-(1.2345E-8%)) ^ (-(1.2345E-7%))) ^ (-(1.2345E-6%)))", "-123.45E-10%^-123.45E-9%^-123.45E-8%" );
		assertParse( "((-(1.2345E-8%)) ^ (((-(1.2345E-7%)) ^ (-1.2345E-6))%))", "-123.45E-10%^(-123.45E-9%^-123.45E-8)%" );
	}

	public void testMulTerm() throws Exception
	{
		assertParse( "(3.0 * 4.0)", "3 * 4" );
		assertParse( "(3.0 * (4.0 ^ 5.0))", "3 * 4 ^ 5" );
		assertParse( "(3.0 / 4.0)", "3 / 4" );
		assertParse( "(3.0 / (4.0 ^ 5.0))", "3 / 4 ^ 5" );
		assertParse( "((3.0 * 4.0) / 5.0)", "3 * 4 / 5" );
		assertParse( "((3.0 / 4.0) * 5.0)", "3 / 4 * 5" );
	}

	public void testAddTerm() throws Exception
	{
		assertParse( "(3.0 - 4.0)", "3-4" ); // Tests that -4 is not lexed as the number -4
		assertParse( "(3.0 + 4.0)", "3 + 4" );
		assertParse( "(3.0 + (4.0 * 5.0))", "3 + 4 * 5" );
		assertParse( "(3.0 - 4.0)", "3 - 4" );
		assertParse( "(3.0 - (4.0 * 5.0))", "3 - 4 * 5" );
		assertParse( "((3.0 + 4.0) - 5.0)", "3 + 4 - 5" );
		assertParse( "((3.0 - 4.0) + 5.0)", "3 - 4 + 5" );
		assertParse( "(3.0 + (4.0 * (5.0 ^ 6.0)))", "3 + 4 * 5 ^ 6" );
		assertParse( "((((-3.0) - (-3.0)) - 3.0) + 3.0)", "-3 - -3 - +3 + +3" );
		assertParse( "(((-(3.0 + 4.0)) * (-(4.0 + 5.0))) / (4.0 - 5.0))", "-(3 + 4) * -(4 + 5) / +(+4 - +5)" );
	}

	public void testConcatTerm() throws Exception
	{
		assertParse( "(3.0 & 4.0)", "3 & 4" );
		assertParse( "(3.0 & (4.0 + 5.0))", "3 & 4+ 5" );
		assertParse( "(3.0 & 4.0 & 5.0)", "3 & 4 & 5" );
		assertParse( "(3.0 & (4.0 & 5.0))", "3 & (4 & 5)" );
	}

	public void testComparisonTerm() throws Exception
	{
		assertParse( "(3.0 = 4.0)", "3 = 4" );
		assertParse( "(3.0 <> 4.0)", "3 <> 4" );
		assertParse( "(3.0 > 4.0)", "3 > 4" );
		assertParse( "(3.0 >= 4.0)", "3 >= 4" );
		assertParse( "(3.0 < 4.0)", "3 < 4" );
		assertParse( "(3.0 <= 4.0)", "3 <= 4" );
		assertParse( "(3.0 < (4.0 & 5.0))", "3 < 4 & 5" );
	}

	public void testMinMaxTerm() throws Exception
	{
		assertParse( "(3.0 _min_ 4.0)", "3 _min_ 4" );
		assertParse( "(3.0 _max_ 4.0)", "3 _max_ 4" );
		assertParse( "(3.0 _max_ (4.0 = 5.0))", "3 _max_ 4 = 5" );
	}

	public void testFun() throws Exception
	{
		assertParse( "ABS( (-12.0) )", "ABS(-12)" );
		assertParse( "ABS( (-12.0) )", "@ABS(-12)" );

		assertParse( "(3.0 ^ ABS( (4.0 + 5.0) ))", "3 ^ ABS(4+5)" );
		assertParse( "(3.0 ^ ABS( 4.0 ))", "3 ^ @ABS(4)" );

		assertParse( "MATCH( 3.0, \"R<A1>A1:A1>(\"A1>A1\")A2>\" )", "MATCH(3, A1:A2)" );
		assertParse( "MATCH( 3.0, \"R<A1>A1:A1>(\"A1>A1\")A2>\" )", "MATCH(3, A1:A2, )" );
		assertParse( "MATCH( 3.0, \"R<A1>A1:A1>(\"A1>A1\")A2>\", 1.0 )", "MATCH(3, A1:A2, 1)" );
	}

	public void testAgg() throws Exception
	{
		assertParse( "SUM( 1.0 )", "SUM(1)" );
		assertParse( "SUM( 1.0 )", "@SUM(1)" );

		assertParse( "SUM( 1.0, 2.0, 3.0 )", "SUM(1, 2, 3)" );
		assertParse( "SUM( 1.0 2.0, 3.0 )", "SUM( 1 2, 3 )" );
		assertParse( "SUM( \"R<A1>A1:A1>(\"A1>A1\")A5>\" )", "SUM( A1:A5 )" );
		assertParse( "SUM( \"NR>MyRange\", \"NC>MyCell\" )", "SUM( MyRange, MyCell )" );

		assertParse( "SUM( \"NR>MyRange\", SUM( \"NR>MyRange\" ) )", "SUM( MyRange, SUM(MyRange))" );

		assertParse( "(SUM( 1.0 ) - 1.0)", "SUM(1)-1" );
	}


	private ExpressionNode parse( String _expr ) throws CompilerException
	{
		return new TestExpressionParser( _expr ).parse();
	}

	private void assertParse( String _expected, String _expr ) throws Exception
	{
		ExpressionNode parsed = parse( _expr );
		assertEquals( _expected, parsed.toString() );
	}


	private static final class TestExpressionParser extends ExpressionParser
	{

		public TestExpressionParser( String _exprText )
		{
			super( _exprText );
		}

		@Override
		protected ExpressionNode makeCellA1( Token _cell, ExpressionNode _node )
		{
			final StringBuilder sb = new StringBuilder( "A1>" );
			if (_node != null) {
				sb.append( "(" ).append( _node ).append( ")" );
			}
			sb.append( _cell.image );
			return new ExpressionNodeForConstantValue( sb.toString() );
		}

		@Override
		protected ExpressionNode makeCellA1ODF( Token _cell, ExpressionNode _node )
		{
			final StringBuilder sb = new StringBuilder( "A1ODF>" );
			if (_node != null) {
				sb.append( "(" ).append( _node ).append( ")" );
			}
			sb.append( _cell.image );
			return new ExpressionNodeForConstantValue( sb.toString() );
		}

		@Override
		protected ExpressionNode makeCellR1C1( Token _cell, ExpressionNode _node )
		{
			return new ExpressionNodeForConstantValue( "RC>" + _cell.image );
		}

		@Override
		protected ExpressionNode makeNamedCellRef( Token _name )
		{
			return new ExpressionNodeForConstantValue( "NC>" + _name.image );
		}

		@Override
		protected ExpressionNode makeNamedRangeRef( Token _name )
		{
			return new ExpressionNodeForConstantValue( "NR>" + _name.image );
		}

		@Override
		protected boolean isRangeName( Token _name )
		{
			return _name.image.equalsIgnoreCase( "myRange" );
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
		protected String makeCellRange( Collection<ExpressionNode> _firstTwoElements )
		{
			Iterator<ExpressionNode> i = _firstTwoElements.iterator();
			return ((ExpressionNodeForConstantValue) i.next()).value()
					+ ":" + ((ExpressionNodeForConstantValue) i.next()).value();
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


		private static abstract class TestExpressionNode extends ExpressionNode
		{

			protected TestExpressionNode()
			{
				super();
			}

			protected TestExpressionNode( Collection<ExpressionNode> _args )
			{
				super( _args );
			}

			protected TestExpressionNode( ExpressionNode... _args )
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
