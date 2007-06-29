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
package org.formulacompiler.compiler.internal.model.optimizer;

import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.FormulaCompiler;
import org.formulacompiler.compiler.internal.expressions.ArrayDescriptor;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForArrayReference;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFunction;
import org.formulacompiler.compiler.internal.model.CellModel;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForCellModel;
import org.formulacompiler.compiler.internal.model.interpreter.InterpretedNumericType;
import org.formulacompiler.compiler.internal.model.rewriting.ModelRewriter;


public class ConstantSubExpressionEliminatorTest extends AbstractOptimizerTest
{

	@SuppressWarnings("unqualified-field-access")
	protected final void optimize( NumericType _type ) throws Exception
	{
		model.traverse( new ModelRewriter( InterpretedNumericType.typeFor( _type ) ) );
		model.traverse( new ConstantSubExpressionEliminator( _type ) );
	}


	@SuppressWarnings("unqualified-field-access")
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


	@SuppressWarnings("unqualified-field-access")
	public void testConstantCellsBigDecimal() throws Exception
	{
		optimize( FormulaCompiler.BIGDECIMAL8 );

		// assertBigConst( "1", constCell ); -- still double because model was constructed that way
		// assertBigConst( "3", constExpr );
		assertBigConst( "3", constSum );
		assertBigConst( "3", constRefSum );

		// assertBigConst( "10", bandExpr );
		// assertBigConst( "11", bandOther );
		assertBigConst( "36", bandRefSum );
	}


	@SuppressWarnings("unqualified-field-access")
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


	@SuppressWarnings("unqualified-field-access")
	public void testPartialAggregationInExprs() throws Exception
	{
		makeConstCellInput();

		CellModel sumOverInputsAndConsts = new CellModel( root, "SumOverInputsAndConsts" );
		sumOverInputsAndConsts.setExpression( sum( ref( constCell ), ref( constExpr ), ref( constSum ) ) );

		optimize( FormulaCompiler.DOUBLE );

		assertExpr( "_FOLD( r: 5.0; xi: (`r + `xi); Inputs.getOne() )", sumOverInputsAndConsts );
	}


	@SuppressWarnings("unqualified-field-access")
	public void testBandsAreNotConst() throws Exception
	{
		makeConstCellInput();

		CellModel sumOverBand = new CellModel( root, "SumOverBand" );
		sumOverBand.setExpression( sum( inner( band, ref( bandExpr ) ) ) );

		optimize( FormulaCompiler.DOUBLE );

		assertExpr( "_FOLD_OR_REDUCE( r: 0; xi: (`r + `xi); Band~>10.0 )", sumOverBand );
	}


	@SuppressWarnings("unqualified-field-access")
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

	@SuppressWarnings("unqualified-field-access")
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

	@SuppressWarnings("unqualified-field-access")
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
