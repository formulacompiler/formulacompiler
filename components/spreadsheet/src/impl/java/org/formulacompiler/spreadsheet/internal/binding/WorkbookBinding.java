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

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.runtime.New;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBinding;
import org.formulacompiler.spreadsheet.internal.CellIndex;
import org.formulacompiler.spreadsheet.internal.SpreadsheetImpl;

public class WorkbookBinding implements SpreadsheetBinding
{
	protected final SpreadsheetImpl workbook;
	protected final SectionBinding root;
	protected Map<CellIndex, InputCellBinding> inputs = New.newMap();
	protected List<OutputCellBinding> outputs = New.newList();
	protected List<SectionBinding> sections = New.newList();


	public WorkbookBinding(SpreadsheetImpl _workbook, Class _inputClass, Class _outputClass)
	{
		super();
		this.workbook = _workbook;
		this.root = new SectionBinding( this, _inputClass, _outputClass );
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


	public List<SectionBinding> getSections()
	{
		return this.sections;
	}


	public SectionBinding getSectionFor( CellIndex _index )
	{
		return this.root.getSectionFor( _index );
	}


	public void validate() throws CompilerException
	{
		this.root.validate();
	}

}
