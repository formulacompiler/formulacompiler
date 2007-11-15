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
package org.formulacompiler.compiler.internal.model.rewriting;

import org.formulacompiler.compiler.FormulaCompiler;
import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFunction;
import org.formulacompiler.compiler.internal.model.ComputationModel;
import org.formulacompiler.compiler.internal.model.interpreter.InterpretedNumericType;

import junit.framework.TestCase;


public class ExpressionRewriterTest extends TestCase
{


	public void testSUM() throws Exception
	{
		assertRewrite( "apply (fold/reduce with s__1 = 0 each xi__2 as s__1 = (s__1 + xi__2)) to list {@( \"[args]\" )}",
				Function.SUM );
	}

	public void testAVERAGE() throws Exception
	{
		assertRewrite(
				"apply (fold/reduce with s__1 = 0 each xi__2 as s__1 = (s__1 + xi__2) with count n__3 into (s__1 / n__3)) to list {@( \"[args]\" )}",
				Function.AVERAGE );
	}

	public void testVARP() throws Exception
	{
		assertRewrite(
				"apply (fold with s__1 = 0, ss__2 = 0 each xi__3 as s__1 = (s__1 + xi__3), ss__2 = (ss__2 + (xi__3 * xi__3)) with count n__4 into ((ss__2 - ((s__1 * s__1) / n__4)) / n__4)) to list {@( \"[args]\" )}",
				Function.VARP );
	}


	private void assertRewrite( String _rewritten, Function _function ) throws Exception
	{
		ExpressionNode args = new ExpressionNodeForConstantValue( "[args]" );
		ExpressionNode e = new ExpressionNodeForFunction( _function, args );
		ExpressionRewriter rw = new ExpressionRewriter( InterpretedNumericType.typeFor( FormulaCompiler.DOUBLE ),
				new NameSanitizer() );
		ExpressionNode re = rw.rewrite( new ComputationModel( null, null ), e );

		assertEquals( _rewritten, re.toString() );
	}


}
