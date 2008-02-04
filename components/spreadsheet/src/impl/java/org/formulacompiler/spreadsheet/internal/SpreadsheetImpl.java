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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.formulacompiler.compiler.internal.AbstractYamlizable;
import org.formulacompiler.compiler.internal.YamlBuilder;
import org.formulacompiler.runtime.New;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetException;


/**
 * Implementation of {@link Spreadsheet}.
 * 
 * @author peo
 */
public final class SpreadsheetImpl extends AbstractYamlizable implements Spreadsheet
{
	private final List<SheetImpl> sheets = New.list();
	private final Map<String, CellRange> names = New.caseInsensitiveMap();
	private final Map<CellIndex, String> namedCells = New.map();


	public List<SheetImpl> getSheetList()
	{
		return this.sheets;
	}


	public void addToNameMap( String _name, CellRange _ref )
	{
		final String upperName = _name.toUpperCase();
		this.names.put( upperName, _ref );
		if (_ref instanceof CellIndex) {
			this.namedCells.put( (CellIndex) _ref, upperName );
		}
	}

	public Map<String, CellRange> getNameMap()
	{
		return this.names;
	}

	public String getNameFor( CellIndex _cell )
	{
		return this.namedCells.get( _cell );
	}


	// --------------------------------------- Implement Spreadsheet


	public Map<String, Range> getRangeNames()
	{
		return Collections.unmodifiableMap( (Map<String, ? extends Range>) this.names );
	}


	public void defineName( String _name, Range _ref )
	{
		addToNameMap( _name, (CellRange) _ref );
	}


	public Spreadsheet.Cell getCell( int _sheetIndex, int _columnIndex, int _rowIndex )
	{
		return new CellIndex( this, _sheetIndex, _columnIndex, _rowIndex );
	}


	public Spreadsheet.Cell getCell( String _cellName ) throws SpreadsheetException.NameNotFound
	{
		final CellRange ref = getNamedRef( _cellName );
		if (null == ref) {
			throw new SpreadsheetException.NameNotFound( "The name '" + _cellName + "' is not defined in this workbook." );
		}
		else if (ref instanceof CellIndex) {
			return (CellIndex) ref;
		}
		else {
			throw new IllegalArgumentException( "The name '" + _cellName + "' is bound to a range, not a cell." );
		}
	}


	public Spreadsheet.Cell getCellA1( String _a1Name ) throws SpreadsheetException.NameNotFound
	{
		if (0 == getSheetList().size()) {
			throw new SpreadsheetException.NameNotFound( "The name '" + _a1Name + "' is not defined; workbook is empty." );
		}
		final CellRefParser parser = CellRefParser.getInstance( CellRefFormat.A1 );
		final CellIndex cell = parser.getCellIndexForCanonicalName( _a1Name, getSheetList().get( 0 ), null );
		if (null == cell) {
			throw new SpreadsheetException.NameNotFound( "The name '" + _a1Name + "' is not defined in this workbook." );
		}
		return cell;
	}


	public Range getRange( String _rangeName ) throws SpreadsheetException.NameNotFound
	{
		final CellRange ref = getNamedRef( _rangeName );
		if (null == ref) {
			throw new SpreadsheetException.NameNotFound( "The name '" + _rangeName + "' is not defined in this workbook." );
		}
		return ref;
	}


	public Sheet[] getSheets()
	{
		return this.sheets.toArray( new Sheet[ this.sheets.size() ] );
	}


	// --------------------------------------- API for parser


	public CellRange getNamedRef( String _name )
	{
		return getNameMap().get( _name );
	}


	public SheetImpl getSheet( String _sheetName )
	{
		for (SheetImpl s : getSheetList()) {
			if (_sheetName.equalsIgnoreCase( s.getName() )) {
				return s;
			}
		}
		return null;
	}


	// --------------------------------------- Own stuff


	public void trim()
	{
		boolean canRemove = true;
		for (int i = getSheetList().size() - 1; i >= 0; i--) {
			SheetImpl sheet = getSheetList().get( i );
			sheet.trim();
			if (canRemove) {
				if (sheet.getRowList().size() == 0) {
					getSheetList().remove( i );
				}
				else canRemove = false;
			}
		}
	}


	@Override
	public void yamlTo( YamlBuilder _to )
	{
		_to.s( "# Abacus Formula Compiler Spreadsheet Model Description v1.0" ).lf();
		_to.s( "# WARNING: THIS FILE MUST NOT CONTAIN HARD TABS!" ).lf();
		_to.s( "---" ).lf();

		_to.desc().pushContext( new DescribeR1C1Style() );
		try {

			_to.lf().ln( "sheets" ).lSpaced( getSheetList() );

			final Map<String, CellRange> nameMap = getNameMap();
			if (0 < nameMap.size()) {
				_to.lf().ln( "names" );

				final List<String> names = New.list( nameMap.size() );
				names.addAll( nameMap.keySet() );
				Collections.sort( names );
				for (final String name : names) {
					final CellRange ref = nameMap.get( name );
					if (!(ref instanceof CellIndex)) {
						_to.desc().onNewLine().append( "- " ).indent();
						{
							_to.vn( "name" ).v( name ).lf();
							_to.vn( "ref" ).v( ref ).lf();
						}
						_to.desc().outdent();
					}
				}
			}

		}
		finally {
			_to.desc().popContext();
		}

	}

}
