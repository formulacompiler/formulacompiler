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


public final class ExpressionNodeForSubstitution extends ExpressionNode
{

	public ExpressionNodeForSubstitution()
	{
		super();
	}

	public ExpressionNodeForSubstitution( ExpressionNode... _args )
	{
		super( _args );
	}

	public ExpressionNodeForSubstitution( Collection<ExpressionNode> _args )
	{
		super( _args );
	}


	@Override
	public boolean isConstant()
	{
		return areConstant( arguments() );
	}


	@Override
	public ExpressionNode innerCloneWithoutArguments()
	{
		return new ExpressionNodeForSubstitution();
	}


	@Override
	protected int countValues( LetDictionary<TypedResult> _letDict, Collection<ExpressionNode> _uncountables )
	{
		return countArgumentValues( _letDict, _uncountables );
	}


	@Override
	protected void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg )
	{
		_to.append( "@" );
		describeArgumentListTo( _to, _cfg );
	}

}
