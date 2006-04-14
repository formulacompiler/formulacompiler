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
package sej.engine.compiler.model.compiler;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import sej.ModelError;
import sej.NumericType;
import sej.engine.compiler.definition.EngineDefinition;
import sej.engine.compiler.definition.InputCellDefinition;
import sej.engine.compiler.definition.SectionDefinition;
import sej.engine.compiler.definition.OutputCellDefinition;
import sej.engine.compiler.model.CellModel;
import sej.engine.compiler.model.EngineModel;
import sej.engine.compiler.model.SectionModel;
import sej.engine.compiler.model.optimizer.ConstantSubExpressionEliminator;
import sej.engine.compiler.model.optimizer.IntermediateResultsInliner;
import sej.model.CellIndex;


public class EngineModelCompiler
{
	private final EngineDefinition engineDef;
	private final NumericType numericType;
	private final Map<CellIndex, CellModel> cellModels = new HashMap<CellIndex, CellModel>();
	private final Map<SectionDefinition, SectionModelCompiler> sectionCompilers = new HashMap<SectionDefinition, SectionModelCompiler>();
	private final Map<SectionModel, SectionModelCompiler> sectionCompilersByModel = new HashMap<SectionModel, SectionModelCompiler>();
	private EngineModel engineModel;


	public EngineModelCompiler(EngineDefinition _definition, NumericType _numericType)
	{
		super();
		this.engineDef = _definition;
		this.numericType = _numericType;
	}


	public EngineDefinition getEngineDef()
	{
		return this.engineDef;
	}


	private NumericType getNumericType()
	{
		return this.numericType;
	}


	public EngineModel compileNewModel() throws ModelError
	{
		buildNewModel();
		eliminateConstantSubExpressions();
		inlineIntermediateResults();
		return this.engineModel;
	}


	EngineModel buildNewModel() throws ModelError
	{
		assert null == this.engineModel;

		SectionDefinition rootDef = (SectionDefinition) this.engineDef.getRoot();
		this.engineModel = new EngineModel();
		new SectionModelCompiler( this, null, rootDef, this.engineModel.getRoot() );

		buildModel();

		return this.engineModel;
	}


	private void buildModel() throws ModelError
	{
		for (OutputCellDefinition outputDef : getEngineDef().getOutputs()) {
			CellModel model = getOrCreateCellModel( outputDef.getIndex() );
			model.makeOutput( outputDef.getCallToImplement() );
		}
		for (Entry<CellIndex, InputCellDefinition> inputEntry : getEngineDef().getInputs().entrySet()) {
			InputCellDefinition inputDef = inputEntry.getValue();
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


	private CellModel getOrCreateCellModel( CellIndex _index ) throws ModelError
	{
		CellModel result = getCellModel( _index );
		if (null == result) {
			result = createCellModel( _index );
		}
		return result;
	}


	private CellModel createCellModel( CellIndex _index ) throws ModelError
	{
		SectionDefinition sectionDef = getEngineDef().getSectionFor( _index );
		SectionModelCompiler sectionCompiler = getOrCreateSectionCompiler( sectionDef );
		CellModel result = sectionCompiler.createCellModel( _index );
		return result;
	}


	private SectionModelCompiler getOrCreateSectionCompiler( SectionDefinition _sectionDef )
	{
		SectionModelCompiler result = getSectionCompiler( _sectionDef );
		if (null == result) {
			result = createSectionCompiler( _sectionDef );
		}
		return result;
	}


	SectionModelCompiler getSectionCompiler( SectionDefinition _sectionDef )
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


	private SectionModelCompiler createSectionCompiler( SectionDefinition _sectionDef )
	{
		SectionModelCompiler parentCompiler = getOrCreateSectionCompiler( _sectionDef.getSection() );
		return parentCompiler.createSectionCompiler( _sectionDef );
	}


	private void eliminateConstantSubExpressions() throws ModelError
	{
		this.engineModel.traverse( new ConstantSubExpressionEliminator( getNumericType() ) );
	}


	private void inlineIntermediateResults() throws ModelError
	{
		this.engineModel.traverse( new IntermediateResultsInliner() );
	}


}
