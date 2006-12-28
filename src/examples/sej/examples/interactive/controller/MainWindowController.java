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
import sej.EngineBuilder;
import sej.SEJ;
import sej.Spreadsheet;
import sej.SpreadsheetException;
import sej.Spreadsheet.Cell;
import sej.SpreadsheetBinder.Section;
import sej.runtime.ComputationFactory;
import sej.runtime.SEJException;
import sej.util.New;


public class MainWindowController
{
	private final SpreadsheetModel spreadsheetModel = new SpreadsheetModel();
	private final CellListModel inputsModel = new CellListModel();
	private final CellListModel outputsModel = new CellListModel();


	public SpreadsheetModel getSpreadsheetModel()
	{
		return this.spreadsheetModel;
	}


	public CellListModel getInputsModel()
	{
		return this.inputsModel;
	}


	public CellListModel getOutputsModel()
	{
		return this.outputsModel;
	}


	public void loadSpreadsheetFrom( String _fileName ) throws SpreadsheetException
	{
		getInputsModel().clear();
		getOutputsModel().clear();
		try {
			getSpreadsheetModel().setSpreadsheet( SEJ.loadSpreadsheet( _fileName ) );
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void computeNow() throws NoSuchMethodException, SEJException
	{
		EngineBuilder builder = SEJ.newEngineBuilder();
		builder.setSpreadsheet( getSpreadsheetModel().getSpreadsheet() );
		builder.setInputClass( Inputs.class );
		builder.setOutputClass( Outputs.class );
		Section root = builder.getRootBinder();

		int iCell;

		final Method inputMethod = Inputs.class.getMethod( "getCellValue", Integer.TYPE );
		iCell = 0;
		for (CellListEntry e : getInputsModel().getCells()) {
			root.defineInputCell( e.index, new CallFrame( inputMethod, iCell++ ) );
		}

		final Method outputMethod = Outputs.class.getMethod( "getCellValue", Integer.TYPE );
		iCell = 0;
		for (CellListEntry e : getOutputsModel().getCells()) {
			root.defineOutputCell( e.index, new CallFrame( outputMethod, iCell++ ) );
		}

		ComputationFactory factory = builder.compile().getComputationFactory();
		Outputs o = (Outputs) factory.newComputation( new Inputs( this.inputsModel ) );

		iCell = 0;
		for (CellListEntry e : getOutputsModel().getCells()) {
			e.value = o.getCellValue( iCell++ );
		}

		getOutputsModel().dataChanged();
	}


	public abstract class DataModel
	{
		private final List<ControllerListener> listeners = New.newList();


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
		private Spreadsheet spreadsheet;


		public Spreadsheet getSpreadsheet()
		{
			return this.spreadsheet;
		}


		public void setSpreadsheet( Spreadsheet _spreadsheet )
		{
			this.spreadsheet = _spreadsheet;
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


		public void add( Spreadsheet.Cell _index )
		{
			this.cells.add( new CellListEntry( _index, null ) );
			dataChanged();
		}


		public void add( int _row, int _col )
		{
			Cell cell = getSpreadsheetModel().getSpreadsheet().getSheets()[ 0 ].getRows()[ _row ].getCells()[ _col ];
			add( cell );
		}

		public void remove( int _row )
		{
			this.cells.remove( _row );
			dataChanged();
		}


	}


	public class CellListEntry
	{
		public Spreadsheet.Cell index;
		public Double value;


		public CellListEntry(Spreadsheet.Cell _index, Double _value)
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


	public static abstract class Outputs
	{
		public double getCellValue( int _cellIndex )
		{
			return 0;
		}
	}

}
