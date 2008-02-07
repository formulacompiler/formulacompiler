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
