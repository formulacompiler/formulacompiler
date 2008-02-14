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

package org.formulacompiler.compiler.internal.model;

import org.formulacompiler.compiler.internal.expressions.ExpressionBuilder;
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


	public static ExpressionNode[] nodes( Object[] _values )
	{
		final ExpressionNode[] result = new ExpressionNode[ _values.length ];
		for (int i = 0; i < _values.length; i++)
			result[ i ] = node( _values[ i ] );
		return result;
	}

	public static ExpressionNode[][] nodes( Object[][] _rows )
	{
		final ExpressionNode[][] result = new ExpressionNode[ _rows.length ][];
		for (int i = 0; i < _rows.length; i++)
			result[ i ] = nodes( _rows[ i ] );
		return result;
	}

	public static ExpressionNode node( Object _value )
	{
		if (_value instanceof ExpressionNode) return (ExpressionNode) _value;
		if (_value instanceof CellModel) return cell( (CellModel) _value );
		return ExpressionBuilder.cst( _value );
	}


	/**
	 * Not to be instantiated.
	 */
	private ComputationModelBuilder()
	{
		super();
	}

}
