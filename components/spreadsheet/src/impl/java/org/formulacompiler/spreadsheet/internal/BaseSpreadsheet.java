/*
 * Copyright (c) 2006-2009 by Abacus Research AG, Switzerland.
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


public abstract class BaseSpreadsheet extends AbstractYamlizable implements Spreadsheet
{
	private final Map<String, CellRange> modelRangeNames = New.caseInsensitiveMap();
	private final Map<String, CellRange> readOnlyModelRangeNames = Collections.unmodifiableMap( this.modelRangeNames );
	private final Map<Range, Set<String>> modelNamedRanges = New.map();
	private final ComputationMode computationMode;
	private Map<String, Range> userRangeNames = null;
	private Map<Range, Set<String>> userNamedRanges = null;
	private Map<String, Range> readOnlyRangeNames = Collections
			.unmodifiableMap( (Map<String, ? extends Range>) this.modelRangeNames );

	public BaseSpreadsheet( final ComputationMode _mode )
	{
		this.computationMode = _mode;
	}

	public abstract List<? extends BaseSheet> getSheetList();

	public void defineModelRangeName( String _name, CellRange _ref )
	{
		this.modelRangeNames.put( _name, _ref );
		putRangeName( this.modelNamedRanges, _ref, _name );
		if (null != this.userRangeNames) {
			this.userRangeNames.put( _name, _ref );
		}
		if (null != this.userNamedRanges) {
			putRangeName( this.userNamedRanges, _ref, _name );
		}
	}

	public Map<String, CellRange> getModelRangeNames()
	{
		return this.readOnlyModelRangeNames;
	}

	public Set<String> getModelNamesFor( Range _range )
	{
		return this.modelNamedRanges.get( _range );
	}

	public Set<String> getNamesFor( Range _range )
	{
		final Set<String> modelNames = this.modelNamedRanges.get( _range );
		if (this.userNamedRanges == null)
			return modelNames;
		final Set<String> userNames = this.userNamedRanges.get( _range );
		if (modelNames == null || modelNames.isEmpty())
			return userNames;
		if (userNames == null || userNames.isEmpty())
			return modelNames;		
		final Set<String> names = New.set();
		names.addAll( modelNames );
		names.addAll( userNames );
		return names;
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
			this.userRangeNames.putAll( this.modelRangeNames );
			this.readOnlyRangeNames = Collections.unmodifiableMap( this.userRangeNames );
		}
		this.userRangeNames.put( _name, _ref );

		if (null == this.userNamedRanges) {
			this.userNamedRanges = New.map();
		}
		putRangeName( this.userNamedRanges, _ref, _name );
	}

	private void putRangeName( Map<Range, Set<String>> _namedRanges, Range _ref, String _name )
	{
		final Set<String> existingCellNames = _namedRanges.get( _ref );
		final Set<String> cellNames;
		if (existingCellNames != null) {
			cellNames = existingCellNames;
		}
		else {
			cellNames = New.sortedSet();
			_namedRanges.put( _ref, cellNames );
		}
		cellNames.add( _name );
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
		return CellRefParser.A1.parseCellA1( _a1Name, new CellIndex( this, 0, 0, 0 ) );
	}


	public Range getRange( String _rangeName ) throws SpreadsheetException.NameNotFound
	{
		final Range ref = getRangeNames().get( _rangeName );
		if (null == ref) {
			throw new SpreadsheetException.NameNotFound( "The name '" + _rangeName + "' is not defined in this workbook." );
		}
		return ref;
	}


	public Spreadsheet.Range getRangeA1( String _a1Name ) throws SpreadsheetException.NameNotFound
	{
		if (0 == getSheetList().size()) {
			throw new SpreadsheetException.NameNotFound( "The name '" + _a1Name + "' is not defined; workbook is empty." );
		}
		return CellRefParser.A1.parseCellRangeA1( _a1Name, new CellIndex( this, 0, 0, 0 ) );
	}


	public Sheet[] getSheets()
	{
		final List<? extends BaseSheet> sheetList = getSheetList();
		return sheetList.toArray( new Sheet[sheetList.size()] );
	}

	// --------------------------------------- API for parser


	public BaseSheet getSheet( String _sheetName )
	{
		for (BaseSheet s : getSheetList()) {
			if (_sheetName.equalsIgnoreCase( s.getName() )) {
				return s;
			}
		}
		throw new IllegalArgumentException( "The sheet '" + _sheetName + "' does not exist in this workbook." );
	}

	// --------------------------------------- Own stuff


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
