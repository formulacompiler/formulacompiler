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

package org.formulacompiler.compiler.internal.expressions.parser;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;


public abstract class ExpressionParser extends GeneratedExpressionParser
{
	private final CellRefFormat cellRefFormat;
	private final String exprText;


	protected ExpressionParser( String _exprText, CellRefFormat _cellRefFormat )
	{
		super( new StringCharStream( _exprText ) );
		this.cellRefFormat = _cellRefFormat;
		this.exprText = _exprText;
	}

	@Override
	protected CellRefFormat getCellRefFormat()
	{
		return this.cellRefFormat;
	}

	public ExpressionNode parse() throws CompilerException
	{
		try {
			rootExpr();
			return popNode();
		}
		catch (InnerParserException e) {
			throw adorn( e.getCause() );
		}
		catch (ParseException e) {
			throw adorn( e );
		}
	}


	protected CompilerException adorn( Throwable _e )
	{
		if (_e instanceof CompilerException.UnsupportedExpressionSource) {
			return (CompilerException.UnsupportedExpressionSource) _e;
		}
		return new CompilerException.UnsupportedExpressionSource( _e, this.exprText, this.token.endColumn - 1 );
	}

}
