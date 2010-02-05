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

package org.formulacompiler.compiler.internal.model.analysis;

import static org.formulacompiler.compiler.internal.expressions.ExpressionBuilder.*;

import java.util.Calendar;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.Operator;
import org.formulacompiler.compiler.internal.expressions.ArrayDescriptor;
import org.formulacompiler.compiler.internal.expressions.DataType;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForArrayReference;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForSwitch;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForSwitchCase;
import org.formulacompiler.compiler.internal.model.CellModel;
import org.formulacompiler.compiler.internal.model.ComputationModel;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForCellModel;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForParentSectionModel;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForSubSectionModel;
import org.formulacompiler.compiler.internal.model.SectionModel;

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


	private static class IFTester extends ConstantTester
	{
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
		new IFTester()
		{

			@Override
			protected void defineCell( SectionModel _root, CellModel _cell, Object _value )
			{
				_cell.setExpression( fun( Function.IF, cst( 1 ), cst( _value ), cst( _value ) ) );
			}

		}.run();
	}

	public void testIFWithUntypedThen() throws Exception
	{
		new IFTester()
		{

			@Override
			protected void defineCell( SectionModel _root, CellModel _cell, Object _value )
			{
				_cell.setExpression( fun( Function.IF, cst( 1 ), err( "Error!" ), cst( _value ) ) );
			}

		}.run();
	}

	public void testIFWithUntypedElse() throws Exception
	{
		new IFTester()
		{

			@Override
			protected void defineCell( SectionModel _root, CellModel _cell, Object _value )
			{
				_cell.setExpression( fun( Function.IF, cst( 1 ), cst( _value ), err( "Error!" ) ) );
			}

		}.run();
	}

	public void testIFWithIncompatibleTypes() throws Exception
	{
		ComputationModel m = new ComputationModel( In.class, Out.class );
		CellModel c = new CellModel( m.getRoot(), "Result" );
		c.setExpression( fun( Function.IF, cst( 1 ), cst( 2 ), cst( "str" ) ) );

		try {
			m.traverse( new TypeAnnotator() );
		} catch (CompilerException.DataTypeError e) {
			assertEquals( "Arguments of expression IF( 1, 2, \"str\" ) must have the same type.\n" +
					"Expression \"str\" has type STRING.\n" +
					"Expression 2 has type NUMERIC.\n" +
					"Cell containing expression is Result.\n" +
					"Referenced by cell Result.", e.getMessage() );
		}
	}

	public void testIFWithUndefinedTypes() throws Exception
	{
		ComputationModel m = new ComputationModel( In.class, Out.class );
		CellModel c = new CellModel( m.getRoot(), "Result" );
		c.setExpression( fun( Function.IF, cst( 1 ), err( "1" ), err( "2" ) ) );

		try {
			m.traverse( new TypeAnnotator() );
		} catch (CompilerException.DataTypeError e) {
			assertEquals( "Cannot determine type of expression IF( 1, ERROR( \"1\" ), ERROR( \"2\" ) ) because its argument(s) are untyped.\n" +
					"Cell containing expression is Result.\n" +
					"Referenced by cell Result.", e.getMessage() );
		}
	}

	public void testSwitchWithUntypedDefaultValue() throws Exception
	{
		new ConstantTester()
		{

			@Override
			protected void defineCell( final SectionModel _root, final CellModel _cell, final Object _value )
			{
				_cell.setExpression( new ExpressionNodeForSwitch( cst( 0 ), err( "Error!" ),
						new ExpressionNodeForSwitchCase( cst( _value ), 0 ),
						new ExpressionNodeForSwitchCase( cst( _value ), 1 ) ) );
			}

			@Override
			protected void checkCell( DataType _type, CellModel _cell )
			{
				final ExpressionNode e = _cell.getExpression();
				assertSame( _type, e.getDataType() );
				assertSame( DataType.NUMERIC, e.arguments().get( 0 ).getDataType() );
				assertSame( _type, e.arguments().get( 1 ).getDataType() );
				assertSame( _type, e.arguments().get( 2 ).getDataType() );
				assertSame( _type, e.arguments().get( 3 ).getDataType() );
				super.checkCell( _type, _cell );
			}

		}.run();
	}

	public void testSwitchWithUntypedValues() throws Exception
	{
		new ConstantTester()
		{

			@Override
			protected void defineCell( final SectionModel _root, final CellModel _cell, final Object _value )
			{
				_cell.setExpression( new ExpressionNodeForSwitch( cst( 0 ), cst( _value ),
						new ExpressionNodeForSwitchCase( err( "Error!" ), 0 ),
						new ExpressionNodeForSwitchCase( err( "Error!" ), 1 ) ) );
			}

			@Override
			protected void checkCell( DataType _type, CellModel _cell )
			{
				final ExpressionNode e = _cell.getExpression();
				assertSame( _type, e.getDataType() );
				assertSame( DataType.NUMERIC, e.arguments().get( 0 ).getDataType() );
				assertSame( _type, e.arguments().get( 1 ).getDataType() );
				assertSame( _type, e.arguments().get( 2 ).getDataType() );
				assertSame( _type, e.arguments().get( 3 ).getDataType() );
				super.checkCell( _type, _cell );
			}

		}.run();
	}

	public void testSwitchWithIncompatibleTypes() throws Exception
	{
		ComputationModel m = new ComputationModel( In.class, Out.class );
		CellModel c = new CellModel( m.getRoot(), "Result" );
		c.setExpression( new ExpressionNodeForSwitch( cst( 0 ), cst( 1 ),
				new ExpressionNodeForSwitchCase( cst( "str" ), 0 ) ) );

		try {
			m.traverse( new TypeAnnotator() );
		} catch (CompilerException.DataTypeError e) {
			assertEquals( "Arguments of expression SWITCH( 0, CASE( 0 ): \"str\", DEFAULT: 1 ) must have the same type.\n" +
					"Expression 1 has type NUMERIC.\n" +
					"Expression CASE( 0 ): \"str\" has type STRING.\n" +
					"Cell containing expression is Result.\n" +
					"Referenced by cell Result.", e.getMessage() );
		}
	}

	public void testSwitchWithUndefinedTypes() throws Exception
	{
		ComputationModel m = new ComputationModel( In.class, Out.class );
		CellModel c = new CellModel( m.getRoot(), "Result" );
		c.setExpression( new ExpressionNodeForSwitch( cst( 0 ), err( "1" ),
				new ExpressionNodeForSwitchCase( err( "2" ), 0 ) ) );

		try {
			m.traverse( new TypeAnnotator() );
		} catch (CompilerException.DataTypeError e) {
			assertEquals( "Cannot determine type of expression SWITCH( 0, CASE( 0 ): ERROR( \"2\" ), DEFAULT: ERROR( \"1\" ) ) because its argument(s) are untyped.\n" +
					"Cell containing expression is Result.\n" +
					"Referenced by cell Result.", e.getMessage() );
		}
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

				_cell.setExpression( fun( Function.INDEX, new ExpressionNodeForArrayReference(
						new ArrayDescriptor( 1, 1, 2 ), cell( rs ), cell( re ) ) ) );
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
			c.setExpression( fun( agg ) );

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


	private ExpressionNodeForCellModel cell( CellModel _cell )
	{
		return new ExpressionNodeForCellModel( _cell );
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
