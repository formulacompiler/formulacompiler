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

import java.util.Collection;

import org.formulacompiler.compiler.internal.DescriptionBuilder;
import org.formulacompiler.compiler.internal.expressions.ExpressionDescriptionConfig;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;


public final class ExpressionNodeForCount extends ExpressionNode
{
	private final int staticValueCount;
	private final SectionModel[] subSectionModels;
	private final int[] subSectionStaticValueCounts;

	public ExpressionNodeForCount( int _staticValueCount, SectionModel[] _subSectionModels,
			int[] _subSectionStaticValueCounts )
	{
		super();
		this.staticValueCount = _staticValueCount;
		this.subSectionModels = _subSectionModels;
		this.subSectionStaticValueCounts = _subSectionStaticValueCounts;
	}


	public final int staticValueCount()
	{
		return this.staticValueCount;
	}


	public final SectionModel[] subSectionModels()
	{
		return this.subSectionModels;
	}


	public final int[] subSectionStaticValueCounts()
	{
		return this.subSectionStaticValueCounts;
	}


	@Override
	protected int countValuesCore( Collection<ExpressionNode> _uncountables )
	{
		throw new AbstractMethodError();
	}


	@Override
	protected void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg )
	{
		_to.append( "(" ).append( this.staticValueCount );
		for (int i = 0; i < this.subSectionModels.length; i++) {
			_to.append( " + " ).append( this.subSectionModels[ i ].getName() ).append( ".size()" );
			_to.append( " * " ).append( this.subSectionStaticValueCounts[ i ] );
		}
		_to.append( ")" );
	}


	@Override
	protected ExpressionNode innerCloneWithoutArguments()
	{
		// Array sharing should be OK here.
		return new ExpressionNodeForCount( this.staticValueCount, this.subSectionModels, this.subSectionStaticValueCounts );
	}


}
