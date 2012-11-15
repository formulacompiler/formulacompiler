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

import org.formulacompiler.compiler.internal.DescriptionBuilder;


public final class ExpressionNodeForLet extends ExpressionNode
{
	private final Type type;
	private final String varName;

	public static enum Type {
		BYVAL, BYNAME, SYMBOLIC
	}

	public ExpressionNodeForLet( Type _type, String _varName )
	{
		super();
		this.varName = _varName;
		this.type = _type;
	}

	public ExpressionNodeForLet( Type _type, String _varName, ExpressionNode _value )
	{
		this( _type, _varName );
		addArgument( _value );
	}

	public ExpressionNodeForLet( Type _type, String _varName, ExpressionNode _value, ExpressionNode _in )
	{
		this( _type, _varName, _value );
		addArgument( _in );
	}

	public ExpressionNodeForLet( String _varName, ExpressionNode _value, ExpressionNode _in )
	{
		this( Type.BYVAL, _varName, _value, _in );
	}


	public Type type()
	{
		return this.type;
	}

	public final String varName()
	{
		return this.varName;
	}

	public final ExpressionNode value()
	{
		return argument( 0 );
	}

	public final ExpressionNode in()
	{
		return argument( 1 );
	}


	@Override
	public ExpressionNode innerCloneWithoutArguments()
	{
		return new ExpressionNodeForLet( this.type, this.varName );
	}


	@Override
	protected int countValues( LetDictionary<TypedResult> _letDict, Collection<ExpressionNode> _uncountables )
	{
		throw new UnsupportedOperationException( "COUNT not supported over _LET" );
	}


	@Override
	protected void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg )
	{
		_to.append( "(let" ).append( type() == Type.BYVAL ? "" : "/" + type().toString().toLowerCase() ).append( " " )
				.append( varName() ).append( " = " );
		value().describeTo( _to, _cfg );
		_to.append( " in " );
		in().describeTo( _to, _cfg );
		_to.append( " )" );
	}

}
