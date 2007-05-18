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
package sej.internal.model.rewriting;

import sej.compiler.Function;
import sej.compiler.SEJCompiler;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForConstantValue;
import sej.internal.expressions.ExpressionNodeForFunction;
import sej.internal.model.interpreter.InterpretedNumericType;
import junit.framework.TestCase;


public class ExpressionRewriterTest extends TestCase
{


	public void testSUM() throws Exception
	{
		assertRewrite( "_FOLD_OR_REDUCE( r: 0; xi: (`r + `xi); @( \"[args]\" ) )", Function.SUM );
	}

	public void testAVERAGE() throws Exception
	{
		assertRewrite( "(_FOLD_OR_REDUCE( r: 0; xi: (`r + `xi); @( \"[args]\" ) ) / COUNT( @( \"[args]\" ) ))", Function.AVERAGE );
	}

	public void testVARP() throws Exception
	{
		assertRewrite(
				"_LET( n: COUNT( @( \"[args]\" ) ); (_LET( m: (_FOLD_OR_REDUCE( r: 0; xi: (`r + `xi); @( \"[args]\" ) ) / `n); _FOLD( r: 0; xi: _LET( ei: (`xi - `m); (`r + (`ei * `ei)) ); @( \"[args]\" ) ) ) / `n) )",
				Function.VARP );
	}


	private void assertRewrite( String _rewritten, Function _function ) throws Exception
	{
		ExpressionNode args = new ExpressionNodeForConstantValue( "[args]" );
		ExpressionNode e = new ExpressionNodeForFunction( _function, args );
		ExpressionRewriter rw = new ExpressionRewriter( InterpretedNumericType.typeFor( SEJCompiler.DOUBLE ) );
		ExpressionNode re = rw.rewrite( e );

		assertEquals( _rewritten, re.toString() );
	}


}
