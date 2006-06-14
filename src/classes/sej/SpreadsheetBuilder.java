package sej;

import java.util.Date;

import sej.expressions.ExpressionNode;
import sej.expressions.Operator;

public interface SpreadsheetBuilder
{
	public Spreadsheet getSpreadsheet();
	public void newPage();
	public void newRow();
	public void newCell( ExpressionNode _expr );
	public void newCell( Number _const );
	public void newCell( String _const );
	public void newCell( Date _const );
	public void newCell( boolean _const );
	public void nameCell( String _string );
	public CellRef currentCell();
	public ExpressionNode ref( CellRef _cell );
	public ExpressionNode op( Operator _op, ExpressionNode... _args );
	
	public static interface CellRef 
	{
		// marker
	}
}
