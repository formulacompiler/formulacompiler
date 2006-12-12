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
import sej.internal.spreadsheet.CellInstance;
import sej.internal.spreadsheet.RowImpl;
import sej.internal.spreadsheet.SpreadsheetImpl;

public abstract class AbstractReferenceTest extends AbstractSheetBasedTest
{
	private static final int STARTING_ROW = 1;
	private static final int EXPECTED_COL = 0;
	private static final int FORMULA_COL = 1;
	private static final int INPUTS_COL = 2;
	private static final int INPUTCOUNT_COL = 9;
	private static final int NAME_COL = 10;
	private static final int HIGHLIGHT_COL = 11;
	private static final int EXCELSAYS_COL = 12;
	private static final int SKIPFOR_COL = 13;


	protected AbstractReferenceTest()
	{
		super();
	}

	protected AbstractReferenceTest(String _baseName, int _startingRowNumber)
	{
		super( _baseName, _startingRowNumber );
	}

	protected AbstractReferenceTest(String _baseName, int _onlyRowNumbered, NumType _onlyType, int _onlyInputVariant,
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

		public SheetRunner(SpreadsheetImpl _book)
		{
			super( _book );
		}

		@Override
		protected int expectedCol()
		{
			return EXPECTED_COL;
		}

		@Override
		protected int formulaCol()
		{
			return FORMULA_COL;
		}

		@Override
		protected int firstInputCol()
		{
			return INPUTS_COL;
		}

		@Override
		protected int nameCol()
		{
			return NAME_COL;
		}

		@Override
		protected int highlightCol()
		{
			return HIGHLIGHT_COL;
		}

		@Override
		protected int excelSaysCol()
		{
			return EXCELSAYS_COL;
		}

		@Override
		protected int skipForCol()
		{
			return SKIPFOR_COL;
		}

		@Override
		protected int getDefaultStartingRow()
		{
			return STARTING_ROW;
		}

		@Override
		protected int inputColumnCountFor( RowImpl _row )
		{
			int result = INPUTCOUNT_COL - INPUTS_COL;
			int iCol = INPUTCOUNT_COL - 1;
			while (iCol >= INPUTS_COL) {
				if (null != _row.getCellOrNull( iCol-- )) break;
				result--;
			}
			return 2 + result;
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
				final CellInstance inputCountCell = _valueRow.getCellOrNull( INPUTCOUNT_COL );
				if (null != inputCountCell) {
					final int inputCount = ((Number) valueOf( inputCountCell )).intValue();
					sizeInputs( inputCount );
					for (int i = 0; i < inputCount; i++) {
						extractInputFrom( _valueRow.getCellIndex( INPUTS_COL + i ), i );
					}
				}
			}

			@Override
			protected CellInstance getValueCell( RowImpl _valueRow, int _iInput )
			{
				return _valueRow.getCellOrNull( _iInput + INPUTS_COL );
			}

		}

	}

}
