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

package org.formulacompiler.spreadsheet.internal.compiler;

import java.util.Map;
import java.util.Map.Entry;

import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.FormulaCompiler;
import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.internal.Util;
import org.formulacompiler.compiler.internal.model.CellModel;
import org.formulacompiler.compiler.internal.model.ComputationModel;
import org.formulacompiler.compiler.internal.model.SectionModel;
import org.formulacompiler.runtime.ComputationMode;
import org.formulacompiler.runtime.New;
import org.formulacompiler.runtime.internal.Environment;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBinding;
import org.formulacompiler.spreadsheet.internal.CellIndex;
import org.formulacompiler.spreadsheet.internal.binding.InputCellBinding;
import org.formulacompiler.spreadsheet.internal.binding.OutputCellBinding;
import org.formulacompiler.spreadsheet.internal.binding.SectionBinding;
import org.formulacompiler.spreadsheet.internal.binding.WorkbookBinding;


public final class SpreadsheetToModelCompiler
{
	private final WorkbookBinding binding;
	private final NumericType numericType;
	private final boolean copyCellNames;
	private final Map<CellIndex, CellModel> cellModels = New.map();
	private final Map<SectionBinding, SectionModelCompiler> sectionCompilers = New.map();
	private final Map<SectionModel, SectionModelCompiler> sectionCompilersByModel = New.map();
	private final ComputationMode computationMode;
	private ComputationModel computationModel;


	public SpreadsheetToModelCompiler( SpreadsheetBinding _binding, NumericType _numericType )
	{
		this( _binding, _numericType, ComputationMode.EXCEL, false );
		Util.assertTesting();
	}

	public SpreadsheetToModelCompiler( SpreadsheetBinding _binding, NumericType _numericType,
			ComputationMode _computationMode, boolean _copyCellNames )
	{
		super();

		assert _binding != null : "Binding must not be null";
		assert _binding instanceof WorkbookBinding : "Binding must be a WorkbookBinding";

		this.binding = (WorkbookBinding) _binding;
		this.numericType = _numericType;
		this.computationMode = _computationMode;
		this.copyCellNames = _copyCellNames;
	}


	public ComputationModel compile() throws CompilerException
	{
		return buildNewModel();
	}


	public WorkbookBinding getEngineDef()
	{
		return this.binding;
	}


	public NumericType numericType()
	{
		return this.numericType;
	}


	private ComputationModel buildNewModel() throws CompilerException
	{
		assert null == this.computationModel;

		final SectionBinding rootDef = this.binding.getRoot();
		final Environment env = this.binding.getEnvironment();
		this.computationModel = new ComputationModel( rootDef.getInputClass(), rootDef.getOutputClass(), getComputationMode(), env );
		new SectionModelCompiler( this, null, rootDef, this.computationModel.getRoot() );

		buildModel();

		if (this.copyCellNames) {
			nameModel();
		}

		return this.computationModel;
	}


	private ComputationMode getComputationMode() {
		return this.computationMode != null ? this.computationMode : this.binding.getComputationMode();
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
				model.makeInput( mapDynamicParams( inputDef.getCallChainToCall() ) );
			}
		}
	}


	private CallFrame mapDynamicParams( CallFrame _frame ) throws CompilerException
	{
		if (null == _frame) return null;
		CallFrame prev = mapDynamicParams( _frame.getPrev() );
		final Object[] args = _frame.getArgs();
		boolean changed = false;
		if (null != args) {
			for (int i = 0; i < args.length; i++) {
				if (args[ i ] instanceof CellIndex) {
					CellIndex cell = (CellIndex) args[ i ];
					CellModel model = getOrCreateCellModel( cell );
					args[ i ] = model;
					changed = true;
				}
			}
		}
		if (null != prev) {
			return prev.chain( _frame.getMethod(), args );
		}
		else if (changed) {
			return FormulaCompiler.newCallFrame( _frame.getMethod(), args );
		}
		return _frame;
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


	private void nameModel()
	{
		final Spreadsheet ss = this.binding.getSpreadsheet();
		for (Entry<String, Spreadsheet.Range> nameDef : ss.getRangeNames().entrySet()) {
			final Spreadsheet.Range range = nameDef.getValue();
			if (range instanceof Spreadsheet.Cell) {
				final CellModel cellModel = getCellModel( (CellIndex) range );
				if (null != cellModel) {
					cellModel.setName( nameDef.getKey().toUpperCase() );
				}
			}
		}
	}

}
