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
package org.formulacompiler.compiler.internal.model.optimizer.consteval;

import org.formulacompiler.compiler.internal.expressions.*;
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
		else if (_node instanceof ExpressionNodeForFold) return new EvalFold( (ExpressionNodeForFold) _node, this.type );
		else if (_node instanceof ExpressionNodeForReduce) return new EvalReduce( (ExpressionNodeForReduce) _node, this.type );
		else if (_node instanceof ExpressionNodeForFoldArray) return new EvalFoldArray( (ExpressionNodeForFoldArray) _node, this.type );
		else if (_node instanceof ExpressionNodeForDatabaseFold) return new EvalDatabaseFold( (ExpressionNodeForDatabaseFold) _node, this.type );
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