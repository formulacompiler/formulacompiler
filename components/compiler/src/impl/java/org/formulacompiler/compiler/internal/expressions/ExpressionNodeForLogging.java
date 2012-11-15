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

import org.formulacompiler.compiler.internal.DescriptionBuilder;

/**
 * @author Vladimir Korenev
 */
public class ExpressionNodeForLogging extends ExpressionNodeForScalar
{
	private final Object source;
	private final String definedName;
	private final boolean input;
	private final boolean output;

	private ExpressionNodeForLogging( Object _source, String _definedName, boolean _input, boolean _output )
	{
		super();
		this.source = _source;
		this.definedName = _definedName;
		this.input = _input;
		this.output = _output;
	}

	public ExpressionNodeForLogging( final ExpressionNode _exp, final Object _source, final String _definedName, boolean _input, boolean _output )
	{
		super( _exp );
		this.source = _source;
		this.definedName = _definedName;
		this.input = _input;
		this.output = _output;
	}

	public Object getSource()
	{
		return this.source;
	}

	public String getDefinedName()
	{
		return this.definedName;
	}

	public boolean isInput()
	{
		return this.input;
	}

	public boolean isOutput()
	{
		return this.output;
	}

	@Override
	protected ExpressionNode innerCloneWithoutArguments()
	{
		return new ExpressionNodeForLogging( this.source, this.definedName, this.input, this.output );
	}

	@Override
	protected void describeToWithConfig( final DescriptionBuilder _to, final ExpressionDescriptionConfig _cfg )
	{
		_to.append( "Logging" );
		describeArgumentListTo( _to, _cfg );
	}
}
