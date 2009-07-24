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

package org.formulacompiler.compiler.internal.model.rewriting;

import static org.formulacompiler.compiler.internal.expressions.ExpressionBuilder.*;

import org.formulacompiler.compiler.FormulaCompiler;
import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.Operator;
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
		assertRewrite(
				"apply (fold/reduce with s__1 = 0.0 each xi__2 as s__1 = (s__1 + xi__2)) to list {@( \"[args]\" )}",
				Function.SUM );
	}

	public void testAVERAGE() throws Exception
	{
		assertRewrite(
				"apply (fold/reduce with s__1 = 0.0 each xi__2 as s__1 = (s__1 + xi__2) with count n__3 into (s__1 / n__3)) to list {@( \"[args]\" )}",
				Function.AVERAGE );
	}

	public void testVARP() throws Exception
	{
		assertRewrite(
				"apply (fold with s__1 = 0.0, ss__2 = 0.0 each xi__3 as s__1 = (s__1 + xi__3), ss__2 = (ss__2 + (xi__3 * xi__3)) with count n__4 into ((ss__2 - ((s__1 * s__1) / n__4)) / n__4)) to list {@( \"[args]\" )}",
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


	public void testRewriteInTypedSubtree() throws Exception
	{
		ExpressionNode args = new ExpressionNodeForConstantValue( "[args]" );
		ExpressionNode e = fun( Function.VALUE, op( Operator.PLUS, cst( 1 ), fun( Function.SUM, args ) ) );

		ExpressionRewriter rw = new ExpressionRewriter( InterpretedNumericType.typeFor( FormulaCompiler.DOUBLE ),
				new NameSanitizer() );
		ExpressionNode re = rw.rewrite( new ComputationModel( null, null ), e );

		assertNull( re.getDataType() );
		assertNull( re.argument( 1 ).getDataType() );

	}


}
