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

import org.formulacompiler.describable.DescriptionBuilder;
import org.formulacompiler.runtime.New;

public final class ExpressionNodeForFoldDefinition extends ExpressionNode
{
	private final String[] accuNames;
	private final String indexName;
	private final String[] eltNames;
	private final String countName;
	private final boolean mayRearrange;
	private boolean mayReduce;
	private int partiallyFoldedElementCount = 0;

	private ExpressionNodeForFoldDefinition( String[] _accuNames, String _indexName, String[] _eltNames,
			String _countName, boolean _mayRearrange, boolean _mayReduce )
	{
		super();
		this.accuNames = _accuNames.clone();
		this.indexName = _indexName;
		this.eltNames = _eltNames.clone();
		this.countName = _countName;
		this.mayRearrange = _mayRearrange;
		this.mayReduce = _mayReduce;
	}

	public ExpressionNodeForFoldDefinition( String[] _accuNames, ExpressionNode[] _initsPerAccu, String _indexName,
			String[] _eltNames, ExpressionNode[] _stepsPerAccu, String _countName, ExpressionNode _mergeAccus,
			ExpressionNode _whenEmpty, boolean _mayRearrange, boolean _mayReduce )
	{
		this( _accuNames, _indexName, _eltNames, _countName, _mayRearrange, _mayReduce );
		addArguments( _initsPerAccu );
		addArguments( _stepsPerAccu );
		addArgument( _mergeAccus );
		addArgument( _whenEmpty );
	}

	public ExpressionNodeForFoldDefinition( String _accuName, ExpressionNode _init, String _indexName, String _eltName,
			ExpressionNode _step, boolean _mayRearrange, boolean _mayReduce )
	{
		this( New.array( _accuName ), New.array( _init ), _indexName, New.array( _eltName ), New.array( _step ), null,
				null, null, _mayRearrange, _mayReduce );
	}

	public ExpressionNodeForFoldDefinition( String _accuName, ExpressionNode _init, String _indexName, String _eltName,
			ExpressionNode _step, ExpressionNode _whenEmpty, boolean _mayRearrange, boolean _mayReduce )
	{
		this( New.array( _accuName ), New.array( _init ), _indexName, New.array( _eltName ), New.array( _step ), null,
				null, _whenEmpty, _mayRearrange, _mayReduce );
	}

	public int accuCount()
	{
		return this.accuNames.length;
	}

	public String accuName( int _index )
	{
		return this.accuNames[ _index ];
	}

	public String[] accuNames()
	{
		return this.accuNames.clone();
	}

	public ExpressionNode accuInit( int _index )
	{
		return argument( _index );
	}

	public boolean isIndexed()
	{
		return null != this.indexName;
	}

	public String indexName()
	{
		return this.indexName;
	}

	public int eltCount()
	{
		return this.eltNames.length;
	}

	public String eltName( int _index )
	{
		return this.eltNames[ _index ];
	}

	public String[] eltNames()
	{
		return this.eltNames.clone();
	}

	public ExpressionNode accuStep( int _index )
	{
		return argument( _index + accuCount() );
	}

	public boolean isCounted()
	{
		return null != this.countName;
	}

	public String countName()
	{
		return this.countName;
	}

	public boolean isMergedExplicitly()
	{
		return null != merge();
	}

	public ExpressionNode merge()
	{
		return argumentOrNull( accuCount() * 2 );
	}

	public boolean isSpecialWhenEmpty()
	{
		return null != whenEmpty();
	}

	public ExpressionNode whenEmpty()
	{
		return argumentOrNull( accuCount() * 2 + 1 );
	}

	public boolean mayRearrange()
	{
		return this.mayRearrange;
	}

	public boolean mayReduce()
	{
		return this.mayReduce;
	}

	public boolean mayReduceAndRearrange()
	{
		return this.mayReduce && this.mayRearrange;
	}

	public int getPartiallyFoldedElementCount()
	{
		return this.partiallyFoldedElementCount;
	}

	public void setPartiallyFoldedElementCount( int _value )
	{
		this.partiallyFoldedElementCount = _value;
	}


	@Override
	protected int countValuesCore( Collection<ExpressionNode> _uncountables )
	{
		return 1;
	}

	@Override
	protected void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg ) throws IOException
	{
		if (mayRearrange()) {
			_to.append( "fold" );
			if (mayReduce()) _to.append( "/reduce" );
		}
		else {
			_to.append( "iterate" );
		}
		_to.append( " with " );
		for (int i = 0; i < accuCount(); i++) {
			if (i > 0) _to.append( ", " );
			_to.append( accuName( i ) ).append( " = " );
			accuInit( i ).describeToWithConfig( _to, _cfg );
		}
		if (isIndexed()) {
			_to.append( " index " ).append( indexName() );
		}
		_to.append( " each " );
		for (int i = 0; i < eltCount(); i++) {
			if (i > 0) _to.append( ", " );
			_to.append( eltName( i ) );
		}
		_to.append( " as " );
		for (int i = 0; i < accuCount(); i++) {
			if (i > 0) _to.append( ", " );
			_to.append( accuName( i ) ).append( " = " );
			accuStep( i ).describeToWithConfig( _to, _cfg );
		}
		if (isCounted()) {
			_to.append( " with count " ).append( countName() );
			if (getPartiallyFoldedElementCount() > 0)
				_to.append( " offset by " ).append( getPartiallyFoldedElementCount() );
		}
		if (isMergedExplicitly()) {
			_to.append( " into " );
			merge().describeToWithConfig( _to, _cfg );
		}
		if (isSpecialWhenEmpty()) {
			_to.append( " when empty " );
			whenEmpty().describeToWithConfig( _to, _cfg );
		}
	}

	@Override
	protected ExpressionNode innerCloneWithoutArguments()
	{
		final ExpressionNodeForFoldDefinition result = new ExpressionNodeForFoldDefinition( this.accuNames,
				this.indexName, this.eltNames, this.countName, this.mayRearrange, this.mayReduce );
		result.setPartiallyFoldedElementCount( this.getPartiallyFoldedElementCount() );
		return result;
	}

	public final ExpressionNodeForFoldDefinition cloneWithoutArgumentsAndForbidReduce()
	{
		final ExpressionNodeForFoldDefinition result = (ExpressionNodeForFoldDefinition) cloneWithoutArguments();
		result.mayReduce = false;
		return result;
	}

}
