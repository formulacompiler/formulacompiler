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
package sej.internal.spreadsheet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sej.Spreadsheet;
import sej.Spreadsheet.Cell;
import sej.describable.AbstractDescribable;
import sej.describable.DescriptionBuilder;

public class RowImpl extends AbstractDescribable implements Spreadsheet.Row
{
	private final SheetImpl sheet;
	private final int rowIndex;
	private final List<CellInstance> cells = new ArrayList<CellInstance>();


	public RowImpl(SheetImpl _sheet)
	{
		this.sheet = _sheet;
		this.rowIndex = _sheet.getRowList().size();
		_sheet.getRowList().add( this );
	}


	public SheetImpl getSheet()
	{
		return this.sheet;
	}


	public Cell[] getCells()
	{
		final int sheetIndex = getSheet().getSheetIndex();
		final int rowIndex = getRowIndex();

		final Cell[] result = new Cell[ this.cells.size() ];
		for (int i = 0; i < this.cells.size(); i++) {
			final CellInstance cellInst = this.cells.get( i );
			if (cellInst == null) {
				result[ i ] = new CellImpl( this, new CellIndex( sheetIndex, i, rowIndex ) );
			}
			else {
				result[ i ] = new CellImpl( cellInst );
			}
		}
		return result;
	}


	public int getRowIndex()
	{
		return this.rowIndex;
	}


	public List<CellInstance> getCellList()
	{
		return this.cells;
	}


	public CellInstance getCellOrNull( int _columnIndex )
	{
		if (_columnIndex < getCellList().size()) return getCellList().get( _columnIndex );
		else return null;
	}


	public CellIndex getCellIndex( int _columnIndex )
	{
		return new CellIndex( getSheet().getSheetIndex(), _columnIndex, getRowIndex() );
	}


	@Override
	public void describeTo( DescriptionBuilder _to ) throws IOException
	{
		_to.appendLine( "<row>" );
		_to.indent();
		for (CellInstance cell : getCellList()) {
			if (null != cell) {
				cell.describeTo( _to );
			}
			else {
				_to.appendLine( "<cell />" );
			}
		}
		_to.outdent();
		_to.appendLine( "</row>" );
	}

}
