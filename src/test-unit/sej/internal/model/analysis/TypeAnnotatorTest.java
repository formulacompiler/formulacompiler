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
package sej.internal.model.analysis;

import java.util.Calendar;

import sej.compiler.Function;
import sej.compiler.Operator;
import sej.internal.expressions.ArrayDescriptor;
import sej.internal.expressions.DataType;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForArrayReference;
import sej.internal.expressions.ExpressionNodeForConstantValue;
import sej.internal.expressions.ExpressionNodeForFunction;
import sej.internal.expressions.ExpressionNodeForOperator;
import sej.internal.model.CellModel;
import sej.internal.model.ComputationModel;
import sej.internal.model.ExpressionNodeForCellModel;
import sej.internal.model.ExpressionNodeForParentSectionModel;
import sej.internal.model.ExpressionNodeForSubSectionModel;
import sej.internal.model.SectionModel;
import junit.framework.TestCase;


public class TypeAnnotatorTest extends TestCase
{


	private static class ConstantTester
	{

		public void run() throws Exception
		{
			assertConstant( DataType.NUMERIC, 123 );
			assertConstant( DataType.NUMERIC, 123.4 );
			assertConstant( DataType.NUMERIC, 123L );
			assertConstant( DataType.NUMERIC, Calendar.getInstance().getTime() );
			assertConstant( DataType.STRING, "Hello world." );
			assertConstant( DataType.STRING, "" );
			assertConstant( DataType.NULL, null );
		}

		private void assertConstant( DataType _type, Object _value ) throws Exception
		{
			ComputationModel m = new ComputationModel( In.class, Out.class );
			SectionModel r = m.getRoot();

			CellModel c = defineCell( r, _value );

			m.traverse( new TypeAnnotator() );

			checkCell( _type, c );
		}

		protected CellModel defineCell( SectionModel _root, Object _value )
		{
			CellModel cell = new CellModel( _root, "Result" );
			defineCell( _root, cell, _value );
			return cell;
		}

		protected void defineCell( SectionModel _root, CellModel _cell, Object _value )
		{
			_cell.setConstantValue( _value );
		}

		protected void checkCell( DataType _type, CellModel _cell )
		{
			assertSame( _type, _cell.getDataType() );
		}

	}


	public void testConstants() throws Exception
	{
		new ConstantTester().run();
	}


	public void testConstantRefs() throws Exception
	{
		new ConstantTester()
		{

			@Override
			protected void defineCell( SectionModel _root, CellModel _cell, Object _value )
			{
				_cell.setExpression( cst( _value ) );
			}

			@Override
			protected void checkCell( DataType _type, CellModel _cell )
			{
				assertSame( _type, _cell.getExpression().getDataType() );
				super.checkCell( _type, _cell );
			}

		}.run();
	}


	public void testConstantCellRefs() throws Exception
	{
		new ConstantTester()
		{
			private CellModel refd;

			@Override
			protected void defineCell( SectionModel _root, CellModel _cell, Object _value )
			{
				this.refd = new CellModel( _root, "Refd" );
				this.refd.setConstantValue( _value );
				_cell.setExpression( cell( this.refd ) );
			}

			@Override
			protected void checkCell( DataType _type, CellModel _cell )
			{
				assertSame( _type, this.refd.getDataType() );
				assertSame( _type, _cell.getExpression().getDataType() );
				super.checkCell( _type, _cell );
			}

		}.run();
	}


	public void testRanges() throws Exception
	{
		assertRange( DataType.NULL );
		assertRange( DataType.NULL, (Object) null );
		assertRange( DataType.NULL, null, null );
		assertRange( DataType.NUMERIC, 1, 2, 3 );
		assertRange( DataType.NUMERIC, 1, null, 3 );
		assertRange( DataType.NUMERIC, null, null, 3 );
		assertRange( DataType.NUMERIC, 1, 2, null );
		assertRange( DataType.NUMERIC, 1, "Test", 3 );
		assertRange( DataType.NUMERIC, "Test", "Test", 3 );
		assertRange( DataType.NUMERIC, 1, 2, "Test" );
		assertRange( DataType.STRING, "Test" );
		assertRange( DataType.STRING, "Test", "More" );
		assertRange( DataType.STRING, null, "Test", "More" );
		assertRange( DataType.STRING, "Test", null, "More" );
		assertRange( DataType.STRING, "Test", "More", null );
	}

	private void assertRange( DataType _type, Object... _values ) throws Exception
	{
		ComputationModel m = new ComputationModel( In.class, Out.class );
		SectionModel r = m.getRoot();
		ExpressionNode[] cells = new ExpressionNode[ _values.length ];
		for (int i = 0; i < _values.length; i++) {
			Object value = _values[ i ];
			CellModel cell = new CellModel( r, "RangeCell_" + i );
			cell.setConstantValue( value );
			cells[ i ] = cell( cell );
		}
		CellModel c = new CellModel( r, "Result" );
		c.setExpression( new ExpressionNodeForArrayReference( new ArrayDescriptor( 1, 1, _values.length ), cells ) );

		m.traverse( new TypeAnnotator() );

		assertSame( _type, c.getExpression().getDataType() );
		assertSame( _type, c.getDataType() );
	}


	public void testOperators() throws Exception
	{
		for (Operator op : Operator.values()) {
			ComputationModel m = new ComputationModel( In.class, Out.class );
			SectionModel r = m.getRoot();
			CellModel c = new CellModel( r, "Result" );
			c.setExpression( op( op ) );

			m.traverse( new TypeAnnotator() );

			DataType expected;
			switch (op) {
				case CONCAT:
					expected = DataType.STRING;
					break;
				default:
					expected = DataType.NUMERIC;
			}
			assertSame( expected, c.getExpression().getDataType() );
			assertSame( expected, c.getDataType() );
		}
	}


	public void testFunctions() throws Exception
	{
		for (Function fun : new Function[] { Function.MATCH, Function.NOT, Function.ROUND, Function.TODAY }) {
			ComputationModel m = new ComputationModel( In.class, Out.class );
			SectionModel r = m.getRoot();
			CellModel c = new CellModel( r, "Result" );
			c.setExpression( fun( fun ) );

			m.traverse( new TypeAnnotator() );

			DataType expected = DataType.NUMERIC;
			assertSame( expected, c.getExpression().getDataType() );
			assertSame( expected, c.getDataType() );
		}
	}

	public void testIF() throws Exception
	{
		new ConstantTester()
		{

			@Override
			protected void defineCell( SectionModel _root, CellModel _cell, Object _value )
			{
				_cell.setExpression( fun( Function.IF, cst( 1 ), cst( _value ), cst( _value ) ) );
			}

			@Override
			protected void checkCell( DataType _type, CellModel _cell )
			{
				final ExpressionNode e = _cell.getExpression();
				assertSame( _type, e.getDataType() );
				assertSame( DataType.NUMERIC, e.arguments().get( 0 ).getDataType() );
				assertSame( _type, e.arguments().get( 1 ).getDataType() );
				assertSame( _type, e.arguments().get( 2 ).getDataType() );
				super.checkCell( _type, _cell );
			}

		}.run();
	}

	public void testINDEX() throws Exception
	{
		new ConstantTester()
		{

			@Override
			protected void defineCell( SectionModel _root, CellModel _cell, Object _value )
			{
				CellModel rs = new CellModel( _root, "RangeStart" );
				rs.setConstantValue( _value );
				CellModel re = new CellModel( _root, "RangeEnd" );
				re.setConstantValue( _value );

				_cell.setExpression( fun( Function.INDEX, new ExpressionNodeForArrayReference( new ArrayDescriptor( 1, 1, 2 ),
						cell( rs ), cell( re ) ) ) );
			}

			@Override
			protected void checkCell( DataType _type, CellModel _cell )
			{
				final ExpressionNode e = _cell.getExpression();
				assertSame( _type, e.getDataType() );
				assertSame( _type, e.arguments().get( 0 ).getDataType() );
				super.checkCell( _type, _cell );
			}

		}.run();
	}


	public void testAggregators() throws Exception
	{
		for (Function agg : Function.aggregators()) {
			ComputationModel m = new ComputationModel( In.class, Out.class );
			SectionModel r = m.getRoot();
			CellModel c = new CellModel( r, "Result" );
			c.setExpression( agg( agg ) );

			m.traverse( new TypeAnnotator() );

			DataType expected = DataType.NUMERIC;
			assertSame( expected, c.getExpression().getDataType() );
			assertSame( expected, c.getDataType() );
		}
	}


	public void testOuterConstantCellRefs() throws Exception
	{
		new ConstantTester()
		{
			private CellModel refd;

			@Override
			protected CellModel defineCell( SectionModel _root, Object _value )
			{
				this.refd = new CellModel( _root, "Refd" );
				this.refd.setConstantValue( _value );
				SectionModel inner = new SectionModel( _root, "Inner", In.class, Out.class );
				CellModel cell = new CellModel( inner, "Result" );
				cell.setExpression( new ExpressionNodeForParentSectionModel( _root, cell( this.refd ) ) );
				return cell;
			}

			@Override
			protected void checkCell( DataType _type, CellModel _cell )
			{
				assertSame( _type, this.refd.getDataType() );
				assertSame( _type, _cell.getExpression().getDataType() );
				assertSame( _type, _cell.getExpression().arguments().get( 0 ).getDataType() );
				super.checkCell( _type, _cell );
			}

		}.run();
	}


	public void testInnerConstantCellAggregations() throws Exception
	{
		new ConstantTester()
		{
			private CellModel refd;

			@Override
			protected CellModel defineCell( SectionModel _root, Object _value )
			{
				SectionModel inner = new SectionModel( _root, "Inner", In.class, Out.class );
				this.refd = new CellModel( inner, "Refd" );
				this.refd.setConstantValue( _value );

				CellModel cell = new CellModel( _root, "Result" );
				cell.setExpression( new ExpressionNodeForSubSectionModel( inner, cell( this.refd ) ) );
				return cell;
			}

			@Override
			protected void checkCell( DataType _type, CellModel _cell )
			{
				assertSame( _type, this.refd.getDataType() );
				assertSame( _type, _cell.getExpression().getDataType() );
				assertSame( _type, _cell.getExpression().arguments().get( 0 ).getDataType() );
				super.checkCell( _type, _cell );
			}

		}.run();
	}


	public void testNull() throws Exception
	{
		ComputationModel m = new ComputationModel( In.class, Out.class );
		SectionModel r = m.getRoot();
		CellModel c = new CellModel( r, "Result" );
		c.setExpression( new ExpressionNodeForArrayReference( new ArrayDescriptor( 1, 1, 2 ), null, null ) );

		m.traverse( new TypeAnnotator() );

		assertSame( DataType.NULL, c.getExpression().getDataType() );
		assertSame( DataType.NULL, c.getDataType() );
	}


	private ExpressionNodeForConstantValue cst( Object _value )
	{
		return new ExpressionNodeForConstantValue( _value );
	}

	private ExpressionNodeForCellModel cell( CellModel _cell )
	{
		return new ExpressionNodeForCellModel( _cell );
	}

	private ExpressionNodeForOperator op( Operator _op, ExpressionNode... _args )
	{
		return new ExpressionNodeForOperator( _op, _args );
	}

	private ExpressionNodeForFunction fun( Function _fun, ExpressionNode... _args )
	{
		return new ExpressionNodeForFunction( _fun, _args );
	}

	private ExpressionNodeForFunction agg( Function _agg, ExpressionNode... _args )
	{
		return new ExpressionNodeForFunction( _agg, _args );
	}


	public static interface In
	{
		// dummy
	}

	public static interface Out
	{
		// dummy
	}

}
