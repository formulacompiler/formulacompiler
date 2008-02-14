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

package org.formulacompiler.compiler.internal.model.optimizer;

import static org.formulacompiler.compiler.internal.expressions.ExpressionBuilder.*;

import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.FormulaCompiler;
import org.formulacompiler.compiler.internal.expressions.ArrayDescriptor;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForArrayReference;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFunction;
import org.formulacompiler.compiler.internal.model.CellModel;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForCellModel;
import org.formulacompiler.compiler.internal.model.interpreter.InterpretedNumericType;
import org.formulacompiler.compiler.internal.model.rewriting.ModelRewriter;


@SuppressWarnings( "unqualified-field-access" )
public class ConstantSubExpressionEliminatorTest extends AbstractOptimizerTest
{

	protected final void optimize( NumericType _type ) throws Exception
	{
		model.traverse( new ModelRewriter( InterpretedNumericType.typeFor( _type ) ) );
		model.traverse( new ConstantSubExpressionEliminator( _type ) );
	}


	public void testConstantCells() throws Exception
	{
		optimize( FormulaCompiler.DOUBLE );

		assertConst( 1.0, constCell );
		assertConst( 2.0, constExpr );
		assertConst( 3.0, constSum );
		assertConst( 3.0, constRefSum );

		assertConst( 10.0, bandExpr );
		assertConst( 11.0, bandOther );
		assertConst( 36.0, bandRefSum );
	}


	public void testConstantCellsBigDecimal() throws Exception
	{
		optimize( FormulaCompiler.BIGDECIMAL_SCALE8 );

		// assertBigConst( "1", constCell ); -- still double because model was constructed that way
		// assertBigConst( "3", constExpr );
		assertBigConst( "3", constSum );
		assertBigConst( "3", constRefSum );

		// assertBigConst( "10", bandExpr );
		// assertBigConst( "11", bandOther );
		assertBigConst( "36", bandRefSum );
	}


	public void testPartialFoldingInExprs() throws Exception
	{
		makeConstCellInput();

		optimize( FormulaCompiler.DOUBLE );

		assertConst( 1.0, constCell );
		assertConst( 2.0, constExpr );
		assertConst( 3.0, constSum );
		assertExpr( "(Inputs.getOne() + 2.0)", constRefSum );

		assertConst( 10.0, bandExpr );
		assertConst( 11.0, bandOther );
		assertExpr( "(33.0 + <~ConstRefSum)", bandRefSum );
	}


	public void testPartialAggregationInExprs() throws Exception
	{
		makeConstCellInput();

		CellModel sumOverInputsAndConsts = new CellModel( root, "SumOverInputsAndConsts" );
		sumOverInputsAndConsts.setExpression( sum( ref( constCell ), ref( constExpr ), ref( constSum ) ) );

		optimize( FormulaCompiler.DOUBLE );

		assertExpr( "apply (fold with s__1 = 5.0 each xi__2 as s__1 = (s__1 + xi__2)) to list {Inputs.getOne()}",
				sumOverInputsAndConsts );
	}


	public void testPartialMultiAccuAggregationInExprs() throws Exception
	{
		makeConstCellInput();

		CellModel sumOverInputsAndConsts = new CellModel( root, "SumOverInputsAndConsts" );
		sumOverInputsAndConsts.setExpression( fun( Function.VARP, ref( constCell ), ref( constExpr ), ref( constSum ) ) );

		optimize( FormulaCompiler.DOUBLE );

		assertExpr(
				"apply (fold with s__1 = 5.0, ss__2 = 13.0 each xi__3 as s__1 = (s__1 + xi__3), ss__2 = (ss__2 + (xi__3 * xi__3)) with count n__4 offset by 2 into ((ss__2 - ((s__1 * s__1) / n__4)) / n__4)) to list {Inputs.getOne()}",
				sumOverInputsAndConsts );
	}


	public void testPartialMultiAccuMultiVectorAggregationInExprs() throws Exception
	{
		makeConstCellInput();

		CellModel sumOverInputsAndConsts = new CellModel( root, "SumOverInputsAndConsts" );
		final ExpressionNode vector = vector( ref( constCell ), ref( constExpr ), ref( constSum ) );
		sumOverInputsAndConsts.setExpression( fun( Function.COVAR, vector, vector ) );

		optimize( FormulaCompiler.DOUBLE );

		assertExpr(
				"apply (fold with sx__1 = 5.0, sy__2 = 5.0, sxy__3 = 13.0 each xi__4, yi__5 as sx__1 = (sx__1 + xi__4), sy__2 = (sy__2 + yi__5), sxy__3 = (sxy__3 + (xi__4 * yi__5)) with count n__6 offset by 2 into ((sxy__3 - ((sx__1 * sy__2) / n__6)) / n__6) when empty ERROR( \"#DIV/0! because list doesn't contain numbers in COVAR\" )) to  vectors {#(1,1,1){Inputs.getOne()}, #(1,1,1){Inputs.getOne()}}",
				sumOverInputsAndConsts );
	}


	public void testNoPartialIndexedAggregationInExprs() throws Exception
	{
		makeConstCellInput();

		CellModel sumOverInputsAndConsts = new CellModel( root, "SumOverInputsAndConsts" );
		sumOverInputsAndConsts.setExpression( fun( Function.NPV, cst( 0.3 ), vector( ref( constSum ), ref( constCell ),
				ref( constExpr ) ) ) );

		optimize( FormulaCompiler.DOUBLE );

		assertExpr(
				"(let rate1__1 = 1.3 in apply (iterate with r__2 = 0.0 index i__3 each vi__4 as r__2 = (r__2 + (vi__4 / (rate1__1 ^ i__3)))) to  vectors {#(1,1,3){3.0, Inputs.getOne(), 2.0}} )",
				sumOverInputsAndConsts );
	}


	@SuppressWarnings( "unqualified-field-access" )
	public void testBandsAreNotConst() throws Exception
	{
		makeConstCellInput();

		CellModel sumOverBand = new CellModel( root, "SumOverBand" );
		sumOverBand.setExpression( sum( inner( band, ref( bandExpr ) ) ) );

		optimize( FormulaCompiler.DOUBLE );

		assertExpr( "apply (fold/reduce with s__1 = 0.0 each xi__2 as s__1 = (s__1 + xi__2)) to list {Band~>BandExpr}",
				sumOverBand );
	}


	@SuppressWarnings( "unqualified-field-access" )
	public void testShortCircuitedINDEX_AllConst() throws Exception
	{
		CellModel i = new CellModel( root, "i" );
		i.setExpression( new ExpressionNodeForConstantValue( 1.0 ) );
		CellModel a = new CellModel( root, "a" );
		a.setExpression( new ExpressionNodeForConstantValue( 2.0 ) );
		CellModel b = new CellModel( root, "a" );
		b.setExpression( new ExpressionNodeForConstantValue( 3.0 ) );
		CellModel c = new CellModel( root, "c" );
		c.setExpression( new ExpressionNodeForConstantValue( 4.0 ) );

		CellModel index = new CellModel( root, "index" );
		index.setExpression( new ExpressionNodeForFunction( Function.INDEX, new ExpressionNodeForArrayReference(
				new ArrayDescriptor( 1, 1, 3 ), new ExpressionNodeForCellModel( a ), new ExpressionNodeForCellModel( b ),
				new ExpressionNodeForCellModel( c ) ), new ExpressionNodeForCellModel( i ), null ) );

		optimize( FormulaCompiler.DOUBLE );

		assertConst( 2.0, index );
	}

	public void testShortCircuitedINDEX_OnlyIndexConst() throws Exception
	{
		CellModel i = new CellModel( root, "i" );
		i.setExpression( new ExpressionNodeForConstantValue( 1.0 ) );
		CellModel a = new CellModel( root, "a" );
		a.makeInput( getInput( "getOne" ) );
		CellModel b = new CellModel( root, "a" );
		b.makeInput( getInput( "getTwo" ) );
		CellModel c = new CellModel( root, "c" );
		c.makeInput( getInput( "getThree" ) );

		CellModel index = new CellModel( root, "index" );
		index.setExpression( new ExpressionNodeForFunction( Function.INDEX, new ExpressionNodeForArrayReference(
				new ArrayDescriptor( 1, 1, 3 ), new ExpressionNodeForCellModel( a ), new ExpressionNodeForCellModel( b ),
				new ExpressionNodeForCellModel( c ) ), new ExpressionNodeForCellModel( i ), null ) );

		optimize( FormulaCompiler.DOUBLE );

		assertExpr( "Inputs.getOne()", index );
	}

	public void testINDEX_OnlyIndexInput() throws Exception
	{
		CellModel i = new CellModel( root, "i" );
		i.makeInput( getInput( "getOne" ) );
		CellModel a = new CellModel( root, "a" );
		a.setExpression( new ExpressionNodeForConstantValue( 2.0 ) );
		CellModel b = new CellModel( root, "a" );
		b.setExpression( new ExpressionNodeForConstantValue( 3.0 ) );
		CellModel c = new CellModel( root, "c" );
		c.setExpression( new ExpressionNodeForConstantValue( 4.0 ) );

		CellModel index = new CellModel( root, "index" );
		index.setExpression( new ExpressionNodeForFunction( Function.INDEX, new ExpressionNodeForArrayReference(
				new ArrayDescriptor( 1, 1, 3 ), new ExpressionNodeForCellModel( a ), new ExpressionNodeForCellModel( b ),
				new ExpressionNodeForCellModel( c ) ), new ExpressionNodeForCellModel( i ), null ) );

		optimize( FormulaCompiler.DOUBLE );

		assertExpr( "INDEX( #(1,1,3){2.0, 3.0, 4.0}, Inputs.getOne() )", index );
	}

}
