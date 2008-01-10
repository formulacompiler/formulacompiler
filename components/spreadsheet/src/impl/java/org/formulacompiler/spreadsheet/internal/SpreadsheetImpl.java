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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.formulacompiler.describable.AbstractDescribable;
import org.formulacompiler.describable.DescriptionBuilder;
import org.formulacompiler.runtime.New;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetException;


/**
 * Implementation of {@link Spreadsheet}.
 * 
 * @author peo
 */
public final class SpreadsheetImpl extends AbstractDescribable implements Spreadsheet
{
	private final List<SheetImpl> sheets = New.list();
	private final Map<String, Reference> names = New.map();
	private final Map<CellIndex, String> namedCells = New.map();


	public List<SheetImpl> getSheetList()
	{
		return this.sheets;
	}


	public void addToNameMap( String _name, Reference _ref )
	{
		final String upperName = _name.toUpperCase();
		this.names.put( upperName, _ref );
		if (_ref instanceof CellIndex) {
			this.namedCells.put( (CellIndex) _ref, upperName );
		}
	}

	public Map<String, Reference> getNameMap()
	{
		return this.names;
	}

	public String getNameFor( CellIndex _cell )
	{
		return this.namedCells.get( _cell );
	}


	// --------------------------------------- Implement Spreadsheet


	public Spreadsheet.NameDefinition[] getDefinedNames()
	{
		final Set<Entry<String, Reference>> entries = this.names.entrySet();
		final Spreadsheet.NameDefinition[] result = new Spreadsheet.NameDefinition[ entries.size() ];
		int i = 0;
		for (Entry<String, Reference> entry : entries) {
			final String name = entry.getKey();
			final Reference ref = entry.getValue();
			result[ i++ ] = getNameDefinition( name, ref );
		}
		return result;
	}


	public Spreadsheet.NameDefinition getDefinedName( String _name )
	{
		Reference ref = this.names.get( _name );
		return (ref != null) ? getNameDefinition( _name, ref ) : null;
	}


	private Spreadsheet.NameDefinition getNameDefinition( final String _name, final Reference _ref )
	{
		final String defName = _name.toUpperCase();
		if (_ref instanceof CellIndex) {
			final CellIndex cell = (CellIndex) _ref;
			return new Spreadsheet.CellNameDefinition()
			{

				public Spreadsheet.Cell getCell()
				{
					return cell;
				}

				public String getName()
				{
					return defName;
				}

			};
		}
		else if (_ref instanceof CellRange) {
			final CellRange range = (CellRange) _ref;
			return new Spreadsheet.RangeNameDefinition()
			{

				public Spreadsheet.Range getRange()
				{
					return range;
				}

				public String getName()
				{
					return defName;
				}

			};
		}
		else throw new InternalError( "Unknown reference type encountered" );

	}


	public void defineName( String _name, Cell _cell )
	{
		addToNameMap( _name, (Reference) _cell );
	}


	public Spreadsheet.Cell getCell( int _sheetIndex, int _columnIndex, int _rowIndex )
	{
		return new CellIndex( this, _sheetIndex, _columnIndex, _rowIndex );
	}


	public Spreadsheet.Cell getCell( String _cellName ) throws SpreadsheetException.NameNotFound
	{
		final Reference ref = getNamedRef( _cellName );
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
		final Reference ref = getNamedRef( _rangeName );
		if (null == ref) {
			throw new SpreadsheetException.NameNotFound( "The name '" + _rangeName + "' is not defined in this workbook." );
		}
		else if (ref instanceof CellRange) {
			return (CellRange) ref;
		}
		else if (ref instanceof CellIndex) {
			final CellIndex cellRef = (CellIndex) ref;
			return new CellRange( cellRef, cellRef );
		}
		else {
			throw new IllegalArgumentException( "The name '" + _rangeName + "' is not bound to a range or a cell." );
		}
	}


	public Sheet[] getSheets()
	{
		return this.sheets.toArray( new Sheet[ this.sheets.size() ] );
	}


	// --------------------------------------- API for parser


	public Reference getNamedRef( String _name )
	{
		return this.getNameMap().get( _name.toUpperCase() );
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
	protected DescriptionBuilder newDescriptionBuilder()
	{
		return new SpreadsheetDescriptionBuilder();
	}

	@Override
	public void describeTo( DescriptionBuilder _to ) throws IOException
	{
		_to.appendLine( "# Abacus Formula Compiler Spreadsheet Model Description v1.0" );
		_to.appendLine( "# WARNING: THIS FILE MUST NOT CONTAIN HARD TABS!" );
		_to.appendLine( "---" );

		_to.lf().ln( "sheets" ).lSpaced( getSheetList() );

		final Map<String, Reference> nameMap = getNameMap();
		if (0 < nameMap.size()) {
			_to.lf().ln( "names" );

			final List<String> names = New.list( nameMap.size() );
			names.addAll( nameMap.keySet() );
			Collections.sort( names );
			for (final String name : names) {
				final Reference ref = nameMap.get( name );
				if (ref instanceof CellRange) {
					_to.onNewLine().append( "- " ).indent();
					{
						_to.vn( "name" ).v( name ).lf();
						_to.vn( "ref" ).v( ref ).lf();
					}
					_to.outdent();
				}
			}
		}

	}

}
