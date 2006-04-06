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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import sej.Spreadsheet;
import sej.ModelError;
import sej.describable.AbstractDescribable;
import sej.describable.DescriptionBuilder;


/**
 * Implementation of {@link Spreadsheet}.
 * 
 * @author peo
 */
public class Workbook extends AbstractDescribable implements Spreadsheet
{
	private List<Sheet> sheets = new ArrayList<Sheet>();
	private Map<String, Reference> names = new HashMap<String, Reference>();


	public List<Sheet> getSheets()
	{
		return this.sheets;
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
			if (ref instanceof CellIndex) {
				final CellIndex cell = (CellIndex) ref;
				result[ i++ ] = new Spreadsheet.CellNameDefinition()
				{

					public Spreadsheet.Cell getCell()
					{
						return cell;
					}

					public String getName()
					{
						return name;
					}

				};
			}
			else if (ref instanceof CellRange) {
				final CellRange range = (CellRange) ref;
				result[ i++ ] = new Spreadsheet.RangeNameDefinition()
				{

					public Spreadsheet.Range getRange()
					{
						return range;
					}

					public String getName()
					{
						return name;
					}

				};
			}
			else throw new InternalError( "Unknown reference type encountered" );
		}
		return result;
	}


	public Spreadsheet.Cell getCell( int _sheetIndex, int _columnIndex, int _rowIndex )
	{
		return new CellIndex( _sheetIndex, _columnIndex, _rowIndex );
	}


	public Spreadsheet.Cell getCell( String _cellName ) throws ModelError.NameNotFound, IllegalArgumentException
	{
		Reference ref = getNamedRef( _cellName );
		if (null == ref) {
			throw new ModelError.NameNotFound( "The name '" + _cellName + "' is not defined in this workbook." );
		}
		else if (ref instanceof CellIndex) {
			return (CellIndex) ref;
		}
		else {
			throw new IllegalArgumentException( "The name '" + _cellName + "' is bound to a range, not a cell." );
		}
	}
	
	
	public Range getRange( String _rangeName ) throws ModelError.NameNotFound, IllegalArgumentException
	{
		final Reference ref = getNamedRef( _rangeName );
		if (null == ref) {
			throw new ModelError.NameNotFound( "The name '" + _rangeName + "' is not defined in this workbook." );
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


	// --------------------------------------- API for parser


	public Reference getNamedRef( String _ident )
	{
		return this.getNameMap().get( _ident );
	}


	public CellIndex getCellIndex( Sheet _defaultSheet, String _cellNameOrCanonicalName, CellIndex _relativeTo )
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


	// --------------------------------------- Own stuff


	CellIndex getCellIndex( String _cellNameOrCanonicalName )
	{
		return getCellIndex( getSheets().get( 0 ), _cellNameOrCanonicalName, null );
	}


	CellInstance getWorkbookCell( Sheet _defaultSheet, String _cellNameOrCanonicalName, CellIndex _relativeTo )
	{
		return getCellIndex( _defaultSheet, _cellNameOrCanonicalName, _relativeTo ).getCell( this );
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
		return getCellIndex( _cellNameOrCanonicalName ).getCell( this );
	}


	@Override
	public Object clone()
	{
		Workbook result = new Workbook();
		result.names.putAll( this.names );
		for (Sheet sheet : getSheets()) {
			if (null == sheet) {
				result.getSheets().add( null );
			}
			else {
				sheet.cloneInto( result );
			}
		}
		return result;
	}


	@Override
	public void describeTo( DescriptionBuilder _to ) throws IOException
	{
		_to.appendLine( "<workbook>" );
		_to.indent();
		for (Sheet sheet : getSheets()) {
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


	public CellRefFormat getCellRefFormat()
	{
		return CellRefFormat.A1; // TODO Change this for Excel!
	}

}
