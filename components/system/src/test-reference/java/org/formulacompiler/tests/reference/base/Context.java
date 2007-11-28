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
import java.util.List;

import org.formulacompiler.compiler.FormulaCompiler;
import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.runtime.Computation;
import org.formulacompiler.runtime.ComputationFactory;
import org.formulacompiler.runtime.New;
import org.formulacompiler.runtime.Computation.Config;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.internal.CellIndex;
import org.formulacompiler.spreadsheet.internal.CellInstance;
import org.formulacompiler.spreadsheet.internal.CellWithLazilyParsedExpression;
import org.formulacompiler.spreadsheet.internal.RowImpl;
import org.formulacompiler.spreadsheet.internal.SheetImpl;
import org.formulacompiler.spreadsheet.internal.SpreadsheetImpl;

import junit.framework.Test;

public final class Context
{
	protected static final File SHEET_PATH = new File( "src/test-reference/data/org/formulacompiler/tests/reference/" );

	private final Context parent;
	private String spreadsheetFileBaseName;
	private File spreadsheetFile;
	private SpreadsheetImpl spreadsheet;
	private SheetImpl sheet;
	private RowImpl row;
	private CellInstance expectedCell;
	private Inputs expected;
	private CellInstance outputCell;
	private CellIndex[] inputCells;
	private Inputs inputs;
	private Integer inputBindingBits;
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


	public SpreadsheetImpl getSpreadsheet()
	{
		return this.spreadsheet != null? this.spreadsheet : this.parent == null? null : this.parent.getSpreadsheet();
	}

	public void setSpreadsheet( SpreadsheetImpl _spreadsheet )
	{
		this.spreadsheet = _spreadsheet;
		setSheet( 0 );
	}

	public List<SheetImpl> getSheets()
	{
		return getSpreadsheet().getSheetList();
	}

	public SheetImpl getSheet( int _sheetIndex )
	{
		List<SheetImpl> sheets = getSheets();
		if (_sheetIndex < 0 && _sheetIndex >= sheets.size()) return null;
		return sheets.get( _sheetIndex );
	}


	public SheetImpl getSheet()
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

	public void setSheet( SheetImpl _sheet )
	{
		this.sheet = _sheet;
	}

	public List<RowImpl> getSheetRows()
	{
		return getSheet().getRowList();
	}

	public RowImpl getSheetRow( int _rowIndex )
	{
		List<RowImpl> rows = getSheetRows();
		if (_rowIndex < 0 && _rowIndex >= rows.size()) return null;
		return rows.get( _rowIndex );
	}


	public RowImpl getRow()
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

	public void setRow( RowImpl _row )
	{
		this.row = _row;
	}

	public List<CellInstance> getRowCells()
	{
		RowImpl row = getRow();
		if (row == null) return NO_CELLS;
		return row.getCellList();
	}
	private static final List<CellInstance> NO_CELLS = New.list( 0 );

	public CellInstance getRowCell( int _columnIndex )
	{
		List<CellInstance> cells = getRowCells();
		if (_columnIndex < 0 || _columnIndex >= cells.size()) return null;
		return cells.get( _columnIndex );
	}


	public CellInstance getExpectedCell()
	{
		return this.expectedCell != null? this.expectedCell : this.parent == null? null : this.parent.getExpectedCell();
	}

	public void setExpectedCell( CellInstance _cell )
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


	public CellInstance getOutputCell()
	{
		return this.outputCell != null? this.outputCell : this.parent == null? null : this.parent.getOutputCell();
	}

	public void setOutputCell( CellInstance _cell )
	{
		this.outputCell = _cell;
	}

	public String getOutputExpr()
	{
		final CellInstance cell = getOutputCell();
		if (cell instanceof CellWithLazilyParsedExpression) {
			final CellWithLazilyParsedExpression exprCell = (CellWithLazilyParsedExpression) cell;
			try {
				return exprCell.getExpression().toString();
			}
			catch (SpreadsheetException e) {
				throw new RuntimeException( e );
			}
		}
		return null;
	}


	public CellIndex[] getInputCells()
	{
		return this.inputCells != null? this.inputCells : this.parent == null? null : this.parent.getInputCells();
	}

	public void setInputCells( CellIndex... _cells )
	{
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


	public Integer getInputBindingBits()
	{
		return this.inputBindingBits != null? this.inputBindingBits : this.parent == null? null : this.parent
				.getInputBindingBits();
	}

	public boolean[] getInputBindingFlags()
	{
		return decodeBinding( getInputBindingBits() );
	}

	private boolean[] decodeBinding( int _bitset )
	{
		final int n = getInputCells().length;
		final boolean[] flags = new boolean[ n ];
		for (int i = 0; i < n; i++) {
			flags[ i ] = (_bitset & (1 << i)) != 0;
		}
		return flags;
	}

	public void setInputBindingBits( int _value )
	{
		this.inputBindingBits = _value;
	}

	public void setInputBindingBits( String _bitstring )
	{
		setInputBindingBits( Integer.parseInt( _bitstring, 2 ) );
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
