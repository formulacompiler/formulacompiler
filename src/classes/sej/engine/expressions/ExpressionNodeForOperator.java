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
package sej.engine.expressions;

import java.io.IOException;

import sej.describable.DescriptionBuilder;


public class ExpressionNodeForOperator extends ExpressionNode
{
	private Operator operator = Operator.NOOP;


	public ExpressionNodeForOperator(Operator _operator, ExpressionNode... _args)
	{
		setOperator( _operator );
		for (ExpressionNode arg : _args) {
			getArguments().add( arg );
		}
	}


	public Operator getOperator()
	{
		return this.operator;
	}


	public void setOperator( Operator _operator )
	{
		this.operator = _operator;
	}


	@Override
	public Object doEvaluate( EvaluationContext _context ) throws EvaluationFailed
	{
		switch (getArguments().size()) {

		case 0:
			return this.operator.evaluate();
		case 1:
			return this.operator.evaluate( evaluateArgument( _context, 0 ) );
		case 2:
			return this.operator.evaluate( evaluateArgument( _context, 0 ), evaluateArgument(
					_context, 1 ) );
		case 3:
			return this.operator.evaluate( evaluateArgument( _context, 0 ), evaluateArgument(
					_context, 1 ), evaluateArgument( _context, 2 ) );

		default:
			Object[] vals = new Object[ getArguments().size() ];
			int i = 0;
			for (ExpressionNode arg : getArguments()) {
				vals[ i++ ] = arg.evaluate( _context );
			}
			return this.operator.evaluate( vals );

		}

	}


	@Override
	public ExpressionNode cloneWithoutArguments()
	{
		return new ExpressionNodeForOperator( this.operator );
	}


	@Override
	public void describeTo( DescriptionBuilder _to ) throws IOException
	{
		switch (getArguments().size()) {

		case 0:
			_to.append( this.operator.getSymbol() );
			break;
		case 1:
			_to.append( "(" );
			if (this.operator.isPrefix()) _to.append( this.operator.getSymbol() );
			describeArgumentTo( _to, 0 );
			if (!this.operator.isPrefix()) _to.append( this.operator.getSymbol() );
			_to.append( ")" );
			break;
		case 2:
			_to.append( "(" );
			describeArgumentTo( _to, 0 );
			_to.append( " " );
			_to.append( this.operator.getSymbol() );
			_to.append( " " );
			describeArgumentTo( _to, 1 );
			_to.append( ")" );
			break;
		}
	}

}
