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

import java.math.BigDecimal;

import sej.NumericType;
import sej.engine.compiler.model.util.InterpretedNumericType;
import sej.expressions.Aggregator;
import sej.expressions.ExpressionNodeForAggregator;
import sej.expressions.ExpressionNodeForOperator;
import sej.expressions.Operator;

public class BigDecimalInterpreterTest extends AbstractExpressionInterpreterTest
{

	@Override
	protected Object valueFromString( String _string )
	{
		return new BigDecimal( _string );
	}

	@Override
	protected String valueToString( Object _value )
	{
		if (_value instanceof BigDecimal) {
			BigDecimal big = (BigDecimal) _value;
			return big.toPlainString();
		}
		else {
			return _value.toString();
		}
	}

	@Override
	protected InterpretedNumericType getType()
	{
		return InterpretedNumericType.typeFor( NumericType.BIGDECIMAL8 );
	}

	@Override
	public void testAverage()
	{
		super.testAverage();

		// The following test fails for doubles due to rounding errors.
		assertEval( "1.3", new ExpressionNodeForAggregator( Aggregator.AVERAGE, cst( "1.2" ), cst( null ), cst( "1.4" ) ) );
	}

	public void testMathContext()
	{
		assertEval( "0.33333333", new ExpressionNodeForOperator( Operator.DIV, cst( "1" ), cst( "3" ) ) );
	}

}
