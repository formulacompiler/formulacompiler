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
import org.formulacompiler.runtime.spreadsheet.CellAddress;


public final class ExpressionNodeForCell extends ExpressionNode
{
	private final CellIndex cellIndex;
	private final String name;


	public ExpressionNodeForCell( CellInstance _cell )
	{
		this.cellIndex = _cell.getCellIndex();
		this.name = null;
	}


	public ExpressionNodeForCell( CellIndex _c )
	{
		this.cellIndex = _c;
		this.name = null;
	}

	public ExpressionNodeForCell( CellIndex _c, String _name )
	{
		this.cellIndex = _c;
		this.name = _name;
	}

	public CellIndex getCellIndex()
	{
		return this.cellIndex;
	}


	public String getName()
	{
		return this.name;
	}


	@Override
	protected CellAddress getCellAddress()
	{
		return this.cellIndex.getCellAddress();
	}


	@Override
	public ExpressionNode innerCloneWithoutArguments()
	{
		return new ExpressionNodeForCell( this.cellIndex, this.name );
	}


	@Override
	protected int countValuesCore( Collection<ExpressionNode> _uncountables )
	{
		return 1;
	}


	@Override
	public void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg )
	{
		if (this.name != null) {
			_to.append( this.name );
		}
		else {
			this.cellIndex.describeTo( _to );
		}
	}


	@Override
	protected ExpressionNode innerCloneWithOffset( int _colOffset, int _rowOffset )
	{
		return new ExpressionNodeForCell( (CellIndex) this.cellIndex.clone( _colOffset, _rowOffset ), this.name );
	}

}
