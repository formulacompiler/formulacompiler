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
package org.formulacompiler.compiler.internal.model;

import org.formulacompiler.compiler.internal.expressions.ExpressionNode;

/**
 * Contains static convenience methods for building models.
 * 
 * @author peo
 */
public final class ComputationModelBuilder
{


	public static CellModel cst( SectionModel _section, String _name, double _value )
	{
		final CellModel result = new CellModel( _section, _name );
		result.setConstantValue( _value );
		return result;
	}

	public static CellModel expr( SectionModel _section, String _name, ExpressionNode _expr )
	{
		final CellModel result = new CellModel( _section, _name );
		result.setExpression( _expr );
		return result;
	}


	public static ExpressionNode cell( CellModel _cell )
	{
		return new ExpressionNodeForCellModel( _cell );
	}

	public static ExpressionNode[] cells( CellModel... _cells )
	{
		final ExpressionNode[] result = new ExpressionNode[ _cells.length ];
		for (int i = 0; i < _cells.length; i++) {
			result[ i ] = cell( _cells[ i ] );
		}
		return result;
	}
	
	public static ExpressionNode sub( SectionModel _subSection, ExpressionNode... _args )
	{
		return new ExpressionNodeForSubSectionModel( _subSection, _args );
	}


	/**
	 * Not to be instantiated.
	 */
	private ComputationModelBuilder()
	{
		super();
	}

}
