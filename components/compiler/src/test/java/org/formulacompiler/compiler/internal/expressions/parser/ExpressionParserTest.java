/*
 * Copyright (c) 2006 by Abacus Research AG, Switzerland.
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
package org.formulacompiler.compiler.internal.expressions.parser;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.expressions.ExpressionDescriptionConfig;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.formulacompiler.describable.DescriptionBuilder;

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
	}

	private void assertString( String _expr, String _expected ) throws Exception
	{
		ExpressionNodeForConstantValue parsed = (ExpressionNodeForConstantValue) parse( _expr );
		assertEquals( _expected, (String) parsed.value() );
	}


	public void testCellA1() throws Exception
	{
		assertParse( "A1", "\"A1>A1\"" );
		assertParse( "AA1", "\"A1>AA1\"" );
		assertParse( "ZAZ987", "\"A1>ZAZ987\"" );
		assertParse( "$A$1", "\"A1>$A$1\"" );
		assertParse( "A$1", "\"A1>A$1\"" );
		assertParse( "$A1", "\"A1>$A1\"" );
		/*
		 * The following is an anomaly which will have to be handled by the proper Excel A1 parser.
		 */
		assertParse( "RC1", "\"RC>RC1\"" );
	}

	public void testCellR1C1() throws Exception
	{
		assertParse( "R1C1", "\"RC>R1C1\"" );
		assertParse( "RC1", "\"RC>RC1\"" );
	}

	public void testNamedCellRef() throws Exception
	{
		assertParse( "Hello + World", "(\"NC>Hello\" + \"NC>World\")" );
		assertParse( "1 + _foo_13", "(1 + \"NC>_foo_13\")" );
	}


	public void testPercentTerm() throws Exception
	{
		assertParse( "-123.45E-10%", "(-(1.2345E-8%))" );
	}

	public void testSignedTerm() throws Exception
	{
		assertParse( "-3", "(-3)" );
		assertParse( "-(3)", "(-3)" );
		assertParse( "--3", "(-(-3))" );
		assertParse( "--(3)", "(-(-3))" );
		assertParse( "+-3", "(-3)" );
		assertParse( "-+3", "(-3)" );
	}

	public void testExpTerm() throws Exception
	{
		assertParse( "3^4", "(3 ^ 4)" );
		assertParse( "3%^4", "((3%) ^ 4)" );
		assertParse( "3^4%", "(3 ^ (4%))" );
		assertParse( "-3%^-4", "((-(3%)) ^ (-4))" );
		assertParse( "-3^-4%", "((-3) ^ (-(4%)))" );
		assertParse( "-123.45E-10%^-123.45E-9%^-123.45E-8%", "(((-(1.2345E-8%)) ^ (-(1.2345E-7%))) ^ (-(1.2345E-6%)))" );
		assertParse( "-123.45E-10%^(-123.45E-9%^-123.45E-8)%", "((-(1.2345E-8%)) ^ (((-(1.2345E-7%)) ^ (-1.2345E-6))%))" );
	}

	public void testMulTerm() throws Exception
	{
		assertParse( "3 * 4", "(3 * 4)" );
		assertParse( "3 * 4 ^ 5", "(3 * (4 ^ 5))" );
		assertParse( "3 / 4", "(3 / 4)" );
		assertParse( "3 / 4 ^ 5", "(3 / (4 ^ 5))" );
		assertParse( "3 * 4 / 5", "((3 * 4) / 5)" );
		assertParse( "3 / 4 * 5", "((3 / 4) * 5)" );
	}

	public void testAddTerm() throws Exception
	{
		assertParse( "3-4", "(3 - 4)" ); // Tests that -4 is not lexed as the number -4
		assertParse( "3 + 4", "(3 + 4)" );
		assertParse( "3 + 4 * 5", "(3 + (4 * 5))" );
		assertParse( "3 - 4", "(3 - 4)" );
		assertParse( "3 - 4 * 5", "(3 - (4 * 5))" );
		assertParse( "3 + 4 - 5", "((3 + 4) - 5)" );
		assertParse( "3 - 4 + 5", "((3 - 4) + 5)" );
		assertParse( "3 + 4 * 5 ^ 6", "(3 + (4 * (5 ^ 6)))" );
		assertParse( "-3 - -3 - +3 + +3", "((((-3) - (-3)) - 3) + 3)" );
		assertParse( "-(3 + 4) * -(4 + 5) / +(+4 - +5)", "(((-(3 + 4)) * (-(4 + 5))) / (4 - 5))" );
	}

	public void testConcatTerm() throws Exception
	{
		assertParse( "3 & 4", "(3 & 4)" );
		assertParse( "3 & 4+ 5", "(3 & (4 + 5))" );
		assertParse( "3 & 4 & 5", "(3 & 4 & 5)" );
		assertParse( "3 & (4 & 5)", "(3 & (4 & 5))" );
	}

	public void testComparisonTerm() throws Exception
	{
		assertParse( "3 = 4", "(3 = 4)" );
		assertParse( "3 <> 4", "(3 <> 4)" );
		assertParse( "3 > 4", "(3 > 4)" );
		assertParse( "3 >= 4", "(3 >= 4)" );
		assertParse( "3 < 4", "(3 < 4)" );
		assertParse( "3 <= 4", "(3 <= 4)" );
		assertParse( "3 < 4 & 5", "(3 < (4 & 5))" );
	}

	public void testMinMaxTerm() throws Exception
	{
		assertParse( "3 _min_ 4", "(3 _min_ 4)" );
		assertParse( "3 _max_ 4", "(3 _max_ 4)" );
		assertParse( "3 _max_ 4 = 5", "(3 _max_ (4 = 5))" );
	}

	public void testFun() throws Exception
	{
		assertParse( "ABS(-12)", "ABS( (-12) )" );
		assertParse( "@ABS(-12)", "ABS( (-12) )" );

		assertParse( "3 ^ ABS(4+5)", "(3 ^ ABS( (4 + 5) ))" );
		assertParse( "3 ^ @ABS(4)", "(3 ^ ABS( 4 ))" );

		assertParse( "MATCH(3, A1:A2)", "MATCH( 3, \"R<A1>A1:A1>A2>\" )" );
		assertParse( "MATCH(3, A1:A2, )", "MATCH( 3, \"R<A1>A1:A1>A2>\" )" );
		assertParse( "MATCH(3, A1:A2, 1)", "MATCH( 3, \"R<A1>A1:A1>A2>\", 1 )" );
	}

	public void testAgg() throws Exception
	{
		assertParse( "SUM(1)", "SUM( 1 )" );
		assertParse( "@SUM(1)", "SUM( 1 )" );

		assertParse( "SUM(1, 2, 3)", "SUM( 1, 2, 3 )" );
		assertParse( "SUM( 1 2, 3 )", "SUM( 1 2, 3 )" );
		assertParse( "SUM( A1:A5 )", "SUM( \"R<A1>A1:A1>A5>\" )" );
		assertParse( "SUM( MyRange, MyCell )", "SUM( \"NR>MyRange\", \"NC>MyCell\" )" );
		
		assertParse( "SUM( MyRange, SUM(MyRange))", "SUM( \"NR>MyRange\", SUM( \"NR>MyRange\" ) )" );
		
		assertParse( "SUM(1)-1", "(SUM( 1 ) - 1)" );
	}

	public void testRewrite() throws Exception
	{
		assertParse( "_FOLD( acc: 0; xi: `acc + `xi; `args )", "_FOLD( acc: 0; xi: (`acc + `xi); `args )" );
		assertParse( "_FOLD_OR_REDUCE( acc: 0; xi: `acc + `xi; `args )",
				"_FOLD_OR_REDUCE( acc: 0; xi: (`acc + `xi); `args )" );
		assertParse( "_REDUCE( acc, xi: `acc + `xi; 0; `args )", "_REDUCE( acc, xi: (`acc + `xi); 0; `args )" );
		assertParse( "_FOLD_ARRAY( acc: 0; xi, i: `acc + `xi; `args )",
				"_FOLD_ARRAY( acc: 0; xi, i: (`acc + `xi); `args )" );
	}


	private ExpressionNode parse( String _expr ) throws CompilerException
	{
		return new TestExpressionParser( _expr ).parse();
	}

	private void assertParse( String _expr, String _expected ) throws Exception
	{
		ExpressionNode parsed = parse( _expr );
		assertEquals( _expected, parsed.toString() );
	}


	private static final class TestExpressionParser extends ExpressionParser
	{

		public TestExpressionParser(String _exprText)
		{
			super( _exprText );
		}

		@Override
		protected ExpressionNode makeCellA1( Token _cell )
		{
			return new ExpressionNodeForConstantValue( "A1>" + _cell.image );
		}

		@Override
		protected ExpressionNode makeCellA1( Token _cell, Token _sheet )
		{
			return new ExpressionNodeForConstantValue( "A1>" + _sheet.image + _cell.image );
		}

		@Override
		protected ExpressionNode makeCellR1C1( Token _cell )
		{
			return new ExpressionNodeForConstantValue( "RC>" + _cell.image );
		}

		@Override
		protected ExpressionNode makeCellR1C1( Token _cell, Token _sheet )
		{
			return new ExpressionNodeForConstantValue( "RC>" + _sheet.image + _cell.image );
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
						throws IOException
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
						throws IOException
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

			protected TestExpressionNode(Collection _args)
			{
				super( _args );
			}

			protected TestExpressionNode(ExpressionNode... _args)
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