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
package sej.internal.model.analysis;

import sej.CompilerException;
import sej.internal.expressions.AbstractExpressionNodeForFold;
import sej.internal.expressions.DataType;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForConstantValue;
import sej.internal.expressions.ExpressionNodeForFold;
import sej.internal.expressions.ExpressionNodeForFold1st;
import sej.internal.expressions.ExpressionNodeForFunction;
import sej.internal.expressions.ExpressionNodeForLet;
import sej.internal.expressions.ExpressionNodeForLetVar;
import sej.internal.expressions.ExpressionNodeForOperator;
import sej.internal.expressions.LetDictionary;
import sej.internal.model.AbstractComputationModelVisitor;
import sej.internal.model.CellModel;
import sej.internal.model.ExpressionNodeForCellModel;
import sej.internal.model.ExpressionNodeForParentSectionModel;
import sej.internal.model.ExpressionNodeForRangeValue;
import sej.internal.model.ExpressionNodeForSubSectionModel;
import sej.internal.model.RangeValue;


public final class TypeAnnotator extends AbstractComputationModelVisitor
{
	private final LetDictionary letDict = new LetDictionary();

	private LetDictionary letDict()
	{
		return this.letDict;
	}


	@Override
	public boolean visit( CellModel _cell ) throws CompilerException
	{
		annotate( _cell );
		return true;
	}


	private DataType annotate( CellModel _cell ) throws CompilerException
	{
		final DataType type = _cell.getDataType();
		if (null != type) {
			return type;
		}
		else {
			final Object cst = _cell.getConstantValue();
			if (null != cst) {
				_cell.setDataType( typeOfConstant( cst ) );
			}
			else {
				ExpressionNode expr = _cell.getExpression();
				if (null != expr) {
					_cell.setDataType( annotate( expr ) );
				}
				else {
					_cell.setDataType( DataType.NULL );
				}
			}
			return _cell.getDataType();
		}
	}


	private DataType annotate( ExpressionNode _expr ) throws CompilerException
	{
		if (null == _expr) {
			return DataType.NULL;
		}
		else {
			final DataType type = _expr.getDataType();
			if (null != type) {
				return type;
			}
			else {
				_expr.setDataType( dispatch( _expr ) );
				return _expr.getDataType();
			}
		}
	}

	private DataType dispatch( ExpressionNode _expr ) throws CompilerException
	{
		if (_expr instanceof ExpressionNodeForConstantValue) return typeOf( (ExpressionNodeForConstantValue) _expr );
		if (_expr instanceof ExpressionNodeForCellModel) return typeOf( (ExpressionNodeForCellModel) _expr );
		if (_expr instanceof ExpressionNodeForRangeValue) return typeOf( (ExpressionNodeForRangeValue) _expr );
		if (_expr instanceof ExpressionNodeForOperator) return typeOf( (ExpressionNodeForOperator) _expr );
		if (_expr instanceof ExpressionNodeForFunction) return typeOf( (ExpressionNodeForFunction) _expr );
		if (_expr instanceof ExpressionNodeForParentSectionModel)
			return typeOf( (ExpressionNodeForParentSectionModel) _expr );
		if (_expr instanceof ExpressionNodeForSubSectionModel) return typeOf( (ExpressionNodeForSubSectionModel) _expr );

		if (_expr instanceof ExpressionNodeForLet) return typeOf( (ExpressionNodeForLet) _expr );
		if (_expr instanceof ExpressionNodeForLetVar) return typeOf( (ExpressionNodeForLetVar) _expr );
		if (_expr instanceof ExpressionNodeForFold) return typeOf( (ExpressionNodeForFold) _expr );
		if (_expr instanceof ExpressionNodeForFold1st) return typeOf( (ExpressionNodeForFold1st) _expr );

		unsupported( _expr );
		return null;
	}

	private void annotateArgs( ExpressionNode _expr ) throws CompilerException
	{
		for (ExpressionNode arg : _expr.arguments()) {
			annotate( arg );
		}
	}


	private DataType typeOfConstant( Object _value )
	{
		if (null == _value) {
			return DataType.NULL;
		}
		else if (_value instanceof String) {
			return DataType.STRING;
		}
		else if (_value instanceof RangeValue) {
			final RangeValue range = (RangeValue) _value;
			if (range.size() > 0) {
				return typeOfConstant( range.get( 0 ) );
			}
			else {
				return DataType.NULL;
			}
		}
		else {
			return DataType.NUMERIC;
		}
	}

	private DataType typeOf( Iterable<ExpressionNode> _exprs )
	{
		DataType type = DataType.NULL;
		for (ExpressionNode arg : _exprs) {
			if (null != arg) {
				DataType argType = arg.getDataType();
				switch (argType) {
					case NUMERIC:
						return argType;
					case STRING:
						type = argType;
						break;
				}
			}
		}
		return type;
	}

	private DataType typeOf( ExpressionNodeForConstantValue _expr )
	{
		return typeOfConstant( _expr.getValue() );
	}

	private DataType typeOf( ExpressionNodeForCellModel _expr ) throws CompilerException
	{
		return annotate( _expr.getCellModel() );
	}

	private DataType typeOf( ExpressionNodeForRangeValue _expr ) throws CompilerException
	{
		annotateArgs( _expr );
		return typeOf( _expr.arguments() );
	}

	private DataType typeOf( ExpressionNodeForOperator _expr ) throws CompilerException
	{
		annotateArgs( _expr );
		switch (_expr.getOperator()) {
			case NOOP:
				return DataType.NULL;
			case CONCAT:
				return DataType.STRING;
			default:
				return DataType.NUMERIC;
		}
	}

	private DataType typeOf( ExpressionNodeForFunction _expr ) throws CompilerException
	{
		annotateArgs( _expr );
		switch (_expr.getFunction()) {

			case IF:
				if (_expr.arguments().size() >= 2) {
					return _expr.arguments().get( 1 ).getDataType();
				}
				else {
					unsupported( _expr );
					return DataType.NULL;
				}

			case INDEX:
				if (_expr.arguments().size() >= 1) {
					return _expr.arguments().get( 0 ).getDataType();
				}
				else {
					unsupported( _expr );
					return DataType.NULL;
				}

			case CONCATENATE:
			case MID:
			case LEFT:
			case RIGHT:
			case SUBSTITUTE:
			case REPLACE:
			case LOWER:
			case UPPER:
			case PROPER:
				return DataType.STRING;

			default:
				return DataType.NUMERIC;
		}
	}

	private DataType typeOf( ExpressionNodeForParentSectionModel _expr ) throws CompilerException
	{
		if (1 != _expr.arguments().size()) unsupported( _expr );
		return annotate( _expr.arguments().get( 0 ) );
	}

	private DataType typeOf( ExpressionNodeForSubSectionModel _expr ) throws CompilerException
	{
		annotateArgs( _expr );
		return typeOf( _expr.arguments() );
	}


	private DataType typeOf( ExpressionNodeForLet _expr ) throws CompilerException
	{
		final DataType valType = annotate( _expr.value() );
		letDict().let( _expr.varName(), valType, null );
		try {
			return annotate( _expr.in() );
		}
		finally {
			letDict().unlet( _expr.varName() );
		}
	}

	private DataType typeOf( ExpressionNodeForLetVar _expr )
	{
		return letDict().lookupType( _expr.varName() );
	}

	private DataType typeOf( ExpressionNodeForFold _expr ) throws CompilerException
	{
		annotateFold( _expr );
		return _expr.initialAccumulatorValue().getDataType();
	}

	private DataType typeOf( ExpressionNodeForFold1st _expr ) throws CompilerException
	{
		final DataType eltType = annotateFold( _expr );

		final String firstName = _expr.firstName();
		letDict().let( firstName, eltType, null );
		try {
			annotate( _expr.firstValue() );
		}
		finally {
			letDict().unlet( firstName );
		}

		return _expr.emptyValue().getDataType();
	}
	
	private DataType annotateFold( AbstractExpressionNodeForFold _expr ) throws CompilerException 
	{
		for (final ExpressionNode elt : _expr.elements()) {
			annotate( elt );
		}
		final DataType eltType = typeOf( _expr.elements() );
		final DataType resultType = annotate( _expr.initialAccumulatorValue() );

		final String accName = _expr.accumulatorName();
		final String eltName = _expr.elementName();
		letDict().let( accName, resultType, null );
		letDict().let( eltName, eltType, null );
		try {
			annotate( _expr.accumulatingStep() );
		}
		finally {
			letDict().unlet( eltName );
			letDict().unlet( accName );
		}

		return eltType;
	}


	private void unsupported( ExpressionNode _expr ) throws CompilerException
	{
		throw new CompilerException.UnsupportedExpression( _expr.toString() );
	}


}
