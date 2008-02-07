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
package org.formulacompiler.compiler.internal.expressions;

import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.Operator;
import org.formulacompiler.compiler.internal.DescriptionBuilder;

import junit.framework.TestCase;

public class ExpressionContextProviderTest extends TestCase
{
	private final ExpressionNode parsedInnerInner = new ExpressionNodeForConstantValue( 3.141 );
	private final ExpressionNode parsedInner = new ExpressionNodeForFunction( Function.ROUND, this.parsedInnerInner,
			new ExpressionNodeForConstantValue( 0 ) );
	private final ExpressionNode parsed = new ExpressionNodeForOperator( Operator.PLUS, this.parsedInner,
			new ExpressionNodeForConstantValue( 2 ) );
	private final ExpressionNode derived = new ExpressionNodeForOperator( Operator.PLUS,
			new ExpressionNodeForConstantValue( 3 ), new ExpressionNodeForConstantValue( 2 ) );
	private final ExpressionNode derivedInner = this.derived.arguments().get( 0 );

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		this.parsed.setContextProviderOnThisAndArgumentsRecursively( new CellExpressionAsContextProvider( "A1",
				this.parsed ) );
		this.derived.setDerivedFrom( this.parsed );
		this.derivedInner.setDerivedFrom( this.parsedInner );
	}


	public void testNoFocus() throws Exception
	{
		String ctx = this.derived.getContext( null );
		assertEquals( "\nIn expression (ROUND( 3.141, 0 ) + 2); error location indicated by >>..<<.\nIn cell A1.", ctx );
	}

	public void testOuterFocus() throws Exception
	{
		String ctx = this.derived.getContext( this.derived );
		assertEquals(
				"\nIn expression  >> (ROUND( 3.141, 0 ) + 2) << ; error location indicated by >>..<<.\nIn cell A1.", ctx );
	}

	public void testInnerFocus() throws Exception
	{
		String ctx = this.derived.getContext( this.derivedInner );
		assertEquals(
				"\nIn expression ( >> ROUND( 3.141, 0 ) <<  + 2); error location indicated by >>..<<.\nIn cell A1.", ctx );
	}

	public void testInnerFocusOnBase() throws Exception
	{
		String ctx = this.parsed.getContext( this.parsedInner );
		assertEquals(
				"\nIn expression ( >> ROUND( 3.141, 0 ) <<  + 2); error location indicated by >>..<<.\nIn cell A1.", ctx );
	}

	public void testDeepInnerFocusOnBase() throws Exception
	{
		String ctx = this.parsed.getContext( this.parsedInnerInner );
		assertEquals(
				"\nIn expression (ROUND(  >> 3.141 << , 0 ) + 2); error location indicated by >>..<<.\nIn cell A1.", ctx );
	}


	private static final class CellExpressionAsContextProvider extends ExpressionSourceAsContextProvider
	{
		private final String cellName;

		public CellExpressionAsContextProvider( String _cellName, ExpressionNode _expr )
		{
			super( _expr );
			this.cellName = _cellName;
		}

		@Override
		public void buildContext( DescriptionBuilder _result, ExpressionNode _focusedNode )
		{
			super.buildContext( _result, _focusedNode );
			_result.append( "\nIn cell " ).append( this.cellName ).append( "." );
		}

	}

}
