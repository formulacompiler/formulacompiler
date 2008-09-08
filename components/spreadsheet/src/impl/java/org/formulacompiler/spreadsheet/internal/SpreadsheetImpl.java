/*
 * Copyright (c) 2006, 2008 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * This file is part of the Abacus Formula Compiler (AFC).
 *
 * For commercial licensing, please contact sales(at)formulacompiler.com.
 *
 * AFC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AFC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AFC.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.formulacompiler.spreadsheet.internal;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.formulacompiler.compiler.internal.AbstractYamlizable;
import org.formulacompiler.compiler.internal.YamlBuilder;
import org.formulacompiler.runtime.ComputationMode;
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
	private final Map<String, CellRange> modelRangeNames = New.caseInsensitiveMap();
	private final Map<String, CellRange> readOnlyModelRangeNames = Collections.unmodifiableMap( this.modelRangeNames );
	private final Map<CellIndex, Set<String>> namedCells = New.map();
	private final ComputationMode computationMode;
	private Map<String, Range> userRangeNames = null;
	private Map<String, Range> readOnlyRangeNames = Collections
			.unmodifiableMap( (Map<String, ? extends Range>) this.modelRangeNames );


	public SpreadsheetImpl( final ComputationMode _mode )
	{
		this.computationMode = _mode;
	}


	public SpreadsheetImpl()
	{
		this( ComputationMode.EXCEL );
	}


	public List<SheetImpl> getSheetList()
	{
		return this.sheets;
	}


	public void defineModelRangeName( String _name, CellRange _ref )
	{
		this.modelRangeNames.put( _name, _ref );
		if (_ref instanceof CellIndex) {
			final CellIndex cellIndex = (CellIndex) _ref;
			final Set<String> existingCellNames = this.namedCells.get( cellIndex );
			final Set<String> cellNames;
			if (existingCellNames != null) {
				cellNames = existingCellNames;
			}
			else {
				cellNames = New.sortedSet();
				this.namedCells.put( cellIndex, cellNames );
			}
			cellNames.add( _name );
		}
		if (null != this.userRangeNames) {
			this.userRangeNames.put( _name, _ref );
		}
	}

	public Map<String, CellRange> getModelRangeNames()
	{
		return this.readOnlyModelRangeNames;
	}

	public Set<String> getModelNamesFor( CellIndex _cell )
	{
		return this.namedCells.get( _cell );
	}

	// --------------------------------------- Implement Spreadsheet


	public Map<String, Range> getRangeNames()
	{
		return this.readOnlyRangeNames;
	}


	public void defineAdditionalRangeName( String _name, Range _ref )
	{
		if (null == _name) throw new IllegalArgumentException( "name is null" );
		if (null == _ref) throw new IllegalArgumentException( "range is null" );
		if (null != this.modelRangeNames.get( _name ))
			throw new IllegalArgumentException( "name already defined in model" );

		if (null == this.userRangeNames) {
			this.userRangeNames = New.caseInsensitiveMap();
			this.readOnlyRangeNames = Collections.unmodifiableMap( this.userRangeNames );
		}
		this.userRangeNames.put( _name, _ref );
	}


	public Spreadsheet.Cell getCell( int _sheetIndex, int _columnIndex, int _rowIndex )
	{
		return new CellIndex( this, _sheetIndex, _columnIndex, _rowIndex );
	}


	public Spreadsheet.Cell getCell( String _cellName ) throws SpreadsheetException.NameNotFound
	{
		final Range ref = getRange( _cellName );
		if (!(ref instanceof CellIndex)) {
			throw new IllegalArgumentException( "The name '" + _cellName + "' is bound to a range, not a cell." );
		}
		return (Cell) ref;
	}


	public Spreadsheet.Cell getCellA1( String _a1Name ) throws SpreadsheetException.NameNotFound
	{
		if (0 == getSheetList().size()) {
			throw new SpreadsheetException.NameNotFound( "The name '" + _a1Name + "' is not defined; workbook is empty." );
		}
		final CellRefParser parser = CellRefParser.getInstance( CellRefFormat.A1 );
		final CellIndex cell = parser.getCellIndexForCanonicalName( _a1Name, new CellIndex( this, 0, 0, 0 ) );
		if (null == cell) {
			throw new SpreadsheetException.NameNotFound( "The name '" + _a1Name + "' is not defined in this workbook." );
		}
		return cell;
	}


	public Range getRange( String _rangeName ) throws SpreadsheetException.NameNotFound
	{
		final Range ref = getRangeNames().get( _rangeName );
		if (null == ref) {
			throw new SpreadsheetException.NameNotFound( "The name '" + _rangeName + "' is not defined in this workbook." );
		}
		return ref;
	}


	public Sheet[] getSheets()
	{
		return this.sheets.toArray( new Sheet[this.sheets.size()] );
	}

	// --------------------------------------- API for parser


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
		for (SheetImpl sheet : getSheetList()) {
			sheet.trim();
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

			final Map<String, CellRange> nameMap = getModelRangeNames();
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


	public ComputationMode getComputationMode()
	{
		return this.computationMode;
	}


}
