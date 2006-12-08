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
package sej.internal.model.rewriting;

import sej.Function;
import sej.Operator;
import sej.SEJ;
import sej.internal.expressions.ArrayDescriptor;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForArrayReference;
import sej.internal.expressions.ExpressionNodeForConstantValue;
import sej.internal.expressions.ExpressionNodeForFunction;
import sej.internal.expressions.ExpressionNodeForOperator;
import sej.internal.model.CellModel;
import sej.internal.model.ComputationModel;
import sej.internal.model.ExpressionNodeForCellModel;
import sej.internal.model.SectionModel;
import sej.internal.model.util.InterpretedNumericType;
import sej.tests.utils.Inputs;
import sej.tests.utils.OutputsWithoutCaching;
import junit.framework.TestCase;

@SuppressWarnings("unqualified-field-access")
public class ModelRewriterTest extends TestCase
{
	private final ComputationModel engineModel = new ComputationModel( Inputs.class, OutputsWithoutCaching.class );
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

		engineModel.traverse( new ModelRewriter( InterpretedNumericType.typeFor( SEJ.DOUBLE ) ) );

		assertEquals( "_FOLD_OR_REDUCE( r: 0.0; xi: (`r + `xi); @( a, b, c ) )", r.getExpression().toString() );
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

		engineModel.traverse( new ModelRewriter( InterpretedNumericType.typeFor( SEJ.DOUBLE ) ) );

		assertBeginsWith(
				"_DFOLD( col: OR( AND( (`col0 = \"Apple\"), (`col1 > 10.0), (`col1 < 16.0) ), (`col0 = \"Pear\") ); r: 0.0; xi: (`r + `xi); 3; #(1,2,5){",
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

		engineModel.traverse( new ModelRewriter( InterpretedNumericType.typeFor( SEJ.DOUBLE ) ) );

		assertBeginsWith(
				"_LET( -crit0: a; _LET( -crit1: b; _LET( -crit2: c; _LET( -crit3: d; "
						+ "_DFOLD( col: OR( AND( (`col0 = `-crit0), (`col1 > `-crit1), (`col1 < `-crit2) ), (`col0 = `-crit3) ); r: 0.0; xi: (`r + `xi); 3; #(1,2,5){",
				r.getExpression().toString() );
	}


	private static void assertBeginsWith( String _expected, String _actual )
	{
		final int expLen = _expected.length();
		final int actLen = _actual.length();
		final int compLen = (expLen > actLen) ? actLen : expLen;
		assertEquals( _expected, _actual.substring( 0, compLen ) );
	}


	private final ExpressionNodeForArrayReference makeRange( Object[][] _rows )
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

	private Object compare( String _comparison, Object _value )
	{
		return new ExpressionNodeForOperator( Operator.CONCAT, new ExpressionNodeForConstantValue( _comparison ),
				makeNode( _value ) );
	}

	private ExpressionNode makeNode( Object _value )
	{
		if (_value instanceof ExpressionNode) {
			return (ExpressionNode) _value;
		}
		if (_value instanceof String) {
			String str = (String) _value;
			if (str.startsWith( "#" )) {
				return new ExpressionNodeForCellModel( new CellModel( rootModel, str.substring( 1 ) ) );
			}
		}
		return new ExpressionNodeForConstantValue( _value );
	}


}
