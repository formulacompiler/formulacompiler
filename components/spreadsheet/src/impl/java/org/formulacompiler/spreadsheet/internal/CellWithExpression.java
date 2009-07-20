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

package org.formulacompiler.spreadsheet.internal;

import org.formulacompiler.compiler.internal.DescriptionBuilder;
import org.formulacompiler.compiler.internal.YamlBuilder;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.spreadsheet.SpreadsheetException;


public class CellWithExpression extends CellInstance
{
	private ExpressionNode expression;

	public CellWithExpression( RowImpl _row )
	{
		super( _row );
	}

	public CellWithExpression( RowImpl _row, ExpressionNode _node )
	{
		super( _row );
		setExpression( _node );
	}


	/**
	 * Parses if necessary and returns the contained expression.
	 *
	 * @return expression contained in this cell.
	 * @throws SpreadsheetException if expression cannot be parsed.
	 */
	public ExpressionNode getExpression() throws SpreadsheetException
	{
		return this.expression;
	}


	public void setExpression( ExpressionNode _value )
	{
		this.expression = _value;
		if (null != _value) {
			_value.setContextProviderOnThisAndArgumentsRecursively( new CellExpressionContextProvider( this, _value ) );
		}
		/*
		 * We don't free the expression parser here because the reference tests need access to the
		 * original expression text. And freeing it would yield minimal benefits, it seems.
		 */
	}


	public String getExpressionText()
	{
		final Object exp = getDescribableExpression();
		return exp != null ? exp.toString() : null;
	}

	private Object getDescribableExpression()
	{
		try {
			return getExpression();
		}
		catch (SpreadsheetException e) {
			final String exDesc = e.toString();
			return "** " + exDesc.replace( "\r\n", "\n" );
		}
	}

	@Override
	public void describeTo( DescriptionBuilder _to )
	{
		_to.append( getDescribableExpression() );
		final Object v = getValue();
		if (null != v) {
			_to.append( " (value=" ).append( v ).append( ")" );
		}
	}

	@Override
	public void yamlTo( YamlBuilder _to )
	{
		_to.desc().pushContext( new DescribeR1C1Style( getCellIndex() ) );
		try {
			_to.vn( "expr" ).s( '=' ).v( getDescribableExpression() ).lf(); // always shown, so don't use nv()
		}
		finally {
			_to.desc().popContext();
		}
		_to.nv( "value", getValue() );
		super.yamlTo( _to );
	}

}
