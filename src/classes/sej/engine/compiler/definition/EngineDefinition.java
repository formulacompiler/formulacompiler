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
package sej.engine.compiler.definition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sej.Compiler;
import sej.Spreadsheet;
import sej.model.CellIndex;
import sej.model.Workbook;

public class EngineDefinition
{
	protected final Workbook workbook;
	protected final SectionDefinition root = new SectionDefinition( this );
	protected Map<CellIndex, InputCellDefinition> inputs = new HashMap<CellIndex, InputCellDefinition>();
	protected List<OutputCellDefinition> outputs = new ArrayList<OutputCellDefinition>();
	protected List<SectionDefinition> sections = new ArrayList<SectionDefinition>();


	public EngineDefinition(Workbook _workbook)
	{
		super();
		this.workbook = _workbook;
	}


	public Workbook getWorkbook()
	{
		return this.workbook;
	}


	public Compiler.Section getRoot()
	{
		return this.root;
	}


	public Map<CellIndex, InputCellDefinition> getInputs()
	{
		return this.inputs;
	}


	public List<OutputCellDefinition> getOutputs()
	{
		return this.outputs;
	}


	public List<SectionDefinition> getSections()
	{
		return this.sections;
	}


	public Spreadsheet getModel()
	{
		return this.workbook;
	}


	public SectionDefinition getSectionFor( CellIndex _index )
	{
		return this.root.getSectionFor( _index );
	}

}
