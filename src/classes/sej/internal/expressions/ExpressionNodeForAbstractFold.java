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
package sej.internal.expressions;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import sej.describable.DescriptionBuilder;

public abstract class ExpressionNodeForAbstractFold extends ExpressionNode
{
	private final String accumulatorName;
	private final String elementName;
	private boolean mayReduce;

	protected ExpressionNodeForAbstractFold(String _accumulatorName, String _elementName, boolean _mayReduce)
	{
		super();
		this.accumulatorName = _accumulatorName;
		this.elementName = _elementName;
		this.mayReduce = _mayReduce;
	}

	public final boolean mayReduce()
	{
		return this.mayReduce;
	}

	protected final void setMayReduce( boolean _value )
	{
		this.mayReduce = _value;
	}

	
	protected final void addArguments( ExpressionNode... _elements )
	{
		for (ExpressionNode element : _elements)
			addArgument( element );
	}

	protected static final ExpressionNode[] elementsToArray( Collection<ExpressionNode> _args )
	{
		return _args.toArray( new ExpressionNode[ _args.size() ] );
	}


	public final String accumulatorName()
	{
		return this.accumulatorName;
	}

	public final ExpressionNode initialAccumulatorValue()
	{
		return argument( 0 );
	}

	public final String elementName()
	{
		return this.elementName;
	}

	public final ExpressionNode accumulatingStep()
	{
		return argument( 1 );
	}
	
	public final Iterable<ExpressionNode> elements()
	{
		return new Iterable<ExpressionNode>()
		{

			public Iterator<ExpressionNode> iterator()
			{
				Iterator<ExpressionNode> result = arguments().iterator();
				skipToElements( result );
				return result;
			}

		};
	}
	
	protected void skipToElements( Iterator<ExpressionNode> _iterator )
	{
		_iterator.next();
		_iterator.next();
	}

	
	protected final void describeElements( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg ) throws IOException
	{
		boolean first = true;
		for (final ExpressionNode element : elements()) {
			if (first) first = false;
			else _to.append( ", " );
			element.describeTo( _to, _cfg );
		}
		_to.append( " )" );
	}

	
	@Override
	protected int countValuesCore( Collection<ExpressionNode> _uncountables )
	{
		return 1;
	}
	
	
}
