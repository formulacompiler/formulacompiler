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

package org.formulacompiler.compiler.internal.model.optimizer.consteval;

import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForArrayReference;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldDatabase;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldDefinition;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldList;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldVectors;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFunction;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForLet;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForLetVar;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForMakeArray;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForMaxValue;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForMinValue;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForOperator;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForSubstitution;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForSwitch;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForSwitchCase;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeShadow;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForCellModel;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForParentSectionModel;
import org.formulacompiler.compiler.internal.model.interpreter.InterpretedNumericType;


public class EvalShadowBuilder implements ExpressionNodeShadow.Builder
{
	private final InterpretedNumericType type;

	public EvalShadowBuilder( InterpretedNumericType _type )
	{
		super();
		this.type = _type;
	}

	public ExpressionNodeShadow shadow( ExpressionNode _node )
	{
		// DO NOT REFORMAT BELOW THIS LINE
		if (_node instanceof ExpressionNodeForConstantValue) return new EvalConstantValue( _node, this.type );
		else if (_node instanceof ExpressionNodeForMinValue) return new EvalExtremum( _node, this.type, false );
		else if (_node instanceof ExpressionNodeForMaxValue) return new EvalExtremum( _node, this.type, true );
		else if (_node instanceof ExpressionNodeForArrayReference) return new EvalRangeValue( _node, this.type );
		else if (_node instanceof ExpressionNodeForOperator) return new EvalOperator( _node, this.type );
		else if (_node instanceof ExpressionNodeForFunction) return newEvalFunction( (ExpressionNodeForFunction) _node );
		else if (_node instanceof ExpressionNodeForCellModel) return new EvalCell( _node, this.type );
		else if (_node instanceof ExpressionNodeForParentSectionModel) return new EvalPassthrough( _node );
		else if (_node instanceof ExpressionNodeForSubstitution) return new EvalSubstitution( _node );
		else if (_node instanceof ExpressionNodeForLet) return new EvalLet( (ExpressionNodeForLet) _node, this.type );
		else if (_node instanceof ExpressionNodeForSwitch) return new EvalSwitch( (ExpressionNodeForSwitch) _node, this.type );
		else if (_node instanceof ExpressionNodeForSwitchCase) return new EvalSwitchCase( (ExpressionNodeForSwitchCase) _node, this.type );
		else if (_node instanceof ExpressionNodeForLetVar) return new EvalLetVar( (ExpressionNodeForLetVar) _node, this.type );
		else if (_node instanceof ExpressionNodeForFoldDefinition) return new EvalFoldDefinition( (ExpressionNodeForFoldDefinition) _node, this.type );
		else if (_node instanceof ExpressionNodeForFoldList) return new EvalFoldList( (ExpressionNodeForFoldList) _node, this.type );
		else if (_node instanceof ExpressionNodeForFoldVectors) return new EvalFoldVectors( (ExpressionNodeForFoldVectors) _node, this.type );
		else if (_node instanceof ExpressionNodeForFoldDatabase) return new EvalFoldDatabase( (ExpressionNodeForFoldDatabase) _node, this.type );
		else if (_node instanceof ExpressionNodeForMakeArray) return new EvalMakeArray( _node );
		else return new EvalNonFoldable( _node );
		// DO NOT REFORMAT ABOVE THIS LINE
	}

	private ExpressionNodeShadow newEvalFunction( ExpressionNodeForFunction _node )
	{
		switch (_node.getFunction()) {
			case IF:
				return new EvalIf( _node, this.type );
			case INDEX:
				return new EvalIndex( _node, this.type );
		}
		return new EvalFunction( _node, this.type );
	}

}