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

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.internal.YamlBuilder;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.spreadsheet.SpreadsheetException;


public abstract class CellInstance extends AbstractStyledElement
{
	private final RowImpl row;
	private final int columnIndex;
	private int maxFractionalDigits = NumericType.UNLIMITED_FRACTIONAL_DIGITS;
	private Object value;


	public CellInstance( RowImpl _row )
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


	public int getMaxFractionalDigits()
	{
		return this.maxFractionalDigits;
	}

	public void setMaxFractionalDigits( int _maxFractionalDigits )
	{
		this.maxFractionalDigits = _maxFractionalDigits;
	}


	public void applyNumberFormat( NumberFormat _numberFormat )
	{
		setMaxFractionalDigits( getMaxFractionalDigitsFor( _numberFormat ) );
	}

	private int getMaxFractionalDigitsFor( NumberFormat _numberFormat )
	{
		if (null == _numberFormat) {
			return NumericType.UNLIMITED_FRACTIONAL_DIGITS;
		}
		else {
			int maxFrac = _numberFormat.getMaximumFractionDigits();
			if (_numberFormat instanceof DecimalFormat) {
				DecimalFormat decFormat = (DecimalFormat) _numberFormat;
				int decMult = decFormat.getMultiplier();
				switch (decMult) {
					case 10:
						maxFrac += 1;
						break;
					case 100:
						maxFrac += 2;
						break;
				}
			}
			return maxFrac;
		}
	}


	public abstract ExpressionNode getExpression() throws SpreadsheetException;


	public Object getValue()
	{
		return this.value;
	}

	public void setValue( Object _value )
	{
		this.value = _value;
	}


	public String getCanonicalName()
	{
		return SheetImpl.getNameA1ForCellIndex( getColumnIndex(), getRow().getRowIndex() );
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
	public void yamlTo( YamlBuilder _to )
	{
		_to.nv( "name", getRow().getSheet().getSpreadsheet().getModelNameFor( getCellIndex() ) );
	}

}
