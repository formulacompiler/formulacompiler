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

package org.formulacompiler.compiler.internal.expressions;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.formulacompiler.compiler.internal.AbstractDescribable;
import org.formulacompiler.compiler.internal.DescriptionBuilder;
import org.formulacompiler.runtime.New;
import org.formulacompiler.runtime.spreadsheet.CellAddress;


public abstract class ExpressionNode extends AbstractDescribable implements TypedResult
{
	private List<ExpressionNode> arguments = New.list();
	private DataType dataType;
	private DataType declaredDataType;
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

	public DataType getDeclaredDataType()
	{
		return this.declaredDataType;
	}

	public void setDeclaredDataType( final DataType _dataType )
	{
		this.declaredDataType = _dataType;
	}

	public final ExpressionNode withDeclaredDataType( DataType _dataType )
	{
		setDeclaredDataType( _dataType );
		return this;
	}


	public boolean isConstant()
	{
		return hasConstantValue();
	}

	public boolean hasConstantValue()
	{
		return false;
	}

	public Object getConstantValue()
	{
		throw new IllegalArgumentException( "Node is not constant or single valued" );
	}

	protected final boolean areConstant( Iterable<ExpressionNode> _args )
	{
		for (ExpressionNode arg : _args) {
			if (!arg.isConstant()) return false;
		}
		return true;
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
		return (this.derivedFrom == null) ? this : this.derivedFrom.getOrigin();
	}

	public final CellAddress getOriginCellAddress() {
		return getOrigin().getCellAddress();
	}

	protected CellAddress getCellAddress() {
		return null;
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
			final ExpressionNode newArg = (null == arg) ? null : arg.clone();
			result.arguments().add( newArg );
		}
		return result;
	}


	private ExpressionNode cloneWithOffset( int _colOffset, int _rowOffset )
	{
		final ExpressionNode result = innerCloneWithOffset( _colOffset, _rowOffset );
		result.contextProvider = this.contextProvider;
		result.dataType = this.dataType;
		result.derivedFrom = this.derivedFrom;
		return result;
	}

	protected ExpressionNode innerCloneWithOffset( int _colOffset, int _rowOffset )
	{
		return innerCloneWithoutArguments();
	}

	public ExpressionNode clone( int colOffset, int rowOffset )
	{
		final ExpressionNode result = cloneWithOffset( colOffset, rowOffset );
		for (ExpressionNode arg : arguments()) {
			final ExpressionNode newArg = (null == arg) ? null : arg.clone( colOffset, rowOffset );
			result.arguments().add( newArg );
		}
		return result;
	}


	@Override
	public final void describeTo( DescriptionBuilder _to )
	{
		if (this.contextProvider != null) {
			this.contextProvider.setUpContext( _to );
		}
		try {
			describeToWithConfig( _to, _to.getContext( ExpressionDescriptionConfig.class ) );
		} finally {
			if (this.contextProvider != null) {
				this.contextProvider.cleanUpContext( _to );
			}
		}
	}


	final void describeTo( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg )
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


	protected abstract void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg );


	protected final void describeArgumentTo( DescriptionBuilder _d, ExpressionDescriptionConfig _cfg, int _iArgument )
	{
		final ExpressionNode arg = this.arguments().get( _iArgument );
		if (null != arg) {
			arg.describeTo( _d, _cfg );
		}
	}


	protected final void describeArgumentListTo( DescriptionBuilder _d, ExpressionDescriptionConfig _cfg )
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
		buildContext( builder, _focusedNode );
		return builder.toString();
	}

	public final void buildContext( DescriptionBuilder _builder, ExpressionNode _focusedNode )
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
		return (null == prov) ? getOrigin().getContextProvider() : prov;
	}


	public final int countArgumentValues( LetDictionary<TypedResult> _letDict, Collection<ExpressionNode> _uncountables )
	{
		int result = 0;
		for (ExpressionNode arg : arguments()) {
			result += arg.countValues( _letDict, _uncountables );
		}
		return result;
	}

	protected abstract int countValues( LetDictionary<TypedResult> _letDict, Collection<ExpressionNode> _uncountables );

}
