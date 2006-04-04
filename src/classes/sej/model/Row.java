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
import java.util.List;
import java.util.ArrayList;

import sej.describable.AbstractDescribable;
import sej.describable.DescriptionBuilder;

public class Row extends AbstractDescribable
{
	private final Sheet sheet;
	private final int rowIndex;
	private final List<CellInstance> cells = new ArrayList<CellInstance>();


	public Row(Sheet _sheet)
	{
		this.sheet = _sheet;
		this.rowIndex = _sheet.getRows().size();
		_sheet.getRows().add( this );
	}


	public Sheet getSheet()
	{
		return this.sheet;
	}


	public int getRowIndex()
	{
		return this.rowIndex;
	}


	public List<CellInstance> getCells()
	{
		return this.cells;
	}


	public CellInstance getCellOrNull( int _columnIndex )
	{
		if (_columnIndex < getCells().size()) return getCells().get( _columnIndex );
		else return null;
	}


	public CellIndex getCellIndex( int _columnIndex )
	{
		return new CellIndex( getSheet().getSheetIndex(), _columnIndex, getRowIndex() );
	}


	public Row cloneInto( Sheet _result )
	{
		Row result = new Row( _result );
		for (CellInstance cell : getCells()) {
			if (null == cell) {
				result.getCells().add( null );
			}
			else {
				cell.cloneInto( result );
			}
		}
		return result;
	}


	@Override
	public void describeTo( DescriptionBuilder _to ) throws IOException
	{
		_to.appendLine( "<row>" );
		_to.indent();
		for (CellInstance cell : getCells()) {
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
