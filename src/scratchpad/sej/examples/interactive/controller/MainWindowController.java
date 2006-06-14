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
package sej.examples.interactive.controller;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import sej.CallFrame;
import sej.SpreadsheetBinder.Section;
import sej.internal.spreadsheet.CellIndex;
import sej.internal.spreadsheet.loader.SpreadsheetLoader;
import sej.runtime.Engine;

import jxl.Workbook;


public class MainWindowController
{
	private final SpreadsheetModel spreadsheet = new SpreadsheetModel();
	private final CellListModel inputs = new CellListModel();
	private final CellListModel outputs = new CellListModel();


	public SpreadsheetModel getSpreadsheet()
	{
		return this.spreadsheet;
	}


	public CellListModel getInputs()
	{
		return this.inputs;
	}


	public CellListModel getOutputs()
	{
		return this.outputs;
	}


	public void loadSpreadsheetFrom( String _fileName )
	{
		getInputs().clear();
		getOutputs().clear();
		try {
			getSpreadsheet().setWorkbook( (Workbook) SpreadsheetLoader.loadFromFile( _fileName ) );
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void computeNow() throws ModelError, NoSuchMethodException
	{
		Compiler compiler = CompilerFactory.newDefaultCompiler( getSpreadsheet().getWorkbook(), Inputs.class,
				Outputs.class );
		Section root = compiler.getRoot();

		int iCell;

		final Method inputMethod = Inputs.class.getMethod( "getCellValue", Integer.TYPE );
		iCell = 0;
		for (CellListEntry e : getInputs().getCells()) {
			root.defineInputCell( e.index, new CallFrame( inputMethod, iCell++ ) );
		}

		final Method outputMethod = Outputs.class.getMethod( "getCellValue", Integer.TYPE );
		iCell = 0;
		for (CellListEntry e : getOutputs().getCells()) {
			root.defineOutputCell( e.index, new CallFrame( outputMethod, iCell++ ) );
		}

		Engine engine = compiler.compileNewEngine();
		Outputs o = (Outputs) engine.newComputation( new Inputs( this.inputs ) );

		iCell = 0;
		for (CellListEntry e : getOutputs().getCells()) {
			e.value = o.getCellValue( iCell++ );
		}

		getOutputs().dataChanged();
	}


	public abstract class DataModel
	{
		private final List<ControllerListener> listeners = new ArrayList<ControllerListener>();


		public List<ControllerListener> getListeners()
		{
			return this.listeners;
		}


		protected void dataChanged()
		{
			for (ControllerListener l : this.listeners) {
				l.dataChanged();
			}
		}

	}


	public class SpreadsheetModel extends DataModel
	{
		private Workbook workbook;


		public Workbook getWorkbook()
		{
			return this.workbook;
		}


		public void setWorkbook( Workbook _workbook )
		{
			this.workbook = _workbook;
			dataChanged();
		}

	}


	public class CellListModel extends DataModel
	{
		private final List<CellListEntry> cells = new ArrayList<CellListEntry>();


		public List<CellListEntry> getCells()
		{
			return this.cells;
		}


		public void clear()
		{
			getCells().clear();
			dataChanged();
		}


		public void add( int _row, int _col )
		{
			add( new CellIndex( 0, _col, _row ) );
		}


		public void add( CellIndex _index )
		{
			this.cells.add( new CellListEntry( _index, null ) );
			dataChanged();
		}


		public void remove( int _row )
		{
			this.cells.remove( _row );
			dataChanged();
		}

	}


	public class CellListEntry
	{
		public CellIndex index;
		public Double value;


		public CellListEntry(CellIndex _index, Double _value)
		{
			this.index = _index;
			this.value = _value;
		}

	}


	public static final class Inputs
	{
		private final CellListModel inputModel;

		public Inputs(CellListModel _inputModel)
		{
			super();
			this.inputModel = _inputModel;
		}

		public double getCellValue( int _cellIndex )
		{
			final Double value = this.inputModel.getCells().get( _cellIndex ).value;
			return (null == value) ? 0 : value;
		}
	}


	public abstract class Outputs
	{
		public double getCellValue( int _cellIndex )
		{
			return 0;
		}
	}

}
