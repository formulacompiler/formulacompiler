package sej.internal.spreadsheet.namer;

import sej.api.Spreadsheet;
import sej.api.SpreadsheetNameCreator;
import sej.api.Spreadsheet.Cell;
import sej.api.Spreadsheet.Sheet;

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
