package sej.internal.spreadsheet;

import java.io.IOException;

import sej.Spreadsheet.Cell;
import sej.Spreadsheet.Row;
import sej.describable.AbstractDescribable;
import sej.describable.DescriptionBuilder;

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

	public CellImpl(RowImpl _row, CellIndex _cellIndex)
	{
		super();
		this.cellIndex = _cellIndex;
		this.row = _row;
		this.cell = null;
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
