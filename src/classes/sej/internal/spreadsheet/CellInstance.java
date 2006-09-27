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
import java.text.NumberFormat;

import sej.SpreadsheetException;
import sej.describable.DescriptionBuilder;
import sej.internal.expressions.ExpressionNode;


public abstract class CellInstance extends AbstractStyledElement
{
	private final RowImpl row;
	private final int columnIndex;
	private NumberFormat numberFormat;


	public CellInstance(RowImpl _row)
	{
		this.row = _row;
		this.columnIndex = _row.getCellList().size();
		_row.getCellList().add( this );
	}


	public RowImpl getRow()
	{
		return this.row;
	}


	public int getColumnIndex()
	{
		return this.columnIndex;
	}


	public NumberFormat getNumberFormat()
	{
		return this.numberFormat;
	}


	public void setNumberFormat( NumberFormat _numberFormat )
	{
		this.numberFormat = _numberFormat;
	}


	public abstract ExpressionNode getExpression() throws SpreadsheetException;


	public abstract Object getValue();


	public String getCanonicalName()
	{
		return SheetImpl.getCanonicalNameForCellIndex( getColumnIndex(), getRow().getRowIndex() );
	}


	public CellIndex getCellIndex()
	{
		int iCol = getColumnIndex();
		int iRow = this.row.getRowIndex();
		SheetImpl sheet = this.row.getSheet();
		int iSheet = sheet.getSheetIndex();
		return new CellIndex( sheet.getSpreadsheet(), iSheet, iCol, iRow );
	}
	
	
	@Override
	public void describeTo( DescriptionBuilder _to ) throws IOException
	{
		_to.append( "<cell id=\"" );
		_to.append( getCanonicalName() );
		_to.append( "\">" );
		_to.newLine();
		_to.indent();
		describeDefinitionTo( _to );
		_to.outdent();
		_to.appendLine( "</cell>" );
	}


	protected abstract void describeDefinitionTo( DescriptionBuilder _to ) throws IOException;


}
