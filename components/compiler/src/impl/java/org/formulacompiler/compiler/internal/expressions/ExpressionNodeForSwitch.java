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

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.formulacompiler.describable.DescriptionBuilder;

public final class ExpressionNodeForSwitch extends ExpressionNode
{

	public ExpressionNodeForSwitch( ExpressionNode _value, ExpressionNode _default,
			ExpressionNodeForSwitchCase... _cases )
	{
		super( _value, _default );
		for (ExpressionNode caze : _cases)
			addArgument( caze );
	}

	protected ExpressionNodeForSwitch()
	{
		super();
	}


	public ExpressionNode selector()
	{
		return argument( 0 );
	}

	public ExpressionNode defaultValue()
	{
		return argument( 1 );
	}
	
	public Iterable<ExpressionNodeForSwitchCase> cases()
	{
		return new Iterable<ExpressionNodeForSwitchCase>() {

			public Iterator<ExpressionNodeForSwitchCase> iterator()
			{
				final Iterator<ExpressionNode> args = arguments().iterator();
				args.next();
				args.next();
				
				return new Iterator<ExpressionNodeForSwitchCase>() {

					public boolean hasNext()
					{
						return args.hasNext();
					}

					public ExpressionNodeForSwitchCase next()
					{
						return (ExpressionNodeForSwitchCase) args.next();
					}

					public void remove()
					{
						throw new IllegalArgumentException();
					}
					
				};
			}
			
		};
	}


	@Override
	protected int countValuesCore( Collection<ExpressionNode> _uncountables )
	{
		return 1;
	}

	@Override
	protected void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg ) throws IOException
	{
		_to.append( "SWITCH( " );
		selector().describeToWithConfig( _to, _cfg );
		for (ExpressionNode caze : cases()) {
			_to.append( ", " );
			caze.describeToWithConfig( _to, _cfg );
		}
		_to.append( ", DEFAULT: " );
		defaultValue().describeToWithConfig( _to, _cfg );
		_to.append( " )" );
	}

	@Override
	protected ExpressionNode innerCloneWithoutArguments()
	{
		return new ExpressionNodeForSwitch();
	}

}
