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

package org.formulacompiler.compiler.internal.model.analysis;

import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.internal.expressions.DataType;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFunction;
import org.formulacompiler.compiler.internal.model.CellModel;
import org.formulacompiler.compiler.internal.model.ComputationModel;
import org.formulacompiler.compiler.internal.model.SectionModel;

import junit.framework.TestCase;

public class ModelIsTypedCheckerTest extends TestCase
{

	public void testMissingCellType() throws Exception
	{
		ComputationModel m = new ComputationModel( In.class, Out.class );
		SectionModel r = m.getRoot();
		CellModel c = new CellModel( r, "Test" );
		c.setConstantValue( 11.1 );

		try {
			m.traverse( new ModelIsTypedChecker() );
			fail( "Assertion expected." );
		}
		catch (AssertionError e) {
			// expected
		}
	}

	public void testCellType() throws Exception
	{
		ComputationModel m = new ComputationModel( In.class, Out.class );
		SectionModel r = m.getRoot();
		CellModel c = new CellModel( r, "Test" );
		c.setConstantValue( 11.1 );
		c.setDataType( DataType.NUMERIC );

		m.traverse( new ModelIsTypedChecker() );
	}

	public void testMissingExpressionNodeType() throws Exception
	{
		ComputationModel m = new ComputationModel( In.class, Out.class );
		SectionModel r = m.getRoot();
		CellModel c = new CellModel( r, "Test" );
		c.setDataType( DataType.NUMERIC );
		c.setExpression( new ExpressionNodeForConstantValue( 11.1 ) );

		try {
			m.traverse( new ModelIsTypedChecker() );
			fail( "Assertion expected." );
		}
		catch (AssertionError e) {
			// expected
		}
	}

	public void testExpressionNodeType() throws Exception
	{
		ComputationModel m = new ComputationModel( In.class, Out.class );
		SectionModel r = m.getRoot();
		CellModel c = new CellModel( r, "Test" );
		c.setDataType( DataType.NUMERIC );
		ExpressionNode expr = new ExpressionNodeForConstantValue( 11.1 );
		expr.setDataType( DataType.NUMERIC );
		c.setExpression( expr );

		m.traverse( new ModelIsTypedChecker() );
	}

	public void testMissingSubExpressionNodeType() throws Exception
	{
		ComputationModel m = new ComputationModel( In.class, Out.class );
		SectionModel r = m.getRoot();
		CellModel c = new CellModel( r, "Test" );
		c.setDataType( DataType.NUMERIC );
		ExpressionNode expr = new ExpressionNodeForConstantValue( 11.1 );
		ExpressionNode sum = new ExpressionNodeForFunction( Function.SUM, expr );
		sum.setDataType( DataType.NUMERIC );
		c.setExpression( sum );

		try {
			m.traverse( new ModelIsTypedChecker() );
			fail( "Assertion expected." );
		}
		catch (AssertionError e) {
			// expected
		}
	}

	public void testSubExpressionNodeType() throws Exception
	{
		ComputationModel m = new ComputationModel( In.class, Out.class );
		SectionModel r = m.getRoot();
		CellModel c = new CellModel( r, "Test" );
		c.setDataType( DataType.NUMERIC );
		ExpressionNode expr = new ExpressionNodeForConstantValue( 11.1 );
		expr.setDataType( DataType.NUMERIC );
		ExpressionNode sum = new ExpressionNodeForFunction( Function.SUM, expr );
		sum.setDataType( DataType.NUMERIC );
		c.setExpression( sum );

		m.traverse( new ModelIsTypedChecker() );
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
