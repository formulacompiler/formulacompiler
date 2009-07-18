/*
 * Copyright (c) 2006, 2008 by Abacus Research AG, Switzerland.
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

/**
 * @author Vladimir Korenev
 */
public class ExpressionNodeForLogging extends ExpressionNode
{
	private final Object source;
	private final String definedName;

	private ExpressionNodeForLogging( final Object _source, final String _definedName )
	{
		super();
		this.source = _source;
		this.definedName = _definedName;
	}

	public ExpressionNodeForLogging( final ExpressionNode _exp, final Object _source, final String _definedName )
	{
		super( _exp );
		this.source = _source;
		this.definedName = _definedName;
	}

	public Object getSource()
	{
		return this.source;
	}

	public String getDefinedName()
	{
		return this.definedName;
	}

	@Override
	protected ExpressionNode innerCloneWithoutArguments()
	{
		return new ExpressionNodeForLogging( this.source, this.definedName );
	}

	@Override
	protected void describeToWithConfig( final DescriptionBuilder _to, final ExpressionDescriptionConfig _cfg )
	{
		_to.append( "Logging" );
		describeArgumentListTo( _to, _cfg );
	}

	@Override
	protected int countValuesCore( final Collection<ExpressionNode> _uncountables )
	{
		return 1;
	}
}
