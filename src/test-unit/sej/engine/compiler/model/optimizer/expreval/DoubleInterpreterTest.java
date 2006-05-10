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
package sej.engine.compiler.model.optimizer.expreval;

import sej.NumericType;
import sej.engine.compiler.model.util.InterpretedNumericType;
import sej.expressions.ExpressionNodeForOperator;
import sej.expressions.Operator;

public class DoubleInterpreterTest extends AbstractExpressionInterpreterTest
{

	@Override
	protected Object valueFromString( String _string )
	{
		return Double.valueOf( _string );
	}
	
	@Override
	protected String valueToString( Object _value )
	{
		return _value.toString();
	}
	
	@Override
	protected InterpretedNumericType getType()
	{
		return InterpretedNumericType.typeFor( NumericType.DOUBLE );
	}

	
	public void testDoubleOnlyOperators()
	{
		assertEval( "5", new ExpressionNodeForOperator( Operator.EXP, cst("25"), cst("0.5") ) );
	}

}
