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

import static org.formulacompiler.compiler.internal.expressions.ExpressionBuilder.*;
import java.util.Collection;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.internal.expressions.ArrayDescriptor;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForArrayReference;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFunction;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForSubstitution;
import org.formulacompiler.compiler.internal.model.CellModel;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForCellModel;
import org.formulacompiler.compiler.internal.model.SectionModel;
import org.formulacompiler.runtime.New;
import org.formulacompiler.spreadsheet.Orientation;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.internal.CellIndex;
import org.formulacompiler.spreadsheet.internal.CellInstance;
import org.formulacompiler.spreadsheet.internal.CellRange;
import org.formulacompiler.spreadsheet.internal.CellWithConstant;
import org.formulacompiler.spreadsheet.internal.CellWithError;
import org.formulacompiler.spreadsheet.internal.CellWithLazilyParsedExpression;
import org.formulacompiler.spreadsheet.internal.ExpressionNodeForCell;
import org.formulacompiler.spreadsheet.internal.ExpressionNodeForRange;
import org.formulacompiler.spreadsheet.internal.ExpressionNodeForRangeShape;
import org.formulacompiler.spreadsheet.internal.binding.CellBinding;
import org.formulacompiler.spreadsheet.internal.binding.InputCellBinding;
import org.formulacompiler.spreadsheet.internal.binding.SectionBinding;
import org.formulacompiler.spreadsheet.internal.binding.WorkbookBinding;


public final class SectionModelCompiler
{
	private final SpreadsheetToModelCompiler compiler;
	private final WorkbookBinding engineDef;
	private final SectionModelCompiler section;
	private final SectionBinding sectionDef;
	private final SectionModel sectionModel;
	private final NumericType numericType;


	public SectionModelCompiler( SpreadsheetToModelCompiler _compiler, SectionModelCompiler _section,
			SectionBinding _sectionDef, SectionModel _model )
	{
		this.compiler = _compiler;
		this.engineDef = _compiler.getEngineDef();
		this.section = _section;
		this.sectionDef = _sectionDef;
		this.sectionModel = _model;
		this.numericType = _compiler.numericType();
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

	public NumericType numericType()
	{
		return this.numericType;
	}


	public CellModel createCellModel( CellBinding _cellDef ) throws CompilerException
	{
		final boolean isInput = _cellDef instanceof InputCellBinding;
		final CellModel result = createCellModel( _cellDef.getIndex(), isInput );
		if (isInput) {
			result.makeInput( ((InputCellBinding) _cellDef).getCallChainToCall() );
		}
		return result;
	}


	public CellModel createCellModel( CellIndex _cellIndex, boolean _isInput ) throws CompilerException
	{
		final CellInstance cell = _cellIndex.getCell();
		final boolean nonNull = (null != cell);
		if (nonNull || _isInput) {
			final CellModel result = new CellModel( this.sectionModel, _cellIndex.getShortName() );
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


	CellModel getOrCreateCellModel( CellIndex _cellIndex ) throws CompilerException
	{
		CellModel result = this.compiler.getCellModel( _cellIndex );
		if (null == result) {
			result = createCellModel( _cellIndex );
		}
		return result;
	}


	CellModel createCellModel( CellIndex _cellIndex ) throws CompilerException
	{
		final InputCellBinding inputDef = this.engineDef.getInputs().get( _cellIndex );
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
		final SectionModel model = new SectionModel( getSectionModel(), _sectionDef.getCallChainToCall().toString(),
				_sectionDef.getInputClass(), _sectionDef.getOutputClass() );
		model.setOriginalName( _sectionDef.getRange().describe() );
		model.makeInput( _sectionDef.getCallChainToCall() );
		if (_sectionDef.getCallToImplement() != null) {
			model.makeOutput( _sectionDef.getCallToImplement() );
		}
		return new SectionModelCompiler( this.compiler, this, _sectionDef, model );
	}


	private void buildCellModel( CellInstance _cell, CellModel _cellModel ) throws CompilerException
	{
		if (null == _cell) throw new IllegalArgumentException();
		if (_cell instanceof CellWithConstant) {
			buildCellModel( _cellModel, (CellWithConstant) _cell );
		}
		else if (_cell instanceof CellWithLazilyParsedExpression) {
			buildCellModel( _cellModel, (CellWithLazilyParsedExpression) _cell );
		}
		else if (_cell instanceof CellWithError) {
			buildCellModel( _cellModel, (CellWithError) _cell );
		}
		_cellModel.setMaxFractionalDigits( _cell.getMaxFractionalDigits() );
	}


	private void buildCellModel( CellModel _cellModel, CellWithConstant _cell )
	{
		_cellModel.setConstantValue( adjustConstantValue( _cell.getValue() ) );
	}


	private void buildCellModel( CellModel _cellModel, CellWithError _cell )
	{
		if (_cell.getValue() == CellWithError.NA) {
			_cellModel.setExpression( new ExpressionNodeForFunction( Function.NA ) );
		}
		else {
			_cellModel.setExpression( err( _cell.getError() ) );
		}
	}


	private void buildCellModel( CellModel _cellModel, CellWithLazilyParsedExpression _cell ) throws CompilerException
	{
		try {
			final ExpressionNode exprModel = buildExpressionModel( _cell.getExpression() );
			_cellModel.setExpression( exprModel );
		}
		catch (SpreadsheetException cause) {
			throw new CompilerException.UnsupportedExpression( cause );
		}
		catch (CompilerException e) {
			e.addMessageContext( "\nReferenced by cell " + _cell.getCanonicalName() + "." );
			throw e;
		}
	}


	private ExpressionNode buildExpressionModel( ExpressionNode _exprDef ) throws CompilerException
	{
		final ExpressionNode result = buildRawExpressionModel( _exprDef );
		if (result != null) {
			result.setDerivedFrom( _exprDef );
		}
		return result;
	}


	@SuppressWarnings( "unchecked" )
	private ExpressionNode buildRawExpressionModel( ExpressionNode _exprDef ) throws CompilerException
	{
		if (null == _exprDef) {
			return null;
		}
		else if (_exprDef instanceof ExpressionNodeForConstantValue) {
			final ExpressionNodeForConstantValue cst = (ExpressionNodeForConstantValue) _exprDef;
			return new ExpressionNodeForConstantValue( adjustConstantValue( cst.value() ) );
		}
		else if (_exprDef instanceof ExpressionNodeForCell) {
			final CellIndex cell = ((ExpressionNodeForCell) _exprDef).getCellIndex();
			return buildExpressionModelForCell( cell );
		}
		else if (_exprDef instanceof ExpressionNodeForRangeShape) {
			final ExpressionNode expressionNode = _exprDef.arguments().get( 0 );
			final CellRange range;
			if (expressionNode instanceof ExpressionNodeForRange) {
				range = ((ExpressionNodeForRange) expressionNode).getRange();
			}
			else if (expressionNode instanceof ExpressionNodeForCell) {
				final CellIndex cellIndex = ((ExpressionNodeForCell) expressionNode).getCellIndex();
				range = CellRange.getCellRange( cellIndex, cellIndex );
			}
			else {
				throw new IllegalArgumentException();
			}
			return new RangeExpressionBuilder( range, true ).build();
		}
		else if (_exprDef instanceof ExpressionNodeForRange) {
			final CellRange range = ((ExpressionNodeForRange) _exprDef).getRange();
			return new RangeExpressionBuilder( range, false ).build();
		}
		else {
			final ExpressionNode result = _exprDef.cloneWithoutArguments();
			for (ExpressionNode arg : _exprDef.arguments()) {
				final ExpressionNode argResult = buildExpressionModel( arg );
				if (argResult instanceof ExpressionNodeForSubstitution) {
					result.arguments().addAll( argResult.arguments() );
				}
				else {
					result.addArgument( argResult );
				}
			}
			return result;
		}
	}


	ExpressionNode buildExpressionModelForCell( CellIndex _cellIndex ) throws CompilerException
	{
		if (this.sectionDef.contains( _cellIndex )) {
			final SectionBinding containingSection = this.sectionDef.getContainingSection( _cellIndex );
			if (null != containingSection) {
				return new RangeExpressionBuilder( CellRange.getCellRange( _cellIndex, _cellIndex ), false ).build();
			}
			return buildExpressionModelForLocalCell( _cellIndex );
		}
		else {
			return buildExpressionModelForOuterCell( _cellIndex );
		}
	}

	private ExpressionNode buildExpressionModelForLocalCell( CellIndex _cellIndex ) throws CompilerException
	{
		final CellModel cellModel = getOrCreateCellModel( _cellIndex );
		return new ExpressionNodeForCellModel( cellModel );
	}


	private ExpressionNode buildExpressionModelForOuterCell( CellIndex _cellIndex ) throws CompilerException
	{
		final SectionPath path = new SectionPath( this );
		path.stepOut();
		path.stepOutTo( _cellIndex );
		return path.wrapAround( path.getSectionCompiler().buildExpressionModelForLocalCell( _cellIndex ) );
	}


	private Object adjustConstantValue( Object _value )
	{
		if (_value instanceof Number) {
			return numericType().valueOf( (Number) _value );
		}
		return _value;
	}


	@SuppressWarnings( { "unqualified-field-access", "hiding" } )
	private final class RangeExpressionBuilder
	{
		private final SectionBinding sectionDef = SectionModelCompiler.this.sectionDef;
		private final RangeExpressionBuilder parent;
		private final boolean shaped;
		private final boolean stepOutOnly;
		private final CellRange range;
		private final int sheets;
		private int rows;
		private int cols;

		private RangeExpressionBuilder( RangeExpressionBuilder _parent, CellRange _range, boolean _shaped,
				boolean _stepOutOnly )
		{
			super();
			this.parent = _parent;
			this.range = _range;
			this.shaped = _shaped;
			this.stepOutOnly = _stepOutOnly;
			final CellIndex from = range.getFrom();
			final CellIndex to = range.getTo();
			this.sheets = to.sheetIndex - from.sheetIndex + 1;
			this.rows = to.rowIndex - from.rowIndex + 1;
			this.cols = to.columnIndex - from.columnIndex + 1;
		}

		private RangeExpressionBuilder( RangeExpressionBuilder _parent, CellRange _range, boolean _shaped )
		{
			this( _parent, _range, _shaped, false );
		}

		private RangeExpressionBuilder( CellRange _range, boolean _shaped, boolean _stepOutOnly )
		{
			this( null, _range, _shaped, _stepOutOnly );
		}

		public RangeExpressionBuilder( CellRange _range, boolean _shaped )
		{
			this( null, _range, _shaped );
		}

		private void makeDynamic( Orientation _orient )
		{
			if (_orient == Orientation.HORIZONTAL) {
				this.cols = ArrayDescriptor.DYNAMIC;
			}
			else {
				this.rows = ArrayDescriptor.DYNAMIC;
			}
			if (null != this.parent) {
				this.parent.makeDynamic( _orient );
			}
		}

		public ExpressionNode build() throws CompilerException
		{
			final CellRange[] tiling = range.tilingAround( sectionDef.getRange(), sectionDef.getOrientation() );
			switch (tiling.length) {
				case CellRange.NO_INTERSECTION:
					return buildOuterRange();
				case CellRange.CONTAINED:
					return buildContainedRange();
				default:
					throw new SpreadsheetException.SectionSpan( range.getShortName(), sectionDef.toString() );
			}
		}

		private ExpressionNode buildOuterRange() throws CompilerException
		{
			final SectionPath path = new SectionPath( SectionModelCompiler.this );
			path.stepOut();
			final ExpressionNode outer = path.getSectionCompiler().new RangeExpressionBuilder( range, shaped, true )
					.build();
			if (shaped) {
				return path.wrapAround( outer );
			}
			else {
				return path.wrapAround( outer.arguments() );
			}
		}

		private ExpressionNode buildContainedRange() throws CompilerException
		{
			final Orientation ownOrient = this.sectionDef.getOrientation();
			final Collection<ExpressionNode> elts = New.collection();

			/*
			 * This loop relies on the subsections of the current section being sorted in ascending
			 * flow order.
			 */
			CellRange next = range;
			for (SectionBinding inner : this.sectionDef.getSections()) {
				final CellRange innerRange = inner.getRange();
				final Orientation innerOrient = inner.getOrientation();
				final CellRange[] tiling = (innerOrient == ownOrient) ? next.tilingAround( innerRange, innerOrient ) : next
						.tilingAround( innerRange );
				switch (tiling.length) {

					case CellRange.NO_INTERSECTION:
						break;

					case CellRange.CONTAINED: {
						final ExpressionNode expr = buildExpressionModelForInnerRange( inner, next );
						elts.add( expr );
						next = null;
						break;
					}

					case CellRange.FLOW_TILES: {
						final CellRange before = tiling[ CellRange.FLOW_BEFORE ];
						if (null != before) {
							/*
							 * This is where we rely on proper sorting. It ensures that `before` cannot
							 * possibly overlap one of the remaining inner section to scan.
							 */
							if (shaped) {
								elts.add( buildExpressionModelForLocalRange( before ) );
							}
							else {
								buildExpressionModelsForLocalRangeCells( before, elts );
							}
						}
						elts.add( buildExpressionModelForInnerRange( inner, tiling[ CellRange.FLOW_INNER ] ) );
						next = tiling[ CellRange.FLOW_AFTER ];
						break;
					}

					default:
						throw new SpreadsheetException.SectionSpan( range.getShortName(), inner.toString() );

				}
				if (null == next) break;
			}
			if (null != next) {
				if (0 == elts.size() || !shaped) {
					buildExpressionModelsForLocalRangeCells( next, elts );
				}
				else {
					elts.add( buildExpressionModelForLocalRange( next ) );
				}
			}

			final CellIndex from = range.getFrom();
			final ExpressionNode result = (shaped) ? new ExpressionNodeForArrayReference( new ArrayDescriptor(
					from.sheetIndex, from.rowIndex, from.columnIndex, sheets, rows, cols ) )
					: new ExpressionNodeForSubstitution();
			result.arguments().addAll( elts );
			return result;
		}

		private ExpressionNode buildExpressionModelForInnerRange( SectionBinding _inner, CellRange _range )
				throws CompilerException
		{
			if (this.stepOutOnly) {
				throw new CompilerException.ReferenceToOuterInnerCell();
			}

			makeDynamic( _inner.getOrientation() );
			final CellRange innerRange = _inner.getPrototypeRange( _range );
			final SectionPath path = new SectionPath( SectionModelCompiler.this );
			path.stepInto( _inner );
			final SectionModelCompiler innerDef = path.getSectionCompiler();
			final ExpressionNode expr = innerDef.new RangeExpressionBuilder( this, innerRange, shaped )
					.buildContainedRange();
			if (shaped) {
				return path.wrapAround( expr );
			}
			else {
				return path.wrapAround( expr.arguments() );
			}
		}

		private ExpressionNode buildExpressionModelForLocalRange( CellRange _range ) throws CompilerException
		{
			final CellIndex from = _range.getFrom();
			final CellIndex to = _range.getTo();
			final int sheets = to.sheetIndex - from.sheetIndex + 1;
			final int rows = to.rowIndex - from.rowIndex + 1;
			final int cols = to.columnIndex - from.columnIndex + 1;
			final ExpressionNode result = new ExpressionNodeForArrayReference( new ArrayDescriptor( from.sheetIndex,
					from.rowIndex, from.columnIndex, sheets, rows, cols ) );
			buildExpressionModelsForLocalRangeCells( _range, result.arguments() );
			return result;
		}

		private void buildExpressionModelsForLocalRangeCells( CellRange _range, Collection<ExpressionNode> _elts )
				throws CompilerException
		{
			for (CellIndex element : _range) {
				final ExpressionNode elementNode = buildExpressionModelForCell( element );
				_elts.add( elementNode );
			}
		}

	}

}
