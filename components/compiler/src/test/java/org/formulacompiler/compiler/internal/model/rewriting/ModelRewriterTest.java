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

package org.formulacompiler.compiler.internal.model.rewriting;

import org.formulacompiler.compiler.FormulaCompiler;
import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.Operator;
import org.formulacompiler.compiler.internal.CallFrameImpl;
import org.formulacompiler.compiler.internal.expressions.ArrayDescriptor;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForArrayReference;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFunction;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForOperator;
import org.formulacompiler.compiler.internal.model.CellModel;
import org.formulacompiler.compiler.internal.model.ComputationModel;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForCellModel;
import org.formulacompiler.compiler.internal.model.SectionModel;
import org.formulacompiler.compiler.internal.model.interpreter.InterpretedNumericType;
import org.formulacompiler.tests.utils.Inputs;
import org.formulacompiler.tests.utils.OutputsWithoutReset;

import junit.framework.TestCase;

@SuppressWarnings( "unqualified-field-access" )
public class ModelRewriterTest extends TestCase
{
	private final ComputationModel engineModel = new ComputationModel( Inputs.class, OutputsWithoutReset.class );
	private final SectionModel rootModel = engineModel.getRoot();


	public void testSUM() throws Exception
	{
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel b = new CellModel( rootModel, "b" );
		final CellModel c = new CellModel( rootModel, "c" );
		final CellModel r = new CellModel( rootModel, "r" );

		r.setExpression( new ExpressionNodeForFunction( Function.SUM, new ExpressionNode[] {
				new ExpressionNodeForCellModel( a ), new ExpressionNodeForCellModel( b ),
				new ExpressionNodeForCellModel( c ) } ) );

		engineModel.traverse( new ModelRewriter( InterpretedNumericType.typeFor( FormulaCompiler.DOUBLE ) ) );

		assertEquals( "apply (fold/reduce with s__1 = 0.0 each xi__2 as s__1 = (s__1 + xi__2)) to list {@( a, b, c )}", r
				.getExpression().toString() );
	}


	public void testDSUM() throws Exception
	{
		final ExpressionNode table = makeRange( new Object[][] {
				new Object[] { "Tree", "Height", "Age", "Yield", "Profit" },
				new Object[] { "Apple", 18.0, 20.0, 14.0, 105.0 }, new Object[] { "Pear", 12.0, 12.0, 10.0, 96.0 } } );

		final ExpressionNode crit = makeRange( new Object[][] { new Object[] { "Tree", "Height", "Height" },
				new Object[] { "Apple", ">10", "<16" }, new Object[] { "Pear", null, null } } );

		final ExpressionNode col = new ExpressionNodeForConstantValue( "Yield" );

		final CellModel r = new CellModel( rootModel, "r" );
		r.setExpression( new ExpressionNodeForFunction( Function.DSUM, table, col, crit ) );

		engineModel.traverse( new ModelRewriter( InterpretedNumericType.typeFor( FormulaCompiler.DOUBLE ) ) );

		assertBeginsWith(
				"apply (fold/reduce with s__1 = 0.0 each xi__2 as s__1 = (s__1 + xi__2)) to db filter col3_: OR( AND( (col3_0 = \"Apple\"), (col3_1",
				r.getExpression().toString() );
	}


	public void testDSUMWithDynamicCriteria() throws Exception
	{
		final ExpressionNode table = makeRange( new Object[][] {
				new Object[] { "Tree", "Height", "Age", "Yield", "Profit" },
				new Object[] { "Apple", 18.0, 20.0, 14.0, 105.0 }, new Object[] { "Pear", 12.0, 12.0, 10.0, 96.0 } } );

		final ExpressionNodeForArrayReference crit = makeRange( new Object[][] {
				new Object[] { "Tree", "Height", "Height" },
				new Object[] { "#a", compare( ">", "#b" ), compare( "<", "#c" ) }, new Object[] { "#d", null, null } } );

		final ExpressionNode col = new ExpressionNodeForConstantValue( "Yield" );

		final CellModel r = new CellModel( rootModel, "r" );
		r.setExpression( new ExpressionNodeForFunction( Function.DSUM, table, col, crit ) );

		engineModel.traverse( new ModelRewriter( InterpretedNumericType.typeFor( FormulaCompiler.DOUBLE ) ) );

		assertBeginsWith(
				"(let/byname crit4_0 = Inputs.getOne() in (let/byname crit4_1 = Inputs.getOne() in (let/byname crit4_2 = Inputs.getOne() in (let/byname crit4_3 = Inputs.getOne() in apply (fold/reduce with s__1 = 0.0 each xi__2 as s__1 = (s__1 + xi__2)) to db filter col3_: OR( AND( (col3_0 = ",
				r.getExpression().toString() );
	}


	private static void assertBeginsWith( String _expected, String _actual )
	{
		final int expLen = _expected.length();
		final int actLen = _actual.length();
		final int compLen = (expLen > actLen) ? actLen : expLen;
		assertEquals( _expected, _actual.substring( 0, compLen ) );
	}


	private final ExpressionNodeForArrayReference makeRange( Object[][] _rows ) throws Exception
	{
		final int nrows = _rows.length;
		final int ncols = _rows[ 0 ].length;

		final ExpressionNodeForArrayReference result = new ExpressionNodeForArrayReference( new ArrayDescriptor( 1,
				nrows, ncols ) );

		for (Object[] row : _rows) {
			for (Object cell : row) {
				result.addArgument( makeNode( cell ) );
			}
		}

		return result;
	}

	private Object compare( String _comparison, Object _value ) throws Exception
	{
		return new ExpressionNodeForOperator( Operator.CONCAT, new ExpressionNodeForConstantValue( _comparison ),
				makeNode( _value ) );
	}

	private ExpressionNode makeNode( Object _value ) throws Exception
	{
		if (_value instanceof ExpressionNode) {
			return (ExpressionNode) _value;
		}
		if (_value instanceof String) {
			String str = (String) _value;
			if (str.startsWith( "#" )) {
				final CellModel cellModel = new CellModel( rootModel, str.substring( 1 ) );
				cellModel.makeInput( new CallFrameImpl( Inputs.class.getMethod( "getOne" ) ) );
				return new ExpressionNodeForCellModel( cellModel );
			}
		}

		final String name = (_value == null) ? "!null!" : "!" + _value.toString() + "!";
		final CellModel cellModel = new CellModel( rootModel, name );
		cellModel.setConstantValue( _value );
		return new ExpressionNodeForCellModel( cellModel );
	}


}
