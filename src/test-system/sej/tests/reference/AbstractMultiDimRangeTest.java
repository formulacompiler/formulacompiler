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
package sej.tests.reference;

import sej.SaveableEngine;
import sej.internal.spreadsheet.CellIndex;
import sej.internal.spreadsheet.CellInstance;
import sej.internal.spreadsheet.RowImpl;
import sej.internal.spreadsheet.SpreadsheetImpl;

public abstract class AbstractMultiDimRangeTest extends AbstractSheetBasedTest
{

	protected AbstractMultiDimRangeTest()
	{
		super();
	}

	protected AbstractMultiDimRangeTest(String _baseName, int _startingRowNumber)
	{
		super( _baseName, _startingRowNumber );
	}

	protected AbstractMultiDimRangeTest(String _baseName, int _onlyRowNumbered, NumType _onlyType, int _onlyInputVariant,
			boolean _caching)
	{
		super( _baseName, _onlyRowNumbered, _onlyType, _onlyInputVariant, _caching );
	}


	@Override
	protected AbstractSheetRunner newSheetRunner( SpreadsheetImpl _impl )
	{
		return new SheetRunner( _impl );
	}


	private final class SheetRunner extends AbstractSheetRunner
	{
		private final int defaultStartingRow;
		private final int leftMostTestColumn;


		public SheetRunner(SpreadsheetImpl _book)
		{
			super( _book );
			final CellIndex testStart = (CellIndex) _book.getRange( "TestHeader" ).getTopLeft();
			this.defaultStartingRow = testStart.rowIndex + 1;
			this.leftMostTestColumn = testStart.columnIndex;
		}

		@Override
		protected int expectedCol()
		{
			return this.leftMostTestColumn + 0;
		}

		@Override
		protected int formulaCol()
		{
			return this.leftMostTestColumn + 1;
		}

		@Override
		protected int firstInputCol()
		{
			return this.leftMostTestColumn + 2;
		}

		@Override
		protected int nameCol()
		{
			return this.leftMostTestColumn + 5;
		}

		@Override
		protected int highlightCol()
		{
			return this.leftMostTestColumn + 6;
		}

		@Override
		protected int excelSaysCol()
		{
			return this.leftMostTestColumn + 7;
		}

		@Override
		protected int skipForCol()
		{
			return this.leftMostTestColumn + 8;
		}

		@Override
		protected int getDefaultStartingRow()
		{
			return this.defaultStartingRow;
		}

		@Override
		protected int inputColumnCountFor( RowImpl _row )
		{
			return (null != _row.getCellOrNull( firstInputCol() )) ? 3 : 2;
		}

		@Override
		protected AbstractRowRunner newRowRunner( RowImpl _formulaRow, RowImpl _valueRow, int _rowNumber,
				SaveableEngine[] _engines )
		{
			return new RowRunner( _formulaRow, _valueRow, _rowNumber, _engines );
		}


		private final class RowRunner extends AbstractRowRunner
		{

			public RowRunner(RowImpl _formulaRow, RowImpl _valueRow, int _rowNumber, SaveableEngine[] _engines)
			{
				super( _formulaRow, _valueRow, _rowNumber, _engines );
			}

			@Override
			protected void extractInputsFrom( RowImpl _valueRow )
			{
				if (null != _valueRow.getCellOrNull( firstInputCol() )) {
					this.inputs = new Object[ 1 ];
					this.inputTypes = new ValueType[ 1 ];
					final CellInstance inputCell = _valueRow.getCellOrNull( firstInputCol() );
					this.inputs[ 0 ] = (null == inputCell) ? null : valueOf( inputCell );
					this.inputTypes[ 0 ] = valueTypeOf( this.inputs[ 0 ] );
				}
			}

		}

	}

}
