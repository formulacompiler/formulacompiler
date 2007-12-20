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
package org.formulacompiler.spreadsheet.internal;

import java.io.IOException;
import java.util.List;

import org.formulacompiler.describable.AbstractDescribable;
import org.formulacompiler.describable.DescriptionBuilder;
import org.formulacompiler.runtime.New;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.Spreadsheet.Row;


public final class SheetImpl extends AbstractDescribable implements Spreadsheet.Sheet
{
	private final SpreadsheetImpl spreadsheet;
	private final int sheetIndex;
	private final String name;
	private final List<RowImpl> rows = New.list();


	public SheetImpl( SpreadsheetImpl _spreadsheet )
	{
		this( _spreadsheet, "Sheet" + (_spreadsheet.getSheetList().size() + 1) );
	}


	public SheetImpl( SpreadsheetImpl _spreadsheet, String _name )
	{
		this.spreadsheet = _spreadsheet;
		this.sheetIndex = _spreadsheet.getSheetList().size();
		this.name = _name;
		_spreadsheet.getSheetList().add( this );
	}


	public SpreadsheetImpl getSpreadsheet()
	{
		return this.spreadsheet;
	}


	public final String getName()
	{
		return this.name;
	}


	public Row[] getRows()
	{
		return this.rows.toArray( new Row[ this.rows.size() ] );
	}


	public int getSheetIndex()
	{
		return this.sheetIndex;
	}


	public List<RowImpl> getRowList()
	{
		return this.rows;
	}


	public int getMaxColumnCount()
	{
		int result = 0;
		for (RowImpl row : getRowList()) {
			final int colCount = row.getCellList().size();
			if (colCount > result) result = colCount;
		}
		return result;
	}


	public static String getNameA1ForCellIndex( int _columnIndex, int _rowIndex )
	{
		StringBuilder result = new StringBuilder();
		if (_columnIndex <= 25) {
			result.append( (char) ('A' + _columnIndex) );
		}
		else {
			int firstLetterIndex = _columnIndex / 26 - 1;
			int secondLetterIndex = _columnIndex % 26;
			result.append( (char) ('A' + firstLetterIndex) );
			result.append( (char) ('A' + secondLetterIndex) );
		}
		result.append( _rowIndex + 1 );
		return result.toString();
	}


	public void trim()
	{
		boolean canRemove = true;
		for (int i = getRowList().size() - 1; i >= 0; i--) {
			RowImpl row = getRowList().get( i );
			row.trim();
			if (canRemove) {
				if (row.getCellList().size() == 0) {
					getRowList().remove( i );
				}
				else canRemove = false;
			}
		}
	}


	@Override
	public void describeTo( DescriptionBuilder _to ) throws IOException
	{
		_to.vn( "name" ).v( getName() ).lf();
		_to.ln( "rows" ).l( getRowList() );
	}


}
