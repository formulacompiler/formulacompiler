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

import java.util.Collection;

import sej.ModelError;
import sej.engine.compiler.definition.CellDefinition;
import sej.engine.compiler.definition.EngineDefinition;
import sej.engine.compiler.definition.InputCellDefinition;
import sej.engine.compiler.definition.SectionDefinition;
import sej.engine.compiler.model.CellModel;
import sej.engine.compiler.model.ExpressionNodeForCellModel;
import sej.engine.compiler.model.ExpressionNodeForRangeValue;
import sej.engine.compiler.model.SectionModel;
import sej.engine.compiler.model.util.InterpretedNumericType;
import sej.expressions.ExpressionNode;
import sej.expressions.ExpressionNodeForConstantValue;
import sej.model.CellIndex;
import sej.model.CellInstance;
import sej.model.CellRange;
import sej.model.CellWithConstant;
import sej.model.CellWithLazilyParsedExpression;
import sej.model.ExpressionNodeForCell;
import sej.model.ExpressionNodeForRange;
import sej.model.ExpressionNodeForRangeShape;


public class SectionModelCompiler
{
	private final EngineModelCompiler compiler;
	private final EngineDefinition engineDef;
	private final SectionModelCompiler section;
	private final SectionDefinition sectionDef;
	private final SectionModel sectionModel;


	public SectionModelCompiler(EngineModelCompiler _compiler, SectionModelCompiler _section,
			SectionDefinition _sectionDef, SectionModel _model)
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


	public SectionDefinition getSectionDef()
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


	public CellModel createCellModel( CellDefinition _cellDef ) throws ModelError
	{
		boolean isInput = _cellDef instanceof InputCellDefinition;
		CellModel result = createCellModel( _cellDef.getIndex(), isInput );
		if (isInput) {
			result.makeInput( ((InputCellDefinition) _cellDef).getCallChainToCall() );
		}
		return result;
	}


	public CellModel createCellModel( CellIndex _cellIndex, boolean _isInput ) throws ModelError
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


	CellModel getOrCreateCellModel( CellIndex _cellIndex ) throws ModelError
	{
		CellModel result = this.compiler.getCellModel( _cellIndex );
		if (null == result) {
			result = createCellModel( _cellIndex );
		}
		return result;
	}


	CellModel createCellModel( CellIndex _cellIndex ) throws ModelError
	{
		InputCellDefinition inputDef = this.engineDef.getInputs().get( _cellIndex );
		if (null != inputDef) {
			return createCellModel( inputDef );
		}
		else {
			return createCellModel( _cellIndex, false );
		}
	}


	SectionModelCompiler getOrCreateSectionCompiler( SectionDefinition _sectionDef )
	{
		SectionModelCompiler result = this.compiler.getSectionCompiler( _sectionDef );
		if (null == result) {
			result = createSectionCompiler( _sectionDef );
		}
		return result;
	}


	SectionModelCompiler createSectionCompiler( SectionDefinition _sectionDef )
	{
		SectionModel model = new SectionModel( getSectionModel(), _sectionDef.getCallChainToCall().toString() );
		return new SectionModelCompiler( this.compiler, this, _sectionDef, model );
	}


	private void buildCellModel( CellInstance _cell, CellModel _cellModel ) throws ModelError
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


	private void buildCellModel( CellModel _cellModel, CellWithLazilyParsedExpression _cell ) throws ModelError
	{
		ExpressionNode exprDef = _cell.getExpression();
		ExpressionNode exprModel = (ExpressionNode) buildExpressionModel( exprDef );
		_cellModel.setExpression( exprModel );
	}


	@SuppressWarnings("unchecked")
	private Object buildExpressionModel( ExpressionNode _exprDef ) throws ModelError
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


	ExpressionNode buildExpressionModel( CellIndex _cellIndex ) throws ModelError
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


	private ExpressionNode buildExpressionModelForContainedCell( CellIndex _cellIndex ) throws ModelError
	{
		CellModel cellModel = getOrCreateCellModel( _cellIndex );
		return new ExpressionNodeForCellModel( cellModel );
	}


	private ExpressionNode buildExpressionModel( CellRange _range ) throws ModelError
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


	private ExpressionNode buildExpressionModelForContainedRange( CellRange _range ) throws ModelError
	{
		ExpressionNode result = new ExpressionNodeForRangeValue( _range.getRangeValue() );
		for (CellIndex element : _range) {
			ExpressionNode elementNode = buildExpressionModel( element );
			result.getArguments().add( elementNode );
		}
		return result;
	}


	private SectionPath getSectionPathFor( CellIndex _cell ) throws ModelError
	{
		return getSectionPathFor( _cell, null );
	}


	private SectionPath getSectionPathFor( CellRange _range ) throws ModelError
	{
		return getSectionPathFor( _range.getFrom(), _range );
	}


	private SectionPath getSectionPathFor( CellIndex _cell, CellRange _range ) throws ModelError
	{
		SectionPath result = null;
		if (this.sectionDef.contains( _cell )) {
			SectionDefinition innerDef = this.sectionDef.getContainingSection( _cell );
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
