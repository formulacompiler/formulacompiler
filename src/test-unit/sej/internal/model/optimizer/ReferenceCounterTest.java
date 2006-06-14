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

import sej.NumericType;
import sej.api.CompilerError;
import sej.internal.model.CellModel;


public class ReferenceCounterTest extends AbstractOptimizerTest
{

	@SuppressWarnings("unqualified-field-access")
	public void testRefCountsOfCells() throws SecurityException, NoSuchMethodException, CompilerError
	{
		makeConstCellInput();
		bandRefSum.makeOutput( getOutput( "getA" ) );

		CellModel otherRef = new CellModel( band, "OtherRef" );
		otherRef.setExpression( ref( bandExpr ) );
		otherRef.makeOutput( getOutput( "getB" ) );

		assertRefs( 0, constCell );
		assertRefs( 0, constExpr );
		assertRefs( 0, constSum );
		assertRefs( 0, constRefSum );

		assertRefs( 0, bandExpr );
		assertRefs( 0, bandOther );
		assertRefs( 0, bandRefSum );

		model.traverse( new ReferenceCounter() );

		assertRefs( 1, constCell );
		assertRefs( 1, constExpr );
		assertRefs( 0, constSum );
		assertRefs( 2, constRefSum );

		assertRefs( 2, bandExpr );
		assertRefs( 1, bandOther );
		assertRefs( 1, bandRefSum );
		assertRefs( 1, otherRef );
	}


	@SuppressWarnings("unqualified-field-access")
	public void testNoRefCountByUnusedCell() throws SecurityException, NoSuchMethodException, CompilerError
	{
		makeConstCellInput();
		bandRefSum.makeOutput( getOutput( "getA" ) );

		CellModel otherRef = new CellModel( band, "OtherRef" );
		otherRef.setExpression( ref( bandExpr ) );

		model.traverse( new ReferenceCounter() );

		assertRefs( 1, constCell );
		assertRefs( 1, constExpr );
		assertRefs( 0, constSum );
		assertRefs( 2, constRefSum );

		assertRefs( 1, bandExpr );
		assertRefs( 1, bandOther );
		assertRefs( 1, bandRefSum );
		assertRefs( 0, otherRef );
	}


	@SuppressWarnings("unqualified-field-access")
	public void testRefCountsOfConstCells() throws SecurityException, NoSuchMethodException, CompilerError
	{
		makeConstCellInput();
		bandRefSum.makeOutput( getOutput( "getA" ) );

		model.traverse( new ConstantSubExpressionEliminator( NumericType.DOUBLE ) );
		model.traverse( new ReferenceCounter() );

		assertRefs( 1, constCell );
		assertRefs( 0, constExpr );
		assertRefs( 0, constSum );
		assertRefs( 2, constRefSum );

		assertRefs( 0, bandExpr );
		assertRefs( 0, bandOther );
		assertRefs( 1, bandRefSum );
	}


}
