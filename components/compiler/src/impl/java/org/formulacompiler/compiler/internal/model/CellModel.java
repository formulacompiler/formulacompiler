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

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.formulacompiler.compiler.internal.expressions.DataType;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.describable.DescriptionBuilder;


public class CellModel extends ElementModel
{
	public static final int UNLIMITED = 10000;

	private Object constantValue;
	private ExpressionNode expression;
	private int maxFractionalDigits = UNLIMITED;
	private int referenceCount = 0;
	private DataType dataType;


	public CellModel(SectionModel _section, String _name)
	{
		super( _section, _name );
		_section.getCells().add( this );
	}


	public Object getConstantValue()
	{
		return this.constantValue;
	}


	public void setConstantValue( Object _constantValue )
	{
		this.constantValue = _constantValue;
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


	public void applyNumberFormat( NumberFormat _numberFormat )
	{
		if (null == _numberFormat) {
			this.maxFractionalDigits = UNLIMITED;
		}
		else {
			int maxFrac = _numberFormat.getMaximumFractionDigits();
			if (_numberFormat instanceof DecimalFormat) {
				DecimalFormat decFormat = (DecimalFormat) _numberFormat;
				int decMult = decFormat.getMultiplier();
				switch (decMult) {
				case 10:
					maxFrac += 1;
					break;
				case 100:
					maxFrac += 2;
					break;
				}
			}
			this.maxFractionalDigits = maxFrac;
		}
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
	public void describeTo( DescriptionBuilder _to ) throws IOException
	{
		_to.append( "<cell id=\"" );
		_to.append( toString() );
		_to.append( "\">" );
		_to.newLine();
		_to.indent();

		if (isInput()) {
			_to.append( "<input call=\"" );
			_to.append( getCallChainToCall().toString() );
			_to.appendLine( "/>" );
		}
		if (isOutput()) _to.appendLine( "<output/>" );

		if (null != this.constantValue) {
			_to.append( "<value>" );
			_to.append( this.constantValue );
			_to.appendLine( "</value>" );
		}
		if (null != this.expression) {
			_to.append( "<expr>" );
			this.expression.describeTo( _to );
			_to.appendLine( "</expr>" );
		}
		if (UNLIMITED > this.maxFractionalDigits) {
			_to.append( "<format maxFractionalDigits=\"" );
			_to.append( this.maxFractionalDigits );
			_to.appendLine( "\" />" );
		}
		if (0 < this.referenceCount) {
			_to.append( "<refs count=\"" );
			_to.append( this.referenceCount );
			_to.appendLine( "\" />" );
		}
		if (null != this.dataType) {
			_to.append( "<type name=\"" );
			_to.append( this.dataType );
			_to.appendLine( "\" />" );
		}

		_to.outdent();
		_to.appendLine( "</cell>" );
	}


}
