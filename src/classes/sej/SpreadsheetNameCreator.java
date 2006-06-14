package sej;


/**
 * Utility interface that supports the creation of cell names from other cells' values.
 * 
 * @author peo
 */
public interface SpreadsheetNameCreator
{
	public static class Config
	{
		public Spreadsheet.Sheet sheet;

		public void validate()
		{
			if (this.sheet == null) throw new IllegalArgumentException( "sheet is null" );
		}
	}


	/**
	 * Creates cell names from row titles. A row title is the constant string value of the leftmost
	 * cell of a row. This value is given as a cell name to the cell just to the right of it. The
	 * method only processes rows which have two leftmost cells and the leftmost one of them holds a
	 * constant string value.
	 */
	public void createCellNamesFromRowTitles();


}
