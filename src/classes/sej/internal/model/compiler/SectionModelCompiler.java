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
package sej.internal.model.compiler;

import java.util.Collection;

import sej.api.CompilerError;
import sej.api.expressions.ExpressionNode;
import sej.api.expressions.ExpressionNodeForConstantValue;
import sej.internal.model.CellModel;
import sej.internal.model.ExpressionNodeForCellModel;
import sej.internal.model.ExpressionNodeForRangeValue;
import sej.internal.model.RangeValue;
import sej.internal.model.SectionModel;
import sej.internal.model.util.InterpretedNumericType;
import sej.internal.spreadsheet.CellIndex;
import sej.internal.spreadsheet.CellInstance;
import sej.internal.spreadsheet.CellRange;
import sej.internal.spreadsheet.CellWithConstant;
import sej.internal.spreadsheet.CellWithLazilyParsedExpression;
import sej.internal.spreadsheet.ExpressionNodeForCell;
import sej.internal.spreadsheet.ExpressionNodeForRange;
import sej.internal.spreadsheet.ExpressionNodeForRangeShape;
import sej.internal.spreadsheet.binding.CellBinding;
import sej.internal.spreadsheet.binding.InputCellBinding;
import sej.internal.spreadsheet.binding.SectionBinding;
import sej.internal.spreadsheet.binding.WorkbookBinding;


public class SectionModelCompiler
{
	private final ComputationModelCompiler compiler;
	private final WorkbookBinding engineDef;
	private final SectionModelCompiler section;
	private final SectionBinding sectionDef;
	private final SectionModel sectionModel;


	public SectionModelCompiler(ComputationModelCompiler _compiler, SectionModelCompiler _section,
			SectionBinding _sectionDef, SectionModel _model)
	{
		this.compiler = _compiler;
		this.engineDef = _compiler.getEngineDef();
		this.section = _section;
		this.sectionDef = _sectionDef;
		this.sectionModel = _model;
		_compiler.addSectionModelCompiler( this );
	}


	public SectionModelCompiler getSection()
	{
		return this.section;
	}


	public SectionBinding getSectionDef()
	{
		return this.sectionDef;
	}


	public SectionModel getSectionModel()
	{
		return this.sectionModel;
	}


	private InterpretedNumericType getNumericType()
	{
		return this.compiler.getNumericType();
	}


	public CellModel createCellModel( CellBinding _cellDef ) throws CompilerError
	{
		boolean isInput = _cellDef instanceof InputCellBinding;
		CellModel result = createCellModel( _cellDef.getIndex(), isInput );
		if (isInput) {
			result.makeInput( ((InputCellBinding) _cellDef).getCallChainToCall() );
		}
		return result;
	}


	public CellModel createCellModel( CellIndex _cellIndex, boolean _isInput ) throws CompilerError
	{
		final CellInstance cell = getCell( _cellIndex );
		final boolean nonNull = (null != cell);
		if (nonNull || _isInput) {
			final CellModel result = new CellModel( this.sectionModel, _cellIndex.toString() );
			this.compiler.addCellModel( _cellIndex, result );
			if (nonNull) {
				buildCellModel( cell, result );
			}
			return result;
		}
		else {
			return null;
		}
	}


	CellModel getOrCreateCellModel( CellIndex _cellIndex ) throws CompilerError
	{
		CellModel result = this.compiler.getCellModel( _cellIndex );
		if (null == result) {
			result = createCellModel( _cellIndex );
		}
		return result;
	}


	CellModel createCellModel( CellIndex _cellIndex ) throws CompilerError
	{
		InputCellBinding inputDef = this.engineDef.getInputs().get( _cellIndex );
		if (null != inputDef) {
			return createCellModel( inputDef );
		}
		else {
			return createCellModel( _cellIndex, false );
		}
	}


	SectionModelCompiler getOrCreateSectionCompiler( SectionBinding _sectionDef )
	{
		SectionModelCompiler result = this.compiler.getSectionCompiler( _sectionDef );
		if (null == result) {
			result = createSectionCompiler( _sectionDef );
		}
		return result;
	}


	SectionModelCompiler createSectionCompiler( SectionBinding _sectionDef )
	{
		SectionModel model = new SectionModel( getSectionModel(), _sectionDef.getCallChainToCall().toString(),
				this.sectionDef.getInputClass(), this.sectionDef.getOutputClass() );
		return new SectionModelCompiler( this.compiler, this, _sectionDef, model );
	}


	private void buildCellModel( CellInstance _cell, CellModel _cellModel ) throws CompilerError
	{
		if (null == _cell) throw new IllegalArgumentException();
		if (_cell instanceof CellWithConstant) buildCellModel( _cellModel, (CellWithConstant) _cell );
		else if (_cell instanceof CellWithLazilyParsedExpression)
			buildCellModel( _cellModel, (CellWithLazilyParsedExpression) _cell );
		_cellModel.applyNumberFormat( _cell.getNumberFormat() );
	}


	private void buildCellModel( CellModel _cellModel, CellWithConstant _cell )
	{
		_cellModel.setConstantValue( getNumericType().adjustConstantValue( _cell.getValue() ) );
	}


	private void buildCellModel( CellModel _cellModel, CellWithLazilyParsedExpression _cell ) throws CompilerError
	{
		ExpressionNode exprDef = _cell.getExpression();
		ExpressionNode exprModel = (ExpressionNode) buildExpressionModel( exprDef );
		_cellModel.setExpression( exprModel );
	}


	@SuppressWarnings("unchecked")
	private Object buildExpressionModel( ExpressionNode _exprDef ) throws CompilerError
	{
		if (null == _exprDef) {
			return null;
		}
		else if (_exprDef instanceof ExpressionNodeForConstantValue) {
			final ExpressionNodeForConstantValue cst = (ExpressionNodeForConstantValue) _exprDef;
			return new ExpressionNodeForConstantValue( getNumericType().adjustConstantValue( cst.getValue() ) );
		}
		else if (_exprDef instanceof ExpressionNodeForCell) {
			final CellIndex cell = ((ExpressionNodeForCell) _exprDef).getCellIndex();
			return buildExpressionModel( cell );
		}
		else if (_exprDef instanceof ExpressionNodeForRangeShape) {
			final CellRange range = ((ExpressionNodeForRange) _exprDef.getArguments().get( 0 )).getRange();
			return buildExpressionModel( range );
		}
		else if (_exprDef instanceof ExpressionNodeForRange) {
			final CellRange range = ((ExpressionNodeForRange) _exprDef).getRange();
			final ExpressionNode rangeNode = buildExpressionModel( range );
			if (rangeNode instanceof ExpressionNodeForRangeValue) {
				return rangeNode.getArguments();
			}
			else {
				return rangeNode;
			}
		}
		else {
			final ExpressionNode result = _exprDef.cloneWithoutArguments();
			for (ExpressionNode _arg : _exprDef.getArguments()) {
				final Object argOrArgs = buildExpressionModel( _arg );
				if (argOrArgs instanceof ExpressionNode) {
					final ExpressionNode arg = (ExpressionNode) argOrArgs;
					result.getArguments().add( arg );
				}
				else {
					final Collection<ExpressionNode> args = (Collection<ExpressionNode>) argOrArgs;
					result.getArguments().addAll( args );
				}
			}
			return result;
		}
	}


	ExpressionNode buildExpressionModel( CellIndex _cellIndex ) throws CompilerError
	{
		SectionPath sectionPath = getSectionPathFor( _cellIndex );
		if (null == sectionPath) {
			return buildExpressionModelForContainedCell( _cellIndex );
		}
		else {
			ExpressionNode cellNode = sectionPath.getSectionCompiler().buildExpressionModelForContainedCell( _cellIndex );
			ExpressionNode targetNode = sectionPath.getTargetNode();
			targetNode.getArguments().add( cellNode );
			return sectionPath.getRootNode();
		}
	}


	private ExpressionNode buildExpressionModelForContainedCell( CellIndex _cellIndex ) throws CompilerError
	{
		CellModel cellModel = getOrCreateCellModel( _cellIndex );
		return new ExpressionNodeForCellModel( cellModel );
	}


	private ExpressionNode buildExpressionModel( CellRange _range ) throws CompilerError
	{
		SectionPath sectionPath = getSectionPathFor( _range );
		if (null == sectionPath) {
			return buildExpressionModelForContainedRange( _range );
		}
		else {
			ExpressionNode rangeNode = sectionPath.getSectionCompiler().buildExpressionModelForContainedRange(
					sectionPath.getTargetRange() );
			ExpressionNode targetNode = sectionPath.getTargetNode();
			targetNode.getArguments().add( rangeNode );
			return sectionPath.getRootNode();
		}
	}


	private ExpressionNode buildExpressionModelForContainedRange( CellRange _range ) throws CompilerError
	{

		final int sheets = _range.getTo().sheetIndex - _range.getFrom().sheetIndex + 1;
		final int rows = _range.getTo().rowIndex - _range.getFrom().rowIndex + 1;
		final int cols = _range.getTo().columnIndex - _range.getFrom().columnIndex + 1;
		final RangeValue rangeValue = new RangeValue( sheets, rows, cols );
		ExpressionNode result = new ExpressionNodeForRangeValue( rangeValue );
		for (CellIndex element : _range) {
			ExpressionNode elementNode = buildExpressionModel( element );
			result.getArguments().add( elementNode );
		}
		return result;
	}


	private SectionPath getSectionPathFor( CellIndex _cell ) throws CompilerError
	{
		return getSectionPathFor( _cell, null );
	}


	private SectionPath getSectionPathFor( CellRange _range ) throws CompilerError
	{
		return getSectionPathFor( _range.getFrom(), _range );
	}


	private SectionPath getSectionPathFor( CellIndex _cell, CellRange _range ) throws CompilerError
	{
		SectionPath result = null;
		if (this.sectionDef.contains( _cell )) {
			SectionBinding innerDef = this.sectionDef.getContainingSection( _cell );
			if (null == innerDef) {
				return null;
			}
			else {
				result = new SectionPath( this );
				result.setTargetRange( _range );
				result.stepInto( innerDef );
				result.buildStepsInto( _cell );
			}
		}
		else {
			result = new SectionPath( this );
			result.setTargetRange( _range );
			result.stepOut();
			result.buildStepsTo( _cell );
		}
		return result;
	}


	private CellInstance getCell( CellIndex _cellIndex )
	{
		return _cellIndex.getCell( this.engineDef.getWorkbook() );
	}


}
