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
package sej.internal.spreadsheet.compiler;

import java.util.Map;
import java.util.Map.Entry;

import sej.compiler.CompilerException;
import sej.compiler.NumericType;
import sej.internal.model.CellModel;
import sej.internal.model.ComputationModel;
import sej.internal.model.SectionModel;
import sej.internal.spreadsheet.CellIndex;
import sej.internal.spreadsheet.binding.InputCellBinding;
import sej.internal.spreadsheet.binding.OutputCellBinding;
import sej.internal.spreadsheet.binding.SectionBinding;
import sej.internal.spreadsheet.binding.WorkbookBinding;
import sej.runtime.New;
import sej.spreadsheet.SpreadsheetBinding;


public final class SpreadsheetToModelCompiler
{
	private final WorkbookBinding binding;
	private final NumericType numericType;
	private final Map<CellIndex, CellModel> cellModels = New.newMap();
	private final Map<SectionBinding, SectionModelCompiler> sectionCompilers = New.newMap();
	private final Map<SectionModel, SectionModelCompiler> sectionCompilersByModel = New.newMap();
	private ComputationModel computationModel;


	public SpreadsheetToModelCompiler(SpreadsheetBinding _binding, NumericType _numericType)
	{
		super();

		assert _binding != null : "Binding must not be null";
		assert _binding instanceof WorkbookBinding : "Binding must be a WorkbookBinding";

		this.binding = (WorkbookBinding) _binding;
		this.numericType = _numericType;
	}


	public ComputationModel compile() throws CompilerException
	{
		buildNewModel();
		return this.computationModel;
	}


	public WorkbookBinding getEngineDef()
	{
		return this.binding;
	}
	
	
	public NumericType numericType()
	{
		return this.numericType;
	}


	ComputationModel buildNewModel() throws CompilerException
	{
		assert null == this.computationModel;

		SectionBinding rootDef = this.binding.getRoot();
		this.computationModel = new ComputationModel( rootDef.getInputClass(), rootDef.getOutputClass() );
		new SectionModelCompiler( this, null, rootDef, this.computationModel.getRoot() );

		buildModel();

		return this.computationModel;
	}


	private void buildModel() throws CompilerException
	{
		for (OutputCellBinding outputDef : getEngineDef().getOutputs()) {
			CellModel model = getOrCreateCellModel( outputDef.getIndex() );
			model.makeOutput( outputDef.getCallToImplement() );
		}
		for (Entry<CellIndex, InputCellBinding> inputEntry : getEngineDef().getInputs().entrySet()) {
			InputCellBinding inputDef = inputEntry.getValue();
			CellModel model = getCellModel( inputDef.getIndex() );
			if (null != model) {
				model.makeInput( inputDef.getCallChainToCall() );
			}
		}
	}


	CellModel getCellModel( CellIndex _index )
	{
		return this.cellModels.get( _index );
	}


	void addCellModel( CellIndex _index, CellModel _cell )
	{
		this.cellModels.put( _index, _cell );
	}


	private CellModel getOrCreateCellModel( CellIndex _index ) throws CompilerException
	{
		CellModel result = getCellModel( _index );
		if (null == result) {
			result = createCellModel( _index );
		}
		return result;
	}


	private CellModel createCellModel( CellIndex _index ) throws CompilerException
	{
		SectionBinding sectionDef = getEngineDef().getSectionFor( _index );
		SectionModelCompiler sectionCompiler = getOrCreateSectionCompiler( sectionDef );
		CellModel result = sectionCompiler.createCellModel( _index );
		return result;
	}


	private SectionModelCompiler getOrCreateSectionCompiler( SectionBinding _sectionDef )
	{
		SectionModelCompiler result = getSectionCompiler( _sectionDef );
		if (null == result) {
			result = createSectionCompiler( _sectionDef );
		}
		return result;
	}


	SectionModelCompiler getSectionCompiler( SectionBinding _sectionDef )
	{
		return this.sectionCompilers.get( _sectionDef );
	}


	SectionModelCompiler getSectionCompiler( SectionModel _sectionModel )
	{
		return this.sectionCompilersByModel.get( _sectionModel );
	}


	void addSectionModelCompiler( SectionModelCompiler _compiler )
	{
		this.sectionCompilers.put( _compiler.getSectionDef(), _compiler );
		this.sectionCompilersByModel.put( _compiler.getSectionModel(), _compiler );
	}


	private SectionModelCompiler createSectionCompiler( SectionBinding _sectionDef )
	{
		SectionModelCompiler parentCompiler = getOrCreateSectionCompiler( _sectionDef.getSection() );
		return parentCompiler.createSectionCompiler( _sectionDef );
	}


}
