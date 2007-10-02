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
package org.formulacompiler.spreadsheet.internal.binding;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.runtime.Computation;
import org.formulacompiler.runtime.New;
import org.formulacompiler.runtime.internal.Environment;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBinding;
import org.formulacompiler.spreadsheet.internal.CellIndex;
import org.formulacompiler.spreadsheet.internal.SpreadsheetImpl;

public class WorkbookBinding implements SpreadsheetBinding
{
	private final SpreadsheetImpl workbook;
	private final SectionBinding root;
	private final Environment environment;
	private final Map<CellIndex, InputCellBinding> inputs = New.map();
	private final List<OutputCellBinding> outputs = New.list();
	private final Set<CellIndex> outputCells = New.set();
	private final List<SectionBinding> sections = New.list();


	public WorkbookBinding(SpreadsheetImpl _workbook, Class _inputClass, Class _outputClass, Computation.Config _compileTimeConfig)
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

	public Environment getEnvironment()
	{
		return this.environment;
	}


	// ------------------------------------------------ Public API to other internal components


	public SectionBinding getRoot()
	{
		return this.root;
	}


	public SpreadsheetImpl getWorkbook()
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


	public List<SectionBinding> getSections()
	{
		return this.sections;
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

	public void add( SectionBinding _binding )
	{
		this.sections.add( _binding );
	}


	public void validate() throws CompilerException
	{
		this.root.validate();
	}

}
