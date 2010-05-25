/*
 * Copyright (c) 2006-2009 by Abacus Research AG, Switzerland.
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

package org.formulacompiler.spreadsheet.internal.binding;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.runtime.Computation;
import org.formulacompiler.runtime.ComputationMode;
import org.formulacompiler.runtime.New;
import org.formulacompiler.runtime.internal.Environment;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBinding;
import org.formulacompiler.spreadsheet.internal.BaseSpreadsheet;
import org.formulacompiler.spreadsheet.internal.CellIndex;

public class WorkbookBinding implements SpreadsheetBinding
{
	private final BaseSpreadsheet workbook;
	private final SectionBinding root;
	private final Environment environment;
	private final Map<CellIndex, InputCellBinding> inputs = New.map();
	private final List<OutputCellBinding> outputs = New.list();
	private final Set<CellIndex> outputCells = New.set();


	public WorkbookBinding( BaseSpreadsheet _workbook, Class _inputClass, Class _outputClass,
			Computation.Config _compileTimeConfig )
	{
		super();
		this.workbook = _workbook;
		this.root = new SectionBinding( this, _inputClass, _outputClass );
		this.environment = Environment.getInstance( _compileTimeConfig );
	}


	// ------------------------------------------------ Implementation of SpreadsheetBinding


	public Spreadsheet getSpreadsheet()
	{
		return getWorkbook();
	}


	public Class getInputClass()
	{
		return this.root.getInputClass();
	}

	public Class getOutputClass()
	{
		return this.root.getOutputClass();
	}

	public ComputationMode getComputationMode()
	{
		return this.workbook.getComputationMode();
	}

	public Environment getEnvironment()
	{
		return this.environment;
	}


	// ------------------------------------------------ Public API to other internal components


	public SectionBinding getRoot()
	{
		return this.root;
	}


	public BaseSpreadsheet getWorkbook()
	{
		return this.workbook;
	}


	public Map<CellIndex, InputCellBinding> getInputs()
	{
		return this.inputs;
	}


	public List<OutputCellBinding> getOutputs()
	{
		return this.outputs;
	}

	public Set<CellIndex> getOutputsCells()
	{
		return this.outputCells;
	}


	public SectionBinding getSectionFor( CellIndex _index )
	{
		return this.root.getSectionFor( _index );
	}


	public void add( InputCellBinding _binding )
	{
		this.inputs.put( _binding.getIndex(), _binding );
	}

	public void add( OutputCellBinding _binding )
	{
		this.outputs.add( _binding );
		this.outputCells.add( _binding.getIndex() );
	}


	public void validate() throws CompilerException
	{
		this.root.validate();
	}

}
