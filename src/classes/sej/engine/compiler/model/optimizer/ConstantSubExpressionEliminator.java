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
package sej.engine.compiler.model.optimizer;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import sej.engine.compiler.model.AbstractEngineModelVisitor;
import sej.engine.compiler.model.CellModel;
import sej.engine.compiler.model.ExpressionNodeForSectionModel;
import sej.engine.compiler.model.SectionModel;
import sej.engine.expressions.Evaluatable;
import sej.engine.expressions.EvaluationContext;
import sej.engine.expressions.EvaluationFailed;
import sej.engine.expressions.ExpressionNode;
import sej.engine.expressions.ExpressionNodeForAggregator;
import sej.engine.expressions.ExpressionNodeForConstantValue;
import sej.engine.expressions.ExpressionNodeForRangeValue;
import sej.engine.expressions.Aggregator.Aggregation;

final public class ConstantSubExpressionEliminator extends AbstractEngineModelVisitor
{
	private final EvaluationContext constantEvaluationContext = null;


	@Override
	public boolean visit( CellModel _cell )
	{
		ExpressionNode sourceExpr = _cell.getExpression();
		if (null != sourceExpr) {
			Object optimizedResult = eliminateConstantsFrom( sourceExpr, _cell.getSection() );
			if (optimizedResult instanceof ExpressionNode) {
				ExpressionNode optimizedExpr = (ExpressionNode) optimizedResult;
				_cell.setExpression( optimizedExpr );
			}
			else {
				_cell.setExpression( null );
				_cell.setConstantValue( optimizedResult );
			}
		}
		return true;
	}


	private Object eliminateConstantsFrom( ExpressionNode _expr, SectionModel _section )
	{
		if (null == _expr) return null;
		Object constantValue;
		try {
			constantValue = _expr.tryToEvaluate( this.constantEvaluationContext );
		}
		catch (InvocationTargetException e) {
			return new EvaluationFailed();
		}
		if (constantValue instanceof EvaluationFailed) {
			SectionModel section = _section;
			if (_expr instanceof ExpressionNodeForSectionModel) {
				ExpressionNodeForSectionModel sectionNode = (ExpressionNodeForSectionModel) _expr;
				section = sectionNode.getSectionModel();
			}
			eliminateConstantsFromArgsOf( _expr, section );
			if (_expr instanceof ExpressionNodeForAggregator) {
				ExpressionNodeForAggregator agg = (ExpressionNodeForAggregator) _expr;
				partiallyAggregate( agg );
			}
			return _expr;
		}
		else {
			return constantValue;
		}
	}


	private void eliminateConstantsFromArgsOf( ExpressionNode _expr, SectionModel _section )
	{
		List<ExpressionNode> args = _expr.getArguments();
		if (args.size() > 0) {
			ExpressionNode[] sourceArgs = args.toArray( new ExpressionNode[ args.size() ] );
			args.clear();
			for (ExpressionNode sourceArg : sourceArgs) {
				Object optimizedResult = eliminateConstantsFrom( sourceArg, _section );
				if (optimizedResult instanceof ExpressionNode) {
					ExpressionNode optimizedExpr = (ExpressionNode) optimizedResult;
					args.add( optimizedExpr );
				}
				else {
					args.add( new ExpressionNodeForConstantValue( optimizedResult ) );
				}
			}
		}
	}


	private void partiallyAggregate( ExpressionNodeForAggregator _expr )
	{
		if (_expr.getAggregator().isPartialAggregationSupported()) {
			final Aggregation partialAggregation = _expr.getAggregator().newAggregation();
			final List<ExpressionNode> args = _expr.getArguments();
			final ExpressionNode[] sourceArgs = args.toArray( new ExpressionNode[ args.size() ] );
			boolean isPartiallyAggregated = false;

			args.clear();
			for (ExpressionNode sourceArg : sourceArgs) {
				if (sourceArg instanceof ExpressionNodeForRangeValue) {
					ExpressionNodeForRangeValue sourceRange = (ExpressionNodeForRangeValue) sourceArg;
					for (ExpressionNode rangeArg : sourceRange.getArguments()) {
						if (aggregatedAsConstant( rangeArg, partialAggregation )) {
							isPartiallyAggregated = true;
						}
						else {
							args.add( rangeArg );
						}
					}
				}
				else if (aggregatedAsConstant( sourceArg, partialAggregation )) {
					isPartiallyAggregated = true;
				}
				else {
					args.add( sourceArg );
				}
			}

			if (isPartiallyAggregated) {
				_expr.setPartialAggregation( partialAggregation );
			}
		}
	}


	boolean aggregatedAsConstant( Evaluatable _evaluator, Aggregation _aggregation )
	{
		Object constantValue;
		try {
			constantValue = _evaluator.tryToEvaluate( this.constantEvaluationContext );
		}
		catch (InvocationTargetException e) {
			return false;
		}
		if (constantValue instanceof EvaluationFailed) {
			return false;
		}
		else {
			_aggregation.aggregate( constantValue );
			return true;
		}
	}


}
