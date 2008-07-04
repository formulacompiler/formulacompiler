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

package org.formulacompiler.spreadsheet.internal.parser;

import org.formulacompiler.spreadsheet.internal.CellInstance;
import org.formulacompiler.spreadsheet.internal.SpreadsheetImpl;

public class SpreadsheetExpressionParserA1ODF extends SpreadsheetExpressionParser
{

	public SpreadsheetExpressionParserA1ODF( String _exprText, CellInstance _parseRelativeTo )
	{
		super( _exprText, _parseRelativeTo );
	}

	public SpreadsheetExpressionParserA1ODF( String _exprText, SpreadsheetImpl _workbook )
	{
		super( _exprText, _workbook );
	}

}
