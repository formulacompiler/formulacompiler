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

package org.formulacompiler.compiler.internal.model.optimizer;

import org.formulacompiler.compiler.FormulaCompiler;
import org.formulacompiler.compiler.internal.model.CellModel;
import org.formulacompiler.compiler.internal.model.interpreter.InterpretedNumericType;
import org.formulacompiler.compiler.internal.model.rewriting.ModelRewriter;


public class IntermediateResultsInlinerTest extends AbstractOptimizerTest
{

	@SuppressWarnings( "unqualified-field-access" )
	protected final void optimize() throws Exception
	{
		model.traverse( new ModelRewriter( InterpretedNumericType.typeFor( FormulaCompiler.DOUBLE ) ) );
		model.traverse( new ConstantSubExpressionEliminator( FormulaCompiler.DOUBLE ) );
		model.traverse( new IntermediateResultsInliner() );
	}


	@SuppressWarnings( "unqualified-field-access" )
	public void testInliningOfSingleRef() throws Exception
	{
		makeConstCellInput();

		CellModel otherRef = new CellModel( this.band, "OtherRef" );
		otherRef.setExpression( plus( ref( bandRefSum ), outer( this.root, ref( constCell ) ) ) );
		otherRef.makeOutput( getOutput( "getA" ) );

		optimize();

		assertConst( 1.0, constCell );
		assertConst( 2.0, constExpr );
		assertConst( 3.0, constSum );
		assertExpr( "(Inputs.getOne() + 2.0)", constRefSum );

		assertConst( 10.0, bandExpr );
		assertConst( 11.0, bandOther );
		assertNull( bandRefSum.getExpression() );
		assertExpr( "((33.0 + <~ConstRefSum) + <~Inputs.getOne())", otherRef );
	}


	@SuppressWarnings( "unqualified-field-access" )
	public void testNoInliningOfDoubleRef() throws Exception
	{
		makeConstCellInput();
		bandRefSum.makeOutput( getOutput( "getA" ) );

		CellModel otherRef = new CellModel( this.band, "OtherRef" );
		otherRef.setExpression( ref( constRefSum ) );
		otherRef.makeOutput( getOutput( "getB" ) );

		optimize();

		assertConst( 1.0, constCell );
		assertConst( 2.0, constExpr );
		assertConst( 3.0, constSum );
		assertExpr( "(Inputs.getOne() + 2.0)", constRefSum );

		assertConst( 10.0, bandExpr );
		assertConst( 11.0, bandOther );
		assertExpr( "(33.0 + <~ConstRefSum)", bandRefSum );
		assertExpr( "ConstRefSum", otherRef );
	}


	@SuppressWarnings( "unqualified-field-access" )
	public void testRepeatedInlining() throws Exception
	{
		makeConstCellInput();

		CellModel otherRef = new CellModel( this.band, "OtherRef" );
		otherRef.setExpression( ref( bandRefSum ) );
		otherRef.makeOutput( getOutput( "getA" ) );

		optimize();

		assertConst( 1.0, constCell );
		assertConst( 2.0, constExpr );
		assertConst( 3.0, constSum );
		assertExpr( "(Inputs.getOne() + 2.0)", constRefSum );

		assertConst( 10.0, bandExpr );
		assertConst( 11.0, bandOther );
		assertNull( bandRefSum.getExpression() );

		assertExpr( "(33.0 + <~ConstRefSum)", otherRef );
	}


	@SuppressWarnings( "unqualified-field-access" )
	public void testInliningOfAggregationArguments() throws Exception
	{
		makeConstCellInput();

		CellModel sum = new CellModel( this.root, "Sum" );
		sum.setExpression( sum( inner( band, ref( bandRefSum ) ) ) );
		sum.makeOutput( getOutput( "getA" ) );

		optimize();

		assertConst( 1.0, constCell );
		assertConst( 2.0, constExpr );
		assertConst( 3.0, constSum );
		assertExpr( "(Inputs.getOne() + 2.0)", constRefSum );

		assertConst( 10.0, bandExpr );
		assertConst( 11.0, bandOther );
		assertNull( bandRefSum.getExpression() );

		assertExpr(
				"apply (fold/reduce with s__1 = 0.0 each xi__2 as s__1 = (s__1 + xi__2)) to list {Band~>(33.0 + <~ConstRefSum)}",
				sum );
	}


	@SuppressWarnings( "unqualified-field-access" )
	public void testInliningOfAggregationArgumentsRefdByUnusedCell() throws Exception
	{
		makeConstCellInput();

		CellModel sum = new CellModel( this.root, "Sum" );
		sum.setExpression( sum( inner( band, ref( bandRefSum ) ) ) );
		sum.makeOutput( getOutput( "getA" ) );

		CellModel otherRef = new CellModel( this.band, "OtherRef " );
		otherRef.setExpression( ref( bandRefSum ) );

		optimize();

		assertConst( 1.0, constCell );
		assertConst( 2.0, constExpr );
		assertConst( 3.0, constSum );
		assertExpr( "(Inputs.getOne() + 2.0)", constRefSum );

		assertConst( 10.0, bandExpr );
		assertConst( 11.0, bandOther );
		assertNull( bandRefSum.getExpression() );

		assertExpr(
				"apply (fold/reduce with s__1 = 0.0 each xi__2 as s__1 = (s__1 + xi__2)) to list {Band~>(33.0 + <~ConstRefSum)}",
				sum );
	}


	@SuppressWarnings( "unqualified-field-access" )
	public void testNoInliningOfDoubleRefAggregationArguments() throws Exception
	{
		makeConstCellInput();

		CellModel sum = new CellModel( this.root, "Sum" );
		sum.setExpression( sum( inner( band, ref( bandRefSum ) ) ) );
		sum.makeOutput( getOutput( "getA" ) );

		CellModel otherRef = new CellModel( this.band, "OtherRef " );
		otherRef.setExpression( ref( bandRefSum ) );
		otherRef.makeOutput( getOutput( "getB" ) );

		optimize();

		assertConst( 1.0, constCell );
		assertConst( 2.0, constExpr );
		assertConst( 3.0, constSum );
		assertExpr( "(Inputs.getOne() + 2.0)", constRefSum );

		assertConst( 10.0, bandExpr );
		assertConst( 11.0, bandOther );
		assertExpr( "(33.0 + <~ConstRefSum)", bandRefSum );

		assertExpr( "apply (fold/reduce with s__1 = 0.0 each xi__2 as s__1 = (s__1 + xi__2)) to list {Band~>BandRefSum}",
				sum );
	}


	@SuppressWarnings( "unqualified-field-access" )
	public void testNoInliningOfOutputAggregationArguments() throws Exception
	{
		makeConstCellInput();
		bandRefSum.makeOutput( getOutput( "getA" ) );

		CellModel sum = new CellModel( this.root, "Sum" );
		sum.setExpression( sum( inner( band, ref( bandRefSum ) ) ) );
		sum.makeOutput( getOutput( "getB" ) );

		optimize();

		assertConst( 1.0, constCell );
		assertConst( 2.0, constExpr );
		assertConst( 3.0, constSum );
		assertExpr( "(Inputs.getOne() + 2.0)", constRefSum );

		assertConst( 10.0, bandExpr );
		assertConst( 11.0, bandOther );
		assertExpr( "(33.0 + <~ConstRefSum)", bandRefSum );

		assertExpr( "apply (fold/reduce with s__1 = 0.0 each xi__2 as s__1 = (s__1 + xi__2)) to list {Band~>BandRefSum}",
				sum );
	}


}
