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

import java.io.IOException;

import org.formulacompiler.describable.AbstractDescribable;
import org.formulacompiler.describable.DescriptionBuilder;


public final class ArrayDescriptor extends AbstractDescribable
{
	public static final int DYNAMIC = Integer.MAX_VALUE;
	private static final int UNSPECIFIED = Integer.MIN_VALUE;

	private final Point origin;
	private final Point extent;

	public ArrayDescriptor( Point _origin, Point _extent )
	{
		this.origin = _origin;
		this.extent = _extent;
	}

	public ArrayDescriptor( int _oSheet, int _oRow, int _oCol, int _nSheet, int _nRow, int _nCol )
	{
		this( new Point( _oSheet, _oRow, _oCol ), new Point( _nSheet, _nRow, _nCol ) );
	}

	public ArrayDescriptor( int _nSheet, int _nRow, int _nCol )
	{
		this( UNSPECIFIED, UNSPECIFIED, UNSPECIFIED, _nSheet, _nRow, _nCol );
	}

	public ArrayDescriptor( ArrayDescriptor _template )
	{
		this( _template.origin, _template.extent );
	}

	public ArrayDescriptor( ArrayDescriptor _template, Point _originDelta, Point _extentDelta )
	{
		this( _template.origin.moveBy( _originDelta ), _template.extent.moveBy( _extentDelta ) );
	}

	public ArrayDescriptor( ArrayDescriptor _template, int _oRowDelta, int _oColDelta, int _nRowDelta, int _nColDelta )
	{
		this( _template, new Point( 0, _oRowDelta, _oColDelta ), new Point( 0, _nRowDelta, _nColDelta ) );
	}


	public Point origin()
	{
		return this.origin;
	}

	public Point extent()
	{
		return this.extent;
	}


	public int numberOfSheets()
	{
		return extent().sheet();
	}

	public int numberOfRows()
	{
		return extent().row();
	}

	public int numberOfColumns()
	{
		return extent().col();
	}

	public int numberOfElements()
	{
		return numberOfSheets() * numberOfRows() * numberOfColumns();
	}


	public String name()
	{
		return origin().name() + "_" + extent().name();
	}


	@Override
	public void describeTo( DescriptionBuilder _to ) throws IOException
	{
		if (origin().sheet() < 0) {
			// This is to remain compatible with older tests.
			_to.append( "#(" ).append( numberOfSheets() );
			_to.append( ',' ).append( numberOrDynamic( numberOfRows() ) );
			_to.append( ',' ).append( numberOrDynamic( numberOfColumns() ) ).append( ')' );
		}
		else {
			_to.append( "#(" ).append( name() ).append( ')' );
		}
	}


	public static final class Point
	{
		private final int sheet;
		private final int row;
		private final int col;

		public Point( int _sheet, int _row, int _col )
		{
			this.sheet = _sheet;
			this.row = _row;
			this.col = _col;
		}

		public final int sheet()
		{
			return this.sheet;
		}

		public final int row()
		{
			return this.row;
		}

		public final int col()
		{
			return this.col;
		}

		public Point moveBy( Point _delta )
		{
			return new Point( incBy( this.sheet, _delta.sheet() ), incBy( this.row, _delta.row() ), incBy( this.col,
					_delta.col() ) );
		}

		private static int incBy( int _n, int _delta )
		{
			return (_n == DYNAMIC)? _n : _n + _delta;
		}

		public String name()
		{
			return "s" + sheet() + "r" + numberOrDynamic( row() ) + "c" + numberOrDynamic( col() );
		}

	}

	private static final String numberOrDynamic( int _number )
	{
		return (_number == DYNAMIC)? "*" : Integer.toString( _number );
	}

}
