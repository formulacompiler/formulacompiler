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
	private List<SheetImpl> sheets = New.newList();
	private Map<String, Reference> names = New.newMap();


	public List<SheetImpl> getSheetList()
	{
		return this.sheets;
	}


	public void addToNameMap( String _name, Reference _ref )
	{
		this.names.put( _name.toUpperCase(), _ref );
	}

	public Map<String, Reference> getNameMap()
	{
		return this.names;
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
		this.names.put( _name.toUpperCase(), (CellIndex) _cell );
	}


	public Spreadsheet.Cell getCell( int _sheetIndex, int _columnIndex, int _rowIndex )
	{
		return new CellIndex( this, _sheetIndex, _columnIndex, _rowIndex );
	}


	public Spreadsheet.Cell getCell( String _cellName ) throws SpreadsheetException.NameNotFound
	{
		Reference ref = getNamedRef( _cellName );
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


	public CellIndex getCellIndex( SheetImpl _defaultSheet, String _cellNameOrCanonicalName, CellIndex _relativeTo )
	{
		Reference ref = getNamedRef( _cellNameOrCanonicalName );
		if (ref instanceof CellIndex) {
			return (CellIndex) ref;
		}
		else if (ref instanceof CellRange) {
			CellRange range = (CellRange) ref;
			return range.getFrom();
		}
		else {
			return _defaultSheet.getCellIndexForCanonicalName( _cellNameOrCanonicalName, _relativeTo );
		}
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


	CellIndex getCellIndex( String _cellNameOrCanonicalName )
	{
		return getCellIndex( getSheetList().get( 0 ), _cellNameOrCanonicalName, null );
	}


	CellInstance getWorkbookCell( SheetImpl _defaultSheet, String _cellNameOrCanonicalName, CellIndex _relativeTo )
	{
		return getCellIndex( _defaultSheet, _cellNameOrCanonicalName, _relativeTo ).getCell();
	}


	/**
	 * Finds cells by name.
	 * 
	 * @param _cellNameOrCanonicalName is the canonical name of the cell (A1, B2, etc.), or its
	 *           specific name defined in the spreadsheet (BasePrice, NumberSold, etc.).
	 * @return The requested cell, or else {@code null}.
	 */
	CellInstance getWorkbookCell( String _cellNameOrCanonicalName )
	{
		return getCellIndex( _cellNameOrCanonicalName ).getCell();
	}


	public CellRefFormat getCellRefFormat()
	{
		return CellRefFormat.A1; // LATER Change this for Excel!
	}


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
	public void describeTo( DescriptionBuilder _to ) throws IOException
	{
		_to.appendLine( "<workbook>" );
		_to.indent();
		for (Sheet sheet : getSheetList()) {
			sheet.describeTo( _to );
		}
		describeNamesTo( _to );
		_to.outdent();
		_to.appendLine( "</workbook>" );
	}


	private void describeNamesTo( DescriptionBuilder _to ) throws IOException
	{
		if (0 < getNameMap().size()) {
			_to.appendLine( "<names>" );
			for (Entry<String, Reference> entry : getNameMap().entrySet()) {
				String name = entry.getKey();
				Reference ref = entry.getValue();
				_to.append( "<name id=\"" );
				_to.append( name );
				_to.append( "\" ref=\"" );
				ref.describeTo( _to );
				_to.append( "\" />" );
				_to.newLine();
			}
			_to.appendLine( "</names>" );
		}
	}


}
