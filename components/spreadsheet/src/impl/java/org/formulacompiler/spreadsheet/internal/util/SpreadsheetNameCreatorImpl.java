/*
 * Copyright (c) 2006 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are prohibited, unless you have been explicitly granted
 * more rights by Abacus Research AG.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
