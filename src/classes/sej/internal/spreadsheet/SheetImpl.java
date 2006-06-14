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
import sej.Spreadsheet.Row;
import sej.describable.AbstractDescribable;
import sej.describable.DescriptionBuilder;


public class SheetImpl extends AbstractDescribable implements Spreadsheet.Sheet
{
	private final SpreadsheetImpl spreadsheet;
	private final int sheetIndex;
	private final List<RowImpl> rows = new ArrayList<RowImpl>();


	public SheetImpl(SpreadsheetImpl _spreadsheet)
	{
		this.spreadsheet = _spreadsheet;
		this.sheetIndex = _spreadsheet.getSheetList().size();
		_spreadsheet.getSheetList().add( this );
	}


	public SpreadsheetImpl getSpreadsheet()
	{
		return this.spreadsheet;
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


	public CellInstance getCell( String _cellNameOrCanonicalName, CellIndex _relativeTo )
	{
		return getSpreadsheet().getWorkbookCell( this, _cellNameOrCanonicalName, _relativeTo );
	}


	public static String getCanonicalNameForColumnIndex( int _columnIndex )
	{
		StringBuilder result = new StringBuilder();
		buildCanonicalNameForColumnIndex( result, _columnIndex );
		return result.toString();
	}


	public static void buildCanonicalNameForColumnIndex( StringBuilder _into, int _columnIndex )
	{
		if (_columnIndex <= 25) {
			_into.append( (char) ('A' + _columnIndex) );
		}
		else {
			int firstLetterIndex = _columnIndex / 26 - 1;
			int secondLetterIndex = _columnIndex % 26;
			_into.append( (char) ('A' + firstLetterIndex) );
			_into.append( (char) ('A' + secondLetterIndex) );
		}
	}


	public static String getCanonicalNameForCellIndex( int _columnIndex, int _rowIndex )
	{
		StringBuilder result = new StringBuilder();
		buildCanonicalNameForColumnIndex( result, _columnIndex );
		result.append( _rowIndex + 1 );
		return result.toString();
	}


	public CellIndex getCellIndexForCanonicalName( String _canonicalName, CellIndex _relativeTo )
	{
		return getCellIndexForCanonicalName( _canonicalName, _relativeTo, getSpreadsheet().getCellRefFormat() );
	}


	public CellIndex getCellIndexForCanonicalName( String _canonicalName, CellIndex _relativeTo, CellRefFormat _format )
	{
		switch (_format) {
		case A1:
			return getCellIndexForCanonicalNameA1( _canonicalName );
		case R1C1:
			return getCellIndexForCanonicalNameR1C1( _canonicalName, _relativeTo );
		}
		return null;
	}


	public CellIndex getCellIndexForCanonicalNameA1( String _canonicalName )
	{
		int colIndex = 0;
		int rowIndex = 0;
		for (int iCh = 0; iCh < _canonicalName.length(); iCh++) {
			char ch = _canonicalName.charAt( iCh );
			if ((ch >= 'A') && (ch <= 'Z')) {
				colIndex = 26 * colIndex + Character.getNumericValue( ch ) - 9;
			}
			else if ((ch >= '0') && (ch <= '9')) {
				rowIndex = 10 * rowIndex + Character.getNumericValue( ch );
			}
		}
		return new CellIndex( getSheetIndex(), colIndex - 1, rowIndex - 1 );
	}


	public CellIndex getCellIndexForCanonicalNameR1C1( String _canonicalName, CellIndex _relativeTo )
	{
		final int rowIndex = parseRCIndex( _relativeTo.rowIndex + 1, _canonicalName, 1 );
		final int colIndex = parseRCIndex( _relativeTo.columnIndex + 1, _canonicalName, _canonicalName.indexOf( 'C' ) + 1 );
		return new CellIndex( getSheetIndex(), colIndex - 1, rowIndex - 1 );
	}


	public static int parseRCIndex( int _relativeTo, String _canonicalName, int _at )
	{
		int result = _relativeTo;
		if (_at < _canonicalName.length()) {
			char ch = _canonicalName.charAt( _at );
			if ('[' == ch) {
				ch = _canonicalName.charAt( ++_at );
			}
			else if ('C' != ch) {
				result = 0;
			}
			StringBuilder sb = new StringBuilder();
			while (ch == '-' || (ch >= '0' && ch <= '9')) {
				sb.append( ch );
				if (++_at >= _canonicalName.length()) break;
				ch = _canonicalName.charAt( _at );
			}
			if (sb.length() > 0) {
				result += Integer.parseInt( sb.toString() );
			}
		}
		return result;
	}


	public CellInstance getCellForCanonicalName( String _canonicalName, CellIndex _relativeTo )
	{
		CellIndex index = getCellIndexForCanonicalName( _canonicalName, _relativeTo );
		return getRowList().get( index.rowIndex ).getCellList().get( index.columnIndex );
	}


	@Override
	public void describeTo( DescriptionBuilder _to ) throws IOException
	{
		_to.appendLine( "<sheet>" );
		_to.indent();
		for (RowImpl row : getRowList()) {
			row.describeTo( _to );
		}
		_to.outdent();
		_to.appendLine( "</sheet>" );
	}


}
