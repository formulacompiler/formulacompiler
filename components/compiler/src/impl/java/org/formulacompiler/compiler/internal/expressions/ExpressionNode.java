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
import java.util.Collections;
import java.util.List;

import org.formulacompiler.describable.AbstractDescribable;
import org.formulacompiler.describable.DescriptionBuilder;
import org.formulacompiler.runtime.New;


public abstract class ExpressionNode extends AbstractDescribable
{
	private List<ExpressionNode> arguments = New.list();
	private DataType dataType;
	private ExpressionNode derivedFrom;
	private ExpressionContextProvider contextProvider;


	protected ExpressionNode()
	{
		super();
	}

	protected ExpressionNode( ExpressionNode... _args )
	{
		Collections.addAll( arguments(), _args );
	}

	public ExpressionNode( Collection<ExpressionNode> _args )
	{
		arguments().addAll( _args );
	}


	public final List<ExpressionNode> arguments()
	{
		return this.arguments;
	}

	public final ExpressionNode argument( int _i )
	{
		return arguments().get( _i );
	}

	public final ExpressionNode argumentOrNull( int _i )
	{
		if (_i < 0 || _i >= arguments().size()) return null;
		return arguments().get( _i );
	}

	public final void addArgument( ExpressionNode _arg )
	{
		this.arguments.add( _arg );
	}

	public final void addArguments( ExpressionNode[] _args )
	{
		for (ExpressionNode arg : _args)
			addArgument( arg );
	}

	public final void replaceArguments( List<ExpressionNode> _newArguments )
	{
		this.arguments = _newArguments;
	}

	public final int cardinality()
	{
		int result = arguments().size();
		while ((result > 0) && (arguments().get( result - 1 ) == null)) {
			result--;
		}
		return result;
	}


	public final DataType getDataType()
	{
		return this.dataType;
	}

	public final void setDataType( DataType _dataType )
	{
		this.dataType = _dataType;
	}


	public final ExpressionNode getDerivedFrom()
	{
		return this.derivedFrom;
	}

	public final void setDerivedFrom( ExpressionNode _node )
	{
		this.derivedFrom = _node;
	}

	public final ExpressionNode getOrigin()
	{
		return (this.derivedFrom == null)? this : this.derivedFrom.getOrigin();
	}


	public ExpressionNode cloneWithoutArguments()
	{
		final ExpressionNode result = innerCloneWithoutArguments();
		result.contextProvider = this.contextProvider;
		result.dataType = this.dataType;
		result.derivedFrom = this.derivedFrom;
		return result;
	}

	protected abstract ExpressionNode innerCloneWithoutArguments();


	@Override
	public ExpressionNode clone()
	{
		final ExpressionNode result = cloneWithoutArguments();
		for (ExpressionNode arg : arguments()) {
			final ExpressionNode newArg = (null == arg)? null : arg.clone();
			result.arguments().add( newArg );
		}
		return result;
	}


	@Override
	public final void describeTo( DescriptionBuilder _to ) throws IOException
	{
		describeToWithConfig( _to, null );
	}


	public final void describeTo( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg ) throws IOException
	{
		if (null != _cfg && _cfg.isFocused( this )) {
			_to.append( _cfg.focusStartMarker() );
			describeToWithConfig( _to, _cfg );
			_to.append( _cfg.focusEndMarker() );
		}
		else {
			describeToWithConfig( _to, _cfg );
		}
	}


	protected abstract void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg )
			throws IOException;


	protected final void describeArgumentTo( DescriptionBuilder _d, ExpressionDescriptionConfig _cfg, int _iArgument )
			throws IOException
	{
		final ExpressionNode arg = this.arguments().get( _iArgument );
		if (null != arg) {
			arg.describeTo( _d, _cfg );
		}
	}


	protected final void describeArgumentListTo( DescriptionBuilder _d, ExpressionDescriptionConfig _cfg )
			throws IOException
	{
		if (0 == arguments().size()) {
			_d.append( "()" );
		}
		else {
			_d.append( "( " );
			describeArgumentTo( _d, _cfg, 0 );
			for (int iArg = 1; iArg < arguments().size(); iArg++) {
				_d.append( ", " );
				describeArgumentTo( _d, _cfg, iArg );
			}
			_d.append( " )" );
		}
	}


	protected final void describeArgumentOrArgumentListTo( DescriptionBuilder _d, ExpressionDescriptionConfig _cfg )
			throws IOException
	{
		if (1 == arguments().size()) {
			describeArgumentTo( _d, _cfg, 0 );
		}
		else {
			describeArgumentListTo( _d, _cfg );
		}
	}


	public final ExpressionContextProvider getContextProvider()
	{
		return this.contextProvider;
	}

	public final void setContextProviderOnThisAndArgumentsRecursively( ExpressionContextProvider _provider )
	{
		this.contextProvider = _provider;
		for (ExpressionNode arg : arguments()) {
			if (null != arg) {
				arg.setContextProviderOnThisAndArgumentsRecursively( _provider );
			}
		}
	}

	public final String getContext( ExpressionNode _focusedNode )
	{
		final DescriptionBuilder builder = new DescriptionBuilder();
		try {
			buildContext( builder, _focusedNode );
		}
		catch (IOException e) {
			builder.append( e.getMessage() );
		}
		return builder.toString();
	}

	public final void buildContext( DescriptionBuilder _builder, ExpressionNode _focusedNode ) throws IOException
	{
		ExpressionContextProvider prov = getNearestContextProvider();
		if (null != prov) {
			prov.buildContext( _builder, _focusedNode );
		}
		else {
			_builder.append( "\nIn expression " );
			describeTo( _builder );
			_builder.append( "." );
		}
	}

	private final ExpressionContextProvider getNearestContextProvider()
	{
		final ExpressionContextProvider prov = getContextProvider();
		return (null == prov)? getOrigin().getContextProvider() : prov;
	}


	public final int countValues( LetDictionary _letDict, Collection<ExpressionNode> _uncountables )
	{
		return countValuesCore( _letDict, _uncountables );
	}

	public final int countArgumentValues( LetDictionary _letDict, Collection<ExpressionNode> _uncountables )
	{
		return countValuesIn( _letDict, arguments(), _uncountables );
	}

	protected final int countValuesIn( LetDictionary _letDict, Iterable<ExpressionNode> _in,
			Collection<ExpressionNode> _uncountables )
	{
		int result = 0;
		for (ExpressionNode arg : _in) {
			result += arg.countValues( _letDict, _uncountables );
		}
		return result;
	}

	protected int countValuesCore( LetDictionary _letDict, Collection<ExpressionNode> _uncountables )
	{
		return countValuesCore( _uncountables );
	}

	protected abstract int countValuesCore( Collection<ExpressionNode> _uncountables );


}
