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
import java.util.Iterator;

import org.formulacompiler.compiler.internal.DescriptionBuilder;

public abstract class ExpressionNodeForFoldApply extends ExpressionNodeForScalar
{

	protected ExpressionNodeForFoldApply()
	{
		super();
	}

	public ExpressionNodeForFoldApply( ExpressionNode _definition, Collection<ExpressionNode> _args )
	{
		this();
		addArgument( _definition );
		arguments().addAll( _args );
	}

	public ExpressionNodeForFoldApply( ExpressionNode _definition, ExpressionNode... _args )
	{
		this();
		addArgument( _definition );
		addArguments( _args );
	}


	public final ExpressionNodeForFoldDefinition fold()
	{
		return (ExpressionNodeForFoldDefinition) argument( 0 );
	}

	public final Iterable<ExpressionNode> elements()
	{
		return new Iterable<ExpressionNode>()
		{
			public Iterator<ExpressionNode> iterator()
			{
				Iterator<ExpressionNode> result = arguments().iterator();
				result.next();
				return result;
			}
		};
	}

	protected final void describeElements( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg )
	{
		boolean first = true;
		for (final ExpressionNode element : elements()) {
			if (first) first = false;
			else _to.append( ", " );
			element.describeTo( _to, _cfg );
		}
	}


	@Override
	protected void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg )
	{
		_to.append( "apply (" );
		describeArgumentTo( _to, _cfg, 0 );
		_to.append( ") to " );
	}

}
