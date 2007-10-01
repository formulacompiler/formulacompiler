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
package org.formulacompiler.tests.reference;

import java.util.Collection;
import java.util.Iterator;

import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.runtime.New;
import org.formulacompiler.spreadsheet.internal.CellIndex;
import org.formulacompiler.spreadsheet.internal.CellInstance;
import org.formulacompiler.spreadsheet.internal.CellRange;
import org.formulacompiler.spreadsheet.internal.CellWithConstant;
import org.formulacompiler.spreadsheet.internal.RowImpl;
import org.formulacompiler.spreadsheet.internal.SpreadsheetImpl;



public abstract class AbstractDatabaseTableTest extends AbstractSheetBasedTest
{

	protected AbstractDatabaseTableTest()
	{
		super();
	}

	protected AbstractDatabaseTableTest(String _baseName, int _startingRowNumber)
	{
		super( _baseName, _startingRowNumber );
	}

	protected AbstractDatabaseTableTest(String _baseName, int _onlyRowNumbered, NumType _onlyType,
			int _onlyInputVariant, boolean _caching)
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
			private static final int TABLE_NAME_OFFS = 1;
			private static final int CRIT_NAME_OFFS = 2;
			private int nRowInputs = 0;

			public RowRunner(RowImpl _formulaRow, RowImpl _valueRow, int _rowNumber, SaveableEngine[] _engines)
			{
				super( _formulaRow, _valueRow, _rowNumber, _engines );
			}

			
			@Override
			protected void extractInputsFrom( RowImpl _valueRow )
			{
				int iInput = 0;

				final Collection<CellIndex> adds = New.collection();
				extractInputsFromNamedRange( _valueRow.getCellOrNull( firstInputCol() + TABLE_NAME_OFFS ), adds );
				extractInputsFromNamedRange( _valueRow.getCellOrNull( firstInputCol() + CRIT_NAME_OFFS ), adds );

				final CellIndex colRefCell = _valueRow.getCellIndex( firstInputCol() );
				if (null != colRefCell.getCell()) {
					sizeInputs( 1 + adds.size() );
					extractInputFrom( colRefCell, iInput++ );
					this.nRowInputs = 1;
				}
				else {
					sizeInputs( adds.size() );
				}

				for (CellIndex add : adds) {
					extractInputFrom( add, iInput++ );
				}
			}

			private void extractInputsFromNamedRange( CellInstance _rangeNameCell, Collection<CellIndex> _cells )
			{
				if (_rangeNameCell instanceof CellWithConstant) {
					final CellWithConstant constCell = (CellWithConstant) _rangeNameCell;
					final Object constVal = constCell.getValue();
					if (constVal != null) {
						final String rangeName = constVal.toString();
						final SpreadsheetImpl sheet = _rangeNameCell.getRow().getSheet().getSpreadsheet();
						final CellRange range = (CellRange) sheet.getRange( rangeName );
						final Iterator<CellIndex> cells = range.iterator();
						while (cells.hasNext()) {
							_cells.add( cells.next() );
						}
					}
				}
			}


			@Override
			protected CellInstance getValueCell( RowImpl _valueRow, int _iInput )
			{
				if (_iInput < this.nRowInputs) {
					return _valueRow.getCellOrNull( _iInput + firstInputCol() );
				}
				return this.inputCells[ _iInput ].getCell();
			}


		}

	}

}
