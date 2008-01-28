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

import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.internal.YamlBuilder;
import org.formulacompiler.compiler.internal.expressions.DataType;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.TypedResult;


public class CellModel extends ElementModel implements TypedResult
{
	private Object constantValue;
	private ExpressionNode expression;
	private int maxFractionalDigits = NumericType.UNLIMITED_FRACTIONAL_DIGITS;
	private int referenceCount = 0;
	private DataType dataType;


	public CellModel( SectionModel _section, String _name )
	{
		super( _section, _name );
		_section.getCells().add( this );
	}


	@Override
	public void setName( String _name )
	{
		super.setName( _name );
	}


	public Object getConstantValue()
	{
		return this.constantValue;
	}


	public void setConstantValue( Object _constantValue )
	{
		this.constantValue = _constantValue;
	}


	public boolean isConstant()
	{
		return hasConstantValue();
	}

	public boolean hasConstantValue()
	{
		return (this.expression == null) && DataType.isValueConstant( getConstantValue() );
	}


	public ExpressionNode getExpression()
	{
		return this.expression;
	}


	public void setExpression( ExpressionNode _expression )
	{
		this.expression = _expression;
	}


	public int getMaxFractionalDigits()
	{
		return this.maxFractionalDigits;
	}


	public void setMaxFractionalDigits( int _maxFractionalDigits )
	{
		this.maxFractionalDigits = _maxFractionalDigits;
	}


	public int getReferenceCount()
	{
		return this.referenceCount;
	}


	public void addReference()
	{
		this.referenceCount++;
	}


	void removeReference()
	{
		this.referenceCount--;
	}


	public boolean isCachingCandidate()
	{
		return (getReferenceCount() > 1 || isInput() || isOutput());
	}


	public DataType getDataType()
	{
		return this.dataType;
	}

	public void setDataType( DataType _dataType )
	{
		this.dataType = _dataType;
	}


	@Override
	public void yamlTo( YamlBuilder _to )
	{
		_to.nv( "name", getName() );
		if (isInput()) _to.ln( "input" ).l( "calls", getCallChainToCall() );
		if (isOutput()) _to.ln( "output" ).l( "implements", (Object[]) getCallsToImplement() );
		_to.nv( "value", this.constantValue );
		_to.nv( "expr", this.expression );
		if (NumericType.UNLIMITED_FRACTIONAL_DIGITS > this.maxFractionalDigits)
			_to.nv( "maxFractionalDigits", this.maxFractionalDigits );
		_to.nv( "refCount", this.referenceCount );
		_to.nv( "type", this.dataType );
	}

}
