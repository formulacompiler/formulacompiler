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


public abstract class ExpressionNodeForScalar extends ExpressionNode
{

	protected ExpressionNodeForScalar()
	{
		super();
	}

	protected ExpressionNodeForScalar( ExpressionNode... _args )
	{
		super( _args );
	}

	protected ExpressionNodeForScalar( Collection<ExpressionNode> _args )
	{
		super( _args );
	}

	@Override
	protected int countValues( LetDictionary<TypedResult> _letDict, Collection<ExpressionNode> _uncountables )
	{
		return 1;
	}

}
