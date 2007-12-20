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
