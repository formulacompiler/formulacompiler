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

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.FormulaCompiler;
import org.formulacompiler.compiler.internal.model.CellModel;
import org.formulacompiler.compiler.internal.model.interpreter.InterpretedNumericType;
import org.formulacompiler.compiler.internal.model.rewriting.ModelRewriter;


public class ReferenceCounterTest extends AbstractOptimizerTest
{

	@SuppressWarnings( "unqualified-field-access" )
	public void testRefCountsOfCells() throws SecurityException, NoSuchMethodException, CompilerException
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


	@SuppressWarnings( "unqualified-field-access" )
	public void testNoRefCountByUnusedCell() throws SecurityException, NoSuchMethodException, CompilerException
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


	@SuppressWarnings( "unqualified-field-access" )
	public void testRefCountsOfConstCells() throws SecurityException, NoSuchMethodException, CompilerException
	{
		makeConstCellInput();
		bandRefSum.makeOutput( getOutput( "getA" ) );

		model.traverse( new ModelRewriter( InterpretedNumericType.typeFor( FormulaCompiler.DOUBLE ) ) );
		model.traverse( new ConstantSubExpressionEliminator( FormulaCompiler.DOUBLE ) );
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
