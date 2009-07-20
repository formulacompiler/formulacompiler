/*
 * Copyright (c) 2006, 2008 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * This file is part of the Abacus Formula Compiler (AFC).
 *
 * For commercial licensing, please contact sales(at)formulacompiler.com.
 *
 * AFC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AFC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AFC.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.formulacompiler.examples.interactive.controller;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.formulacompiler.runtime.ComputationFactory;
import org.formulacompiler.runtime.FormulaCompilerException;
import org.formulacompiler.runtime.New;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.Spreadsheet.Cell;
import org.formulacompiler.spreadsheet.SpreadsheetBinder.Section;


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
			getSpreadsheetModel().setSpreadsheet( SpreadsheetCompiler.loadSpreadsheet( _fileName ) );
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void computeNow() throws NoSuchMethodException, FormulaCompilerException
	{
		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.setSpreadsheet( getSpreadsheetModel().getSpreadsheet() );
		builder.setInputClass( Inputs.class );
		builder.setOutputClass( Outputs.class );
		Section root = builder.getRootBinder();

		int iCell;

		final Method inputMethod = Inputs.class.getMethod( "getCellValue", Integer.TYPE );
		iCell = 0;
		for (CellListEntry e : getInputsModel().getCells()) {
			root.defineInputCell( e.index, inputMethod, iCell++ );
		}

		final Method outputMethod = Outputs.class.getMethod( "getCellValue", Integer.TYPE );
		iCell = 0;
		for (CellListEntry e : getOutputsModel().getCells()) {
			root.defineOutputCell( e.index, outputMethod, iCell++ );
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
		private final List<ControllerListener> listeners = New.list();


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


		public CellListEntry( Spreadsheet.Cell _index, Double _value )
		{
			this.index = _index;
			this.value = _value;
		}

	}


	public static final class Inputs
	{
		private final CellListModel inputModel;

		public Inputs( CellListModel _inputModel )
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
