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
import java.util.ListIterator;

import org.formulacompiler.compiler.internal.DescriptionBuilder;


public final class ExpressionNodeForArrayReference extends ExpressionNode
{
	private final ArrayDescriptor arrayDescriptor;

	public ExpressionNodeForArrayReference( ArrayDescriptor _descriptor, ExpressionNode... _args )
	{
		super( _args );
		this.arrayDescriptor = _descriptor;
	}


	public ArrayDescriptor arrayDescriptor()
	{
		return this.arrayDescriptor;
	}


	@Override
	public boolean hasConstantValue()
	{
		return areConstant( arguments() );
	}

	@Override
	public Object getConstantValue()
	{
		return this;
	}


	@Override
	public ExpressionNode innerCloneWithoutArguments()
	{
		return new ExpressionNodeForArrayReference( new ArrayDescriptor( arrayDescriptor() ) );
	}


	@Override
	protected int countValues( LetDictionary _letDict, Collection<ExpressionNode> _uncountables )
	{
		return arrayDescriptor().numberOfElements();
	}


	@Override
	public void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg )
	{
		arrayDescriptor().describeTo( _to );
		_to.append( '{' );
		boolean isFirst = true;
		for (ExpressionNode arg : arguments()) {
			if (isFirst) isFirst = false;
			else _to.append( ", " );
			arg.describeTo( _to, _cfg );
		}
		_to.append( '}' );
	}


	public final ExpressionNodeForArrayReference subArray( int _firstRowDelta, int _nRows, int _firstColDelta, int _nCols )
	{
		final ArrayDescriptor myDesc = arrayDescriptor();
		final int myCols = myDesc.numberOfColumns();
		if (myDesc.numberOfSheets() != 1)
			throw new IllegalArgumentException( "Cannot handle arrays spanning sheets in subArray()" );

		final ArrayDescriptor subDesc = new ArrayDescriptor( myDesc, _firstRowDelta, _firstColDelta,
				_nRows - myDesc.extent().row(), _nCols - myDesc.extent().col() );
		final ExpressionNodeForArrayReference sub = new ExpressionNodeForArrayReference( subDesc );
		sub.setDataType( getDataType() );
		sub.setDeclaredDataType( getDeclaredDataType() );
		sub.setDerivedFrom( getDerivedFrom() );

		final ListIterator<ExpressionNode> vals = arguments().listIterator( _firstRowDelta * myCols );
		final int lastCol = _firstColDelta + _nCols - 1;
		for (int iRow = 0; iRow < _nRows; iRow++) {
			for (int iCol = 0; iCol < myCols; iCol++) {
				final ExpressionNode val = vals.next();
				if (_firstColDelta <= iCol && iCol <= lastCol) {
					sub.addArgument( val );
				}
			}
		}
		return sub;
	}
}
