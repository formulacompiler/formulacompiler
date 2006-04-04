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
package sej.engine.compiler.model.optimizer;

import sej.ModelError;
import sej.engine.compiler.model.CellModel;

public class ConstantSubExpressionEliminatorTest extends AbstractOptimizerTest
{

	
	@SuppressWarnings("unqualified-field-access")
	public void testConstantCells() throws ModelError
	{
		model.traverse( new ConstantSubExpressionEliminator() );

		assertConst( 1.0, constCell );
		assertConst( 2.0, constExpr );
		assertConst( 3.0, constSum );
		assertConst( 3.0, constRefSum );

		assertConst( 10.0, bandExpr );
		assertConst( 11.0, bandOther );
		assertConst( 36.0, bandRefSum );
	}


	@SuppressWarnings("unqualified-field-access")
	public void testPartialFoldingInExprs() throws NoSuchMethodException, ModelError
	{
		makeConstCellInput();

		model.traverse( new ConstantSubExpressionEliminator() );

		assertConst( 1.0, constCell );
		assertConst( 2.0, constExpr );
		assertConst( 3.0, constSum );
		assertExpr( "(getOne() + 2.0)", constRefSum );

		assertConst( 10.0, bandExpr );
		assertConst( 11.0, bandOther );
		assertExpr( "(33.0 + ..ConstRefSum)", bandRefSum );
	}


	@SuppressWarnings("unqualified-field-access")
	public void testPartialAggregationInExprs() throws NoSuchMethodException, ModelError
	{
		makeConstCellInput();

		CellModel sumOverInputsAndConsts = new CellModel( root, "SumOverInputsAndConsts" );
		sumOverInputsAndConsts.setExpression( sum( ref( constCell ), ref( constExpr ), ref( constSum ) ) );

		model.traverse( new ConstantSubExpressionEliminator() );

		assertExpr( "SUM{5.0}( getOne() )", sumOverInputsAndConsts );
	}


	@SuppressWarnings("unqualified-field-access")
	public void testBandsAreNotConst() throws NoSuchMethodException, ModelError
	{
		makeConstCellInput();

		CellModel sumOverBand = new CellModel( root, "SumOverBand" );
		sumOverBand.setExpression( sum( inner( band, ref( bandExpr ) ) ) );

		model.traverse( new ConstantSubExpressionEliminator() );

		assertExpr( "SUM( Band.10.0 )", sumOverBand );
	}


}
