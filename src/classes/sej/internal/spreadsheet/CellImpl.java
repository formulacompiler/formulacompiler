package sej.internal.spreadsheet;

import java.io.IOException;

import sej.api.AbstractDescribable;
import sej.api.DescriptionBuilder;
import sej.api.Spreadsheet.Cell;
import sej.api.Spreadsheet.Row;

public class CellImpl extends AbstractDescribable implements Cell
{
	private final CellIndex cellIndex;
	private final RowImpl row;
	private final CellInstance cell;

	public CellImpl(SpreadsheetImpl _spreadsheet, CellIndex _cellIndex)
	{
		super();
		this.cellIndex = _cellIndex;
		this.row = _cellIndex.getRow( _spreadsheet );
		this.cell = _cellIndex.getCell( _spreadsheet );

	}

	public CellImpl(CellInstance _instance)
	{
		super();
		this.cellIndex = _instance.getCellIndex();
		this.row = _instance.getRow();
		this.cell = _instance;
	}

	public Object getConstantValue()
	{
		if (this.cell instanceof CellWithConstant) {
			return ((CellWithConstant) this.cell).getValue();
		}
		else {
			return null;
		}
	}

	public Row getRow()
	{
		return this.row;
	}

	public CellIndex getCellIndex()
	{
		return this.cellIndex;
	}

	@Override
	public void describeTo( DescriptionBuilder _to ) throws IOException
	{
		this.cellIndex.describeTo( _to );
	}

}
