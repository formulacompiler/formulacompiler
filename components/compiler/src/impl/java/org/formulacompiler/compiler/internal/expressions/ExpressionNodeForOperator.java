/*
 * Copyright (c) 2006-2009 by Abacus Research AG, Switzerland.
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

package org.formulacompiler.compiler.internal.expressions;

import java.util.Collection;

import org.formulacompiler.compiler.Operator;
import org.formulacompiler.compiler.internal.DescriptionBuilder;


public class ExpressionNodeForOperator extends ExpressionNodeForScalar
{
	private final Operator operator;


	public ExpressionNodeForOperator( Operator _operator, ExpressionNode... _args )
	{
		super( _args );
		this.operator = _operator;
	}

	public ExpressionNodeForOperator( Operator _operator, Collection<ExpressionNode> _args )
	{
		super( _args );
		this.operator = _operator;
	}


	public Operator getOperator()
	{
		return this.operator;
	}


	@Override
	public ExpressionNode innerCloneWithoutArguments()
	{
		return new ExpressionNodeForOperator( this.operator );
	}


	@Override
	public void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg )
	{
		final int argCount = arguments().size();
		switch (argCount) {

			case 0:
				_to.append( this.operator.getSymbol() );
				break;
			case 1:
				_to.append( "(" );
				if (this.operator.isPrefix()) _to.append( this.operator.getSymbol() );
				describeArgumentTo( _to, _cfg, 0 );
				if (!this.operator.isPrefix()) _to.append( this.operator.getSymbol() );
				_to.append( ")" );
				break;
			default:
				_to.append( "(" );
				describeArgumentTo( _to, _cfg, 0 );
				for (int i = 1; i < argCount; i++) {
					_to.append( " " );
					_to.append( this.operator.getSymbol() );
					_to.append( " " );
					describeArgumentTo( _to, _cfg, i );
				}
				_to.append( ")" );
				break;
		}
	}

}
