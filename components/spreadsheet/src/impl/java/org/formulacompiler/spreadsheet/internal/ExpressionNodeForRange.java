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


public final class ExpressionNodeForRange extends ExpressionNode
{
	private final CellRange range;
	private final String name;


	public ExpressionNodeForRange( CellRange _range )
	{
		this.range = _range;
		this.name = null;
	}

	public ExpressionNodeForRange( CellRange _range, String _name )
	{
		this.range = _range;
		this.name = _name;
	}


	public CellRange getRange()
	{
		return this.range;
	}


	public String getName()
	{
		return this.name;
	}


	@Override
	public ExpressionNode innerCloneWithoutArguments()
	{
		return new ExpressionNodeForRange( this.range, this.name );
	}


	@Override
	public ExpressionNode innerCloneWithOffset( int _colOffset, int _rowOffset )
	{
		return new ExpressionNodeForRange( this.range.clone( _colOffset, _rowOffset ), this.name );
	}


	@Override
	protected int countValuesCore( Collection<ExpressionNode> _uncountables )
	{
		throw new AbstractMethodError();
	}


	@Override
	public void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg )
	{
		if (this.name != null) {
			_to.append( this.name );
		}
		else {
			this.range.describeTo( _to );
		}
	}


}