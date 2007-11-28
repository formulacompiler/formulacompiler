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
package org.formulacompiler.tests.reference.base;

import java.io.File;

import org.formulacompiler.compiler.FormulaCompiler;
import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.runtime.Computation;
import org.formulacompiler.runtime.ComputationFactory;
import org.formulacompiler.runtime.Computation.Config;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.Spreadsheet.Cell;

import junit.framework.Test;

public final class Context
{
	protected static final File SHEET_PATH = new File( "src/test-reference/data/org/formulacompiler/tests/reference/" );

	private final Context parent;
	private String spreadsheetFileBaseName;
	private File spreadsheetFile;
	private Spreadsheet spreadsheet;
	private Spreadsheet.Sheet[] sheets;
	private Spreadsheet.Sheet sheet;
	private Spreadsheet.Row[] sheetRows;
	private Spreadsheet.Row row;
	private Spreadsheet.Cell[] rowCells;
	private Spreadsheet.Cell expectedCell;
	private Inputs expected;
	private Spreadsheet.Cell outputCell;
	private Spreadsheet.Cell[] inputCells;
	private Inputs inputs;
	private boolean[] inputIsBound;
	private SaveableEngine engine;
	private Computation.Config computationConfig;
	private ComputationFactory factory;
	private BindingType numberBindingType;
	private Boolean explicitCaching;
	private RowSetup.Builder rowSetupBuilder;
	private FailedEngineReporter failedEngineReporter;
	private Documenter documenter;


	public Context( Context _parent )
	{
		this.parent = _parent;
	}

	public Context( String _spreadsheetFileBaseName, String _extension )
	{
		this( (Context) null );
		this.spreadsheetFileBaseName = _spreadsheetFileBaseName;
		this.spreadsheetFile = new File( SHEET_PATH, _spreadsheetFileBaseName + _extension );
	}

	public Context( File _spreadsheetFile )
	{
		this( (Context) null );
		this.spreadsheetFile = _spreadsheetFile;
		this.spreadsheetFileBaseName = _spreadsheetFile.getName();
	}


	public File getSpreadsheetFile()
	{
		return this.spreadsheetFile != null? this.spreadsheetFile : this.parent == null? null : this.parent
				.getSpreadsheetFile();
	}

	public String getSpreadsheetFileBaseName()
	{
		return this.spreadsheetFileBaseName != null? this.spreadsheetFileBaseName : this.parent == null? null
				: this.parent.getSpreadsheetFileBaseName();
	}


	public Spreadsheet getSpreadsheet()
	{
		return this.spreadsheet != null? this.spreadsheet : this.parent == null? null : this.parent.getSpreadsheet();
	}

	public void setSpreadsheet( Spreadsheet _spreadsheet )
	{
		this.spreadsheet = _spreadsheet;
		this.sheets = _spreadsheet == null? null : _spreadsheet.getSheets();
	}

	public Spreadsheet.Sheet[] getSheets()
	{
		return this.spreadsheet != null? this.sheets : this.parent == null? null : this.parent.getSheets();
	}

	public Spreadsheet.Sheet getSheet( int _sheetIndex )
	{
		final Spreadsheet.Sheet[] sheets = getSheets();
		if (_sheetIndex < 0 && _sheetIndex >= sheets.length) return null;
		return sheets[ _sheetIndex ];
	}


	public Spreadsheet.Sheet getSheet()
	{
		return this.sheet != null? this.sheet : this.parent == null? null : this.parent.getSheet();
	}

	public int getSheetIndex()
	{
		return getSheet().getSheetIndex();
	}

	public void setSheet( int _sheetIndex )
	{
		setSheet( getSheet( _sheetIndex ) );
	}

	public void setSheet( Spreadsheet.Sheet _sheet )
	{
		this.sheet = _sheet;
		this.sheetRows = _sheet == null? null : _sheet.getRows();
	}

	public Spreadsheet.Row[] getSheetRows()
	{
		return this.sheet != null? this.sheetRows : this.parent == null? null : this.parent.getSheetRows();
	}

	public Spreadsheet.Row getSheetRow( int _rowIndex )
	{
		final Spreadsheet.Row[] cells = getSheetRows();
		if (_rowIndex < 0 && _rowIndex >= cells.length) return null;
		return cells[ _rowIndex ];
	}


	public Spreadsheet.Row getRow()
	{
		return this.row != null? this.row : this.parent == null? null : this.parent.getRow();
	}

	public int getRowIndex()
	{
		return getRow().getRowIndex();
	}

	public void setRow( int _rowIndex )
	{
		setRow( getSheetRow( _rowIndex ) );
	}

	public void setRow( Spreadsheet.Row _row )
	{
		this.row = _row;
		this.rowCells = _row == null? null : _row.getCells();
	}

	public Spreadsheet.Cell[] getRowCells()
	{
		final Spreadsheet.Cell[] cells = this.row != null? this.rowCells : this.parent == null? null : this.parent
				.getRowCells();
		return (cells == null)? NO_CELLS : cells;
	}

	private static final Cell[] NO_CELLS = new Spreadsheet.Cell[ 0 ];

	public Spreadsheet.Cell getRowCell( int _columnIndex )
	{
		final Spreadsheet.Cell[] cells = getRowCells();
		if (_columnIndex < 0 || _columnIndex >= cells.length) return null;
		return cells[ _columnIndex ];
	}


	public Spreadsheet.Cell getExpectedCell()
	{
		return this.expectedCell != null? this.expectedCell : this.parent == null? null : this.parent.getExpectedCell();
	}

	public void setExpectedCell( Spreadsheet.Cell _cell )
	{
		this.expectedCell = _cell;
	}


	public Inputs getExpected()
	{
		return this.expected != null? this.expected : this.parent == null? null : this.parent.getExpected();
	}

	public void setExpected( Inputs _cell )
	{
		this.expected = _cell;
	}


	public Spreadsheet.Cell getOutputCell()
	{
		return this.outputCell != null? this.outputCell : this.parent == null? null : this.parent.getOutputCell();
	}

	public void setOutputCell( Spreadsheet.Cell _cell )
	{
		this.outputCell = _cell;
	}

	public String getOutputExpr()
	{
		try {
			return getOutputCell().getExpressionText();
		}
		catch (SpreadsheetException e) {
			throw new RuntimeException( e );
		}
	}


	public Spreadsheet.Cell[] getInputCells()
	{
		return this.inputCells != null? this.inputCells : this.parent == null? null : this.parent.getInputCells();
	}

	public void setInputCells( Spreadsheet.Cell... _cells )
	{
		assert null == _cells || null == getInputIsBound() || _cells.length == getInputIsBound().length;
		this.inputCells = _cells;
	}


	public Inputs getInputs()
	{
		return this.inputs != null? this.inputs : this.parent == null? null : this.parent.getInputs();
	}

	public void setInputs( Inputs _cell )
	{
		this.inputs = _cell;
	}


	public boolean[] getInputIsBound()
	{
		return this.inputIsBound != null? this.inputIsBound : this.parent == null? null : this.parent.getInputIsBound();
	}

	public void setInputIsBound( boolean... _value )
	{
		assert null == _value || _value.length == getInputCells().length;
		this.inputIsBound = _value;
	}

	public void setInputIsBound( int _bitset )
	{
		final int n = getInputCells().length;
		final boolean[] flags = new boolean[ n ];
		for (int i = 0; i < n; i++) {
			flags[ i ] = (_bitset & (1 << i)) != 0;
		}
		setInputIsBound( flags );
	}

	public void setInputIsBound( String _bitstring )
	{
		setInputIsBound( Integer.parseInt( _bitstring, 2 ) );
	}


	public Computation.Config getComputationConfig()
	{
		return this.computationConfig != null? this.computationConfig : this.parent == null? null : this.parent
				.getComputationConfig();
	}

	public void setComputationConfig( Computation.Config _value )
	{
		this.computationConfig = _value;
	}


	public Boolean getExplicitCaching()
	{
		return this.explicitCaching != null? this.explicitCaching : this.parent == null? false : this.parent
				.getExplicitCaching();
	}

	public void setExplicitCaching( Boolean _value )
	{
		this.explicitCaching = _value;
	}


	public BindingType getNumberBindingType()
	{
		return this.numberBindingType != null? this.numberBindingType : this.parent == null? BindingType.DOUBLE
				: this.parent.getNumberBindingType();
	}

	public void setNumberBindingType( BindingType _value )
	{
		this.numberBindingType = _value;
	}

	public NumericType getNumericType()
	{
		switch (getNumberBindingType()) {
			case DOUBLE:
				return FormulaCompiler.DOUBLE;
			case BIGDEC_PREC:
				return FormulaCompiler.BIGDECIMAL128;
			case BIGDEC_SCALE:
				return FormulaCompiler.BIGDECIMAL_SCALE8;
			case LONG:
				return FormulaCompiler.LONG_SCALE6;
		}
		throw new IllegalArgumentException( "Invalid number binding type." );
	}


	public SaveableEngine getEngine() throws Exception
	{
		return this.engine != null? this.engine : this.parent == null? null : this.parent.getEngine();
	}

	public void setEngine( SaveableEngine _value )
	{
		this.engine = _value;
	}


	public ComputationFactory getFactory() throws Exception
	{
		return this.factory != null? this.factory : this.parent == null? null : this.parent.getFactory();
	}

	public void setFactory( ComputationFactory _value )
	{
		this.factory = _value;
	}


	public RowSetup.Builder getRowSetupBuilder()
	{
		return this.rowSetupBuilder != null? this.rowSetupBuilder : this.parent == null? null : this.parent
				.getRowSetupBuilder();
	}

	public void setRowSetupBuilder( RowSetup.Builder _value )
	{
		this.rowSetupBuilder = _value;
	}

	public RowSetup getRowSetup()
	{
		return getRowSetupBuilder().newInstance( this );
	}


	public Documenter getDocumenter()
	{
		return this.documenter != null? this.documenter : this.parent == null? Documenter.Mock.INSTANCE : this.parent
				.getDocumenter();
	}

	public void setDocumenter( Documenter _value )
	{
		this.documenter = _value;
	}


	public FailedEngineReporter getFailedEngineReporter()
	{
		return this.failedEngineReporter != null? this.failedEngineReporter : this.parent == null? null : this.parent
				.getFailedEngineReporter();
	}

	public void setFailedEngineReporter( FailedEngineReporter _value )
	{
		this.failedEngineReporter = _value;
	}

	void reportFailedEngineAndRethrow( Test _test, SaveableEngine _engine, Throwable _failure ) throws Throwable
	{
		try {
			final FailedEngineReporter reporter = getFailedEngineReporter();
			if (null != reporter) {
				reporter.reportFailedEngine( _test, _engine, _failure );
			}
		}
		catch (Throwable t) {
			System.err.println( "Error reporting failed engine:" );
			t.printStackTrace();
		}
		throw _failure;
	}

	protected static interface FailedEngineReporter
	{
		void reportFailedEngine( Test _test, SaveableEngine _engine, Throwable _failure ) throws Throwable;
	}

	public String getDescription()
	{
		StringBuilder s = new StringBuilder();
		s.append( getSpreadsheetFile().getName() );
		s.append( ", row " ).append( getRowIndex() );
		BindingType type = getNumberBindingType();
		if (type != null) s.append( ", type:" ).append( type.name() );
		Boolean caching = getExplicitCaching();
		if (caching != null) s.append( ", caching:" ).append( caching.toString() );
		Config config = getComputationConfig();
		if (config != null) s.append( ", config:" ).append( config.toString() );
		return s.toString();
	}

}
