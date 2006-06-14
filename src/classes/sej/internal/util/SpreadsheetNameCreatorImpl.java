/*
 * Copyright © 2006 by Abacus Research AG, Switzerland.
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
package sej.internal.util;

import sej.Spreadsheet;
import sej.SpreadsheetNameCreator;
import sej.Spreadsheet.Cell;
import sej.Spreadsheet.Sheet;

public final class SpreadsheetNameCreatorImpl implements SpreadsheetNameCreator
{
	private final Spreadsheet spreadsheet;
	private final Sheet sheet;

	public SpreadsheetNameCreatorImpl(Config _cfg)
	{
		super();
		_cfg.validate();
		this.spreadsheet = _cfg.sheet.getSpreadsheet();
		this.sheet = _cfg.sheet;
	}


	public void createCellNamesFromRowTitles()
	{
		for (Spreadsheet.Row row : this.sheet.getRows()) {
			final Cell[] cells = row.getCells();
			if (cells.length >= 2) {
				final Cell titleCell = cells[ 0 ];
				final Object titleValue = titleCell.getConstantValue();
				if (titleValue instanceof String) {
					final String title = (String) titleValue;
					this.spreadsheet.defineName( title, cells[ 1 ] );
				}
			}
		}
	}


}
