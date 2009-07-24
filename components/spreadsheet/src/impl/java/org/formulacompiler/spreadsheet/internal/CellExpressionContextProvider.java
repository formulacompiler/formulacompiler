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

import org.formulacompiler.compiler.internal.DescriptionBuilder;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionSourceAsContextProvider;

final class CellExpressionContextProvider extends ExpressionSourceAsContextProvider
{
	private final CellInstance cell;

	public CellExpressionContextProvider( CellInstance _cell, ExpressionNode _expr )
	{
		super( _expr );
		this.cell = _cell;
	}


	@Override
	public void buildContext( DescriptionBuilder _result, ExpressionNode _focusedNode )
	{
		super.buildContext( _result, _focusedNode );
		_result.append( "\nCell containing expression is " ).append( this.cell.getCanonicalName() ).append( "." );
	}

	@Override
	public void setUpContext( DescriptionBuilder _builder )
	{
		_builder.pushContext( this.cell.getRow().getSheet() );
	}

	@Override
	public void cleanUpContext( DescriptionBuilder _builder )
	{
		_builder.popContext();
	}

}
