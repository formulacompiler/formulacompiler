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

package org.formulacompiler.compiler.internal.expressions;

import org.formulacompiler.compiler.internal.DescriptionBuilder;


public final class ExpressionNodeForFoldDatabase extends ExpressionNodeForFoldApply
{
	private final DataType[] filterColumnTypes;
	private final String filterColumnNamePrefix;
	private final int staticFoldedColumnIndex;
	private final int[] foldableColumnKeys;
	private final String[] filterColumnNames;

	private ExpressionNodeForFoldDatabase( DataType[] _filterColumnTypes, String _filterColNamePrefix,
			int _staticFoldedColumnIndex, int[] _foldableColumnKeys )
	{
		super();
		this.filterColumnTypes = _filterColumnTypes;
		this.filterColumnNamePrefix = _filterColNamePrefix;
		this.staticFoldedColumnIndex = _staticFoldedColumnIndex;
		this.foldableColumnKeys = _foldableColumnKeys;

		final int nCol = _filterColumnTypes.length;
		this.filterColumnNames = new String[ nCol ];
		for (int iCol = 0; iCol < nCol; iCol++) {
			this.filterColumnNames[ iCol ] = _filterColNamePrefix + iCol;
		}
	}

	public ExpressionNodeForFoldDatabase( ExpressionNode _foldDef, DataType[] _filterColumnTypes,
			String _filterColNamePrefix, ExpressionNode _filter, int _staticFoldedColumnIndex, int[] _foldableColumnKeys,
			ExpressionNode _foldedColumnIndex, ExpressionNode _arrayRef )
	{
		this( _filterColumnTypes, _filterColNamePrefix, _staticFoldedColumnIndex, _foldableColumnKeys );
		addArgument( _foldDef );
		addArgument( _filter );
		addArgument( _foldedColumnIndex );
		addArgument( _arrayRef );
	}


	public DataType[] filterColumnTypes()
	{
		return this.filterColumnTypes;
	}

	public final String filterColumnNamePrefix()
	{
		return this.filterColumnNamePrefix;
	}

	public final ExpressionNode filter()
	{
		return argument( 1 );
	}

	public final int staticFoldedColumnIndex()
	{
		return this.staticFoldedColumnIndex;
	}

	public final int[] foldableColumnKeys()
	{
		return this.foldableColumnKeys;
	}

	public final ExpressionNode foldedColumnIndex()
	{
		return argument( 2 );
	}

	public final ExpressionNodeForArrayReference table()
	{
		return (ExpressionNodeForArrayReference) argument( 3 );
	}

	public final String[] filterColumnNames()
	{
		return this.filterColumnNames;
	}


	@Override
	protected void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg )
	{
		super.describeToWithConfig( _to, _cfg );
		_to.append( "db filter " ).append( filterColumnNamePrefix() ).append( ": " );
		filter().describeTo( _to, _cfg );
		_to.append( " column " );
		if (staticFoldedColumnIndex() >= 0) {
			_to.append( '#' ).append( staticFoldedColumnIndex() );
		}
		else {
			foldedColumnIndex().describeTo( _to, _cfg );
		}
		_to.append( " of " );
		table().describeTo( _to, _cfg );
	}

	@Override
	protected ExpressionNode innerCloneWithoutArguments()
	{
		return new ExpressionNodeForFoldDatabase( filterColumnTypes(), filterColumnNamePrefix(),
				staticFoldedColumnIndex(), foldableColumnKeys() );
	}

}
