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

package org.formulacompiler.spreadsheet.internal;

import java.util.Collection;

import org.formulacompiler.compiler.internal.DescriptionBuilder;
import org.formulacompiler.compiler.internal.expressions.ExpressionDescriptionConfig;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.LetDictionary;


public final class ExpressionNodeForRangeIntersection extends ExpressionNode
{

	public ExpressionNodeForRangeIntersection( Collection<ExpressionNode> _args )
	{
		super( _args );
	}


	public ExpressionNodeForRangeIntersection()
	{
		super();
	}


	@Override
	public ExpressionNode innerCloneWithoutArguments()
	{
		return new ExpressionNodeForRangeIntersection();
	}


	@Override
	protected int countValues( LetDictionary _letDict, Collection<ExpressionNode> _uncountables )
	{
		throw new UnsupportedOperationException();
	}


	@Override
	public void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg )
	{
		describeArgumentTo( _to, _cfg, 0 );
		for (int iArg = 1; iArg < arguments().size(); iArg++) {
			_to.append( " " );
			describeArgumentTo( _to, _cfg, iArg );
		}
	}

}
