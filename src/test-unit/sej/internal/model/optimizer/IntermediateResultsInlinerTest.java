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
package sej.internal.model.optimizer;

import sej.SEJCompiler;
import sej.internal.model.CellModel;
import sej.internal.model.rewriting.ModelRewriter;
import sej.internal.model.util.InterpretedNumericType;

public class IntermediateResultsInlinerTest extends AbstractOptimizerTest
{
	
	@SuppressWarnings("unqualified-field-access")
	protected final void optimize( ) throws Exception
	{
		model.traverse( new ModelRewriter(InterpretedNumericType.typeFor( SEJCompiler.DOUBLE )) );
		model.traverse( new ConstantSubExpressionEliminator( SEJCompiler.DOUBLE ) );
		model.traverse( new IntermediateResultsInliner() );
	}


	@SuppressWarnings("unqualified-field-access")
	public void testInliningOfSingleRef() throws Exception
	{
		makeConstCellInput();

		CellModel otherRef = new CellModel( this.band, "OtherRef" );
		otherRef.setExpression( plus( ref( bandRefSum ), outer( this.root, ref( constCell ) ) ) );
		otherRef.makeOutput( getOutput( "getA" ) );

		optimize( );

		assertConst( 1.0, constCell );
		assertConst( 2.0, constExpr );
		assertConst( 3.0, constSum );
		assertExpr( "(Inputs.getOne() + 2.0)", constRefSum );

		assertConst( 10.0, bandExpr );
		assertConst( 11.0, bandOther );
		assertNull( bandRefSum.getExpression() );
		assertExpr( "((33.0 + <~ConstRefSum) + <~Inputs.getOne())", otherRef );
	}


	@SuppressWarnings("unqualified-field-access")
	public void testNoInliningOfDoubleRef() throws Exception
	{
		makeConstCellInput();
		bandRefSum.makeOutput( getOutput( "getA" ) );

		CellModel otherRef = new CellModel( this.band, "OtherRef" );
		otherRef.setExpression( ref( constRefSum ) );
		otherRef.makeOutput( getOutput( "getB" ) );

		optimize( );

		assertConst( 1.0, constCell );
		assertConst( 2.0, constExpr );
		assertConst( 3.0, constSum );
		assertExpr( "(Inputs.getOne() + 2.0)", constRefSum );

		assertConst( 10.0, bandExpr );
		assertConst( 11.0, bandOther );
		assertExpr( "(33.0 + <~ConstRefSum)", bandRefSum );
		assertExpr( "ConstRefSum", otherRef );
	}


	@SuppressWarnings("unqualified-field-access")
	public void testRepeatedInlining() throws Exception
	{
		makeConstCellInput();

		CellModel otherRef = new CellModel( this.band, "OtherRef" );
		otherRef.setExpression( ref( bandRefSum ) );
		otherRef.makeOutput( getOutput( "getA" ) );

		optimize( );

		assertConst( 1.0, constCell );
		assertConst( 2.0, constExpr );
		assertConst( 3.0, constSum );
		assertExpr( "(Inputs.getOne() + 2.0)", constRefSum );

		assertConst( 10.0, bandExpr );
		assertConst( 11.0, bandOther );
		assertNull( bandRefSum.getExpression() );

		assertExpr( "(33.0 + <~ConstRefSum)", otherRef );
	}


	@SuppressWarnings("unqualified-field-access")
	public void testInliningOfAggregationArguments() throws Exception
	{
		makeConstCellInput();

		CellModel sum = new CellModel( this.root, "Sum" );
		sum.setExpression( sum( inner( band, ref( bandRefSum ) ) ) );
		sum.makeOutput( getOutput( "getA" ) );

		optimize( );

		assertConst( 1.0, constCell );
		assertConst( 2.0, constExpr );
		assertConst( 3.0, constSum );
		assertExpr( "(Inputs.getOne() + 2.0)", constRefSum );

		assertConst( 10.0, bandExpr );
		assertConst( 11.0, bandOther );
		assertNull( bandRefSum.getExpression() );

		assertExpr( "_FOLD_OR_REDUCE( r: 0.0; xi: (`r + `xi); Band~>(33.0 + <~ConstRefSum) )", sum );
	}


	@SuppressWarnings("unqualified-field-access")
	public void testInliningOfAggregationArgumentsRefdByUnusedCell() throws Exception
	{
		makeConstCellInput();

		CellModel sum = new CellModel( this.root, "Sum" );
		sum.setExpression( sum( inner( band, ref( bandRefSum ) ) ) );
		sum.makeOutput( getOutput( "getA" ) );

		CellModel otherRef = new CellModel( this.band, "OtherRef " );
		otherRef.setExpression( ref( bandRefSum ) );

		optimize( );

		assertConst( 1.0, constCell );
		assertConst( 2.0, constExpr );
		assertConst( 3.0, constSum );
		assertExpr( "(Inputs.getOne() + 2.0)", constRefSum );

		assertConst( 10.0, bandExpr );
		assertConst( 11.0, bandOther );
		assertNull( bandRefSum.getExpression() );

		assertExpr( "_FOLD_OR_REDUCE( r: 0.0; xi: (`r + `xi); Band~>(33.0 + <~ConstRefSum) )", sum );
	}


	@SuppressWarnings("unqualified-field-access")
	public void testNoInliningOfDoubleRefAggregationArguments() throws Exception
	{
		makeConstCellInput();

		CellModel sum = new CellModel( this.root, "Sum" );
		sum.setExpression( sum( inner( band, ref( bandRefSum ) ) ) );
		sum.makeOutput( getOutput( "getA" ) );

		CellModel otherRef = new CellModel( this.band, "OtherRef " );
		otherRef.setExpression( ref( bandRefSum ) );
		otherRef.makeOutput( getOutput( "getB" ) );

		optimize( );

		assertConst( 1.0, constCell );
		assertConst( 2.0, constExpr );
		assertConst( 3.0, constSum );
		assertExpr( "(Inputs.getOne() + 2.0)", constRefSum );

		assertConst( 10.0, bandExpr );
		assertConst( 11.0, bandOther );
		assertExpr( "(33.0 + <~ConstRefSum)", bandRefSum );

		assertExpr( "_FOLD_OR_REDUCE( r: 0.0; xi: (`r + `xi); Band~>BandRefSum )", sum );
	}


	@SuppressWarnings("unqualified-field-access")
	public void testNoInliningOfOutputAggregationArguments() throws Exception
	{
		makeConstCellInput();
		bandRefSum.makeOutput( getOutput( "getA" ) );

		CellModel sum = new CellModel( this.root, "Sum" );
		sum.setExpression( sum( inner( band, ref( bandRefSum ) ) ) );
		sum.makeOutput( getOutput( "getB" ) );

		optimize( );

		assertConst( 1.0, constCell );
		assertConst( 2.0, constExpr );
		assertConst( 3.0, constSum );
		assertExpr( "(Inputs.getOne() + 2.0)", constRefSum );

		assertConst( 10.0, bandExpr );
		assertConst( 11.0, bandOther );
		assertExpr( "(33.0 + <~ConstRefSum)", bandRefSum );

		assertExpr( "_FOLD_OR_REDUCE( r: 0.0; xi: (`r + `xi); Band~>BandRefSum )", sum );
	}


}
