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

package org.formulacompiler.spreadsheet.internal.util;

import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetNameCreator;
import org.formulacompiler.spreadsheet.Spreadsheet.Cell;
import org.formulacompiler.spreadsheet.Spreadsheet.Sheet;

public final class SpreadsheetNameCreatorImpl implements SpreadsheetNameCreator
{
	private final Spreadsheet spreadsheet;
	private final Sheet sheet;

	public SpreadsheetNameCreatorImpl( Config _cfg )
	{
		super();
		_cfg.validate();
		this.spreadsheet = _cfg.sheet.getSpreadsheet();
		this.sheet = _cfg.sheet;
	}

	public static final class Factory implements SpreadsheetNameCreator.Factory
	{
		public SpreadsheetNameCreator newInstance( Config _config )
		{
			return new SpreadsheetNameCreatorImpl( _config );
		}
	}


	public void createCellNamesFromRowTitles()
	{
		for (Spreadsheet.Row row : this.sheet.getRows()) {
			final Cell[] cells = row.getCells();
			if (cells.length >= 2) {
				final Cell titleCell = cells[ 0 ];
				if (null != titleCell) {
					final Object titleValue = titleCell.getConstantValue();
					if (titleValue instanceof String) {
						this.spreadsheet.defineAdditionalRangeName( sanitize( (String) titleValue ), cells[ 1 ] );
					}
				}
			}
		}
	}


	private String sanitize( String _source )
	{
		final char[] src = _source.toCharArray();
		final char[] tgt = new char[ src.length ];
		int n = 0;
		for (char c : src) {
			if (" -+*/#\"'%&$£^~§°=?!,.;:<>\\()[]{}\n\r\t".indexOf( c ) < 0) {
				tgt[ n++ ] = c;
			}
		}
		return String.valueOf( tgt, 0, n );
	}


}
