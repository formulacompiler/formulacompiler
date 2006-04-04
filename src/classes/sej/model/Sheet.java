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
package sej.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import sej.describable.AbstractDescribable;
import sej.describable.DescriptionBuilder;


public class Sheet extends AbstractDescribable
{
	private final Workbook workbook;
	private final int sheetIndex;
	private final List<Row> rows = new ArrayList<Row>();
	private final Pattern rcStyle = Pattern.compile( "R[\\[\\]\\-1-9]*C[\\[\\]\\-1-9]*" );


	public Sheet(Workbook _workbook)
	{
		this.workbook = _workbook;
		this.sheetIndex = _workbook.getSheets().size();
		_workbook.getSheets().add( this );
	}


	public Workbook getWorkbook()
	{
		return this.workbook;
	}


	public int getSheetIndex()
	{
		return this.sheetIndex;
	}


	public List<Row> getRows()
	{
		return this.rows;
	}


	public int getMaxColumnCount()
	{
		int result = 0;
		for (Row row : getRows()) {
			final int colCount = row.getCells().size();
			if (colCount > result) result = colCount;
		}
		return result;
	}


	public CellInstance getCell( String _cellNameOrCanonicalName, CellIndex _relativeTo )
	{
		return getWorkbook().getWorkbookCell( this, _cellNameOrCanonicalName, _relativeTo );
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
		int colIndex = 0;
		int rowIndex = 0;

		if (this.rcStyle.matcher( _canonicalName ).matches()) {
			rowIndex = parseRCIndex( _relativeTo.rowIndex + 1, _canonicalName, 1 );
			colIndex = parseRCIndex( _relativeTo.columnIndex + 1, _canonicalName, _canonicalName.indexOf( 'C' ) + 1 );
		}

		else {
			for (int iCh = 0; iCh < _canonicalName.length(); iCh++) {
				char ch = _canonicalName.charAt( iCh );
				if ((ch >= 'A') && (ch <= 'Z')) {
					colIndex = 26 * colIndex + Character.getNumericValue( ch ) - 9;
				}
				else {
					rowIndex = 10 * rowIndex + Character.getNumericValue( ch );
				}
			}
		}

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
		return getRows().get( index.rowIndex ).getCells().get( index.columnIndex );
	}


	public Sheet cloneInto( Workbook _result )
	{
		Sheet result = new Sheet( _result );
		for (Row row : getRows()) {
			if (null == row) {
				result.getRows().add( null );
			}
			else {
				row.cloneInto( result );
			}
		}
		return result;
	}


	@Override
	public void describeTo( DescriptionBuilder _to ) throws IOException
	{
		_to.appendLine( "<sheet>" );
		_to.indent();
		for (Row row : getRows()) {
			row.describeTo( _to );
		}
		_to.outdent();
		_to.appendLine( "</sheet>" );
	}


}
