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

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.Operator;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFunction;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForOperator;
import org.formulacompiler.compiler.internal.model.CellModel;
import org.formulacompiler.compiler.internal.model.ComputationModel;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForCellModel;
import org.formulacompiler.compiler.internal.model.SectionModel;

import junit.framework.TestCase;

public class CircularReferencesCheckerTest extends TestCase
{

	public void testDirectReference() throws CompilerException
	{
		final ComputationModel m = new ComputationModel( In.class, Out.class );
		final SectionModel r = m.getRoot();
		final CellModel c = new CellModel( r, "Cell1" );
		c.setExpression( new ExpressionNodeForFunction( Function.IF,
				new ExpressionNodeForConstantValue( 0 ),
				new ExpressionNodeForCellModel( c ),
				new ExpressionNodeForConstantValue( 2 ) ) );

		try {
			m.traverse( new CircularReferencesChecker() );
			fail( "Assertion expected." );
		}
		catch (CompilerException.CyclicReferenceException e) {
			assertEquals( "Cyclic reference: Cell1 -> ...\n" +
					"Cell containing expression is Cell1.\n" +
					"Referenced by cell Cell1.", e.getMessage() );
		}
	}

	public void testIndirectReference() throws CompilerException
	{
		final ComputationModel m = new ComputationModel( In.class, Out.class );
		final SectionModel r = m.getRoot();
		final CellModel c1 = new CellModel( r, "Cell1" );
		final CellModel c2 = new CellModel( r, "Cell2" );
		c1.setExpression( new ExpressionNodeForFunction( Function.IF,
				new ExpressionNodeForConstantValue( 0 ),
				new ExpressionNodeForCellModel( c2 ),
				new ExpressionNodeForConstantValue( 2 ) ) );
		c2.setExpression( new ExpressionNodeForOperator( Operator.PLUS,
				new ExpressionNodeForCellModel( c1 ),
				new ExpressionNodeForConstantValue( 3 ) ) );

		try {
			m.traverse( new CircularReferencesChecker() );
			fail( "Assertion expected." );
		}
		catch (CompilerException.CyclicReferenceException e) {
			assertEquals( "Cyclic reference: Cell1 -> Cell2 -> ...\n" +
					"Cell containing expression is Cell1.\n" +
					"Referenced by cell Cell1.", e.getMessage() );
		}
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
