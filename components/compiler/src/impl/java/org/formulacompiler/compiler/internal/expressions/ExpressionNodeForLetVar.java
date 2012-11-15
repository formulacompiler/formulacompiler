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


public final class ExpressionNodeForLetVar extends ExpressionNode
{
	private final String varName;

	public ExpressionNodeForLetVar( String _varName )
	{
		super();
		this.varName = _varName;
	}


	public final String varName()
	{
		return this.varName;
	}


	@Override
	public ExpressionNode innerCloneWithoutArguments()
	{
		return new ExpressionNodeForLetVar( varName() );
	}


	@Override
	protected int countValues( LetDictionary _letDict, Collection<ExpressionNode> _uncountables )
	{
		final Object val = _letDict.lookup( varName() );
		if (val instanceof ExpressionNode) {
			return ((ExpressionNode) val).countValues( _letDict, _uncountables );
		}
		else if (val instanceof ArrayDescriptor) {
			return ((ArrayDescriptor) val).numberOfElements();
		}
		else {
			return 1;
		}
	}


	@Override
	protected void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg )
	{
		_to.append( varName() );
	}

}
