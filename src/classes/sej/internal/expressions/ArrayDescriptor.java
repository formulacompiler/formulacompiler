/*
 * Copyright © 2006 by Abacus Research AG, Switzerland.
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
package sej.internal.expressions;

import java.io.IOException;

import sej.describable.AbstractDescribable;
import sej.describable.DescriptionBuilder;

public class ArrayDescriptor extends AbstractDescribable
{
	public static final int DYNAMIC = Integer.MAX_VALUE;

	private final int numberOfSheets;
	private final int numberOfRows;
	private final int numberOfColumns;


	public ArrayDescriptor(int _numberOfSheets, int _numberOfRows, int _numberOfColumns)
	{
		this.numberOfSheets = _numberOfSheets;
		this.numberOfRows = _numberOfRows;
		this.numberOfColumns = _numberOfColumns;
	}


	public ArrayDescriptor(ArrayDescriptor _template)
	{
		this.numberOfSheets = _template.numberOfSheets;
		this.numberOfRows = _template.numberOfRows;
		this.numberOfColumns = _template.numberOfColumns;
	}


	public int getNumberOfSheets()
	{
		return this.numberOfSheets;
	}


	public int getNumberOfRows()
	{
		return this.numberOfRows;
	}


	public int getNumberOfColumns()
	{
		return this.numberOfColumns;
	}


	public int getNumberOfElements()
	{
		return this.numberOfSheets * this.numberOfRows * this.numberOfColumns;
	}


	@Override
	public void describeTo( DescriptionBuilder _to ) throws IOException
	{
		_to.append( "#(" ).append( getNumberOfSheets() );
		_to.append( ',' ).append( numberOrDynamic( getNumberOfRows() ) );
		_to.append( ',' ).append( numberOrDynamic( getNumberOfColumns() ) ).append( ')' );
	}

	private final String numberOrDynamic( int _number )
	{
		return (_number == DYNAMIC) ? "*" : Integer.toString( _number );
	}


}
