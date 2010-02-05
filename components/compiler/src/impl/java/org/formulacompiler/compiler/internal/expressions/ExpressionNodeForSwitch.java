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

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

import org.formulacompiler.compiler.internal.DescriptionBuilder;

public final class ExpressionNodeForSwitch extends ExpressionNode
{

	public ExpressionNodeForSwitch( ExpressionNode _value, ExpressionNode _default,
			ExpressionNodeForSwitchCase... _cases )
	{
		super( _value, _default );
		for (ExpressionNode caze : _cases)
			addArgument( caze );
	}

	protected ExpressionNodeForSwitch()
	{
		super();
	}


	public ExpressionNode selector()
	{
		return argument( 0 );
	}

	public ExpressionNode defaultValue()
	{
		return argument( 1 );
	}

	public Collection<ExpressionNodeForSwitchCase> cases()
	{
		return new AbstractCollection<ExpressionNodeForSwitchCase>()
		{

			@Override
			public Iterator<ExpressionNodeForSwitchCase> iterator()
			{
				final Iterator<ExpressionNode> args = arguments().iterator();
				for (int i = 0; i < offsetOfCasesInArguments(); i++)
					args.next();

				return new Iterator<ExpressionNodeForSwitchCase>()
				{

					public boolean hasNext()
					{
						return args.hasNext();
					}

					public ExpressionNodeForSwitchCase next()
					{
						return (ExpressionNodeForSwitchCase) args.next();
					}

					public void remove()
					{
						throw new IllegalArgumentException();
					}

				};
			}

			@Override
			public int size()
			{
				return arguments().size() - offsetOfCasesInArguments();
			}
		};
	}

	public int offsetOfValueInArguments()
	{
		return 0;
	}

	public int offsetOfDefaultInArguments()
	{
		return 1;
	}

	public int offsetOfCasesInArguments()
	{
		return 2;
	}

	public int numberOfCases()
	{
		return arguments().size() - offsetOfCasesInArguments();
	}


	@Override
	protected int countValuesCore( Collection<ExpressionNode> _uncountables )
	{
		return 1;
	}

	@Override
	protected void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg )
	{
		_to.append( "SWITCH( " );
		selector().describeToWithConfig( _to, _cfg );
		for (ExpressionNode caze : cases()) {
			_to.append( ", " );
			caze.describeToWithConfig( _to, _cfg );
		}
		_to.append( ", DEFAULT: " );
		defaultValue().describeToWithConfig( _to, _cfg );
		_to.append( " )" );
	}

	@Override
	protected ExpressionNode innerCloneWithoutArguments()
	{
		return new ExpressionNodeForSwitch();
	}

}
