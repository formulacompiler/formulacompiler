package sej;



/**
 * Utility interface that supports simple cell binding using the cell names in the spreadsheet and
 * reflection on the input and output types.
 * 
 * @author peo
 */
public interface SpreadsheetByNameBinder
{

	public static class Config
	{
		public SpreadsheetBinder binder;

		public void validate()
		{
			if (this.binder == null) throw new IllegalArgumentException( "binder is null" );
		}
	}

	
	public CellBinder inputs();

	
	public CellBinder outputs();


	public static interface CellBinder
	{
		
		public void bindAllMethodsToNamedCells() throws CompilerError;

		public void bindAllNamedCellsToMethods() throws CompilerError;
		
	}

}
