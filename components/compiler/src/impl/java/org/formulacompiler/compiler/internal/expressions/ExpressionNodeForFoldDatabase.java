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
