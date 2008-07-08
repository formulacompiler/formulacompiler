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

package org.formulacompiler.spreadsheet;

import org.formulacompiler.compiler.CompilerException;


/**
 * Base class for all spreadsheet-related errors thrown by AFC.
 * 
 * @author peo
 */
public class SpreadsheetException extends CompilerException
{

	public SpreadsheetException( String _message )
	{
		super( _message );
	}

	public SpreadsheetException( String _message, Throwable _cause )
	{
		super( _message, _cause );
	}

	public SpreadsheetException( Throwable _cause )
	{
		super( _cause );
	}


	/**
	 * You attempted to access a cell or range by a name which is not defined.
	 * 
	 * @author peo
	 */
	public static class NameNotFound extends RuntimeException
	{

		public NameNotFound( String _message )
		{
			super( _message );
		}

		public NameNotFound( String _message, Throwable _cause )
		{
			super( _message, _cause );
		}

	}


	/**
	 * When a formula gives a range name in a place where a single cell is expected, then the range
	 * must be unidimensional. This means it is either only one row high or one column wide.
	 * 
	 * @author peo
	 */
	public static class CellRangeNotUniDimensional extends SpreadsheetException
	{

		public CellRangeNotUniDimensional( String _message )
		{
			super( _message );
		}

	}


	/**
	 * A cell formula contains a parsing error.
	 * 
	 * @author peo
	 */
	public static class UnsupportedExpression extends SpreadsheetException
	{

		public UnsupportedExpression( Throwable _cause )
		{
			super( _cause.getMessage(), _cause );
		}

	}


	/**
	 * A unsupported spreadsheet file format extension was encountered.
	 * 
	 * @author peo
	 */
	public static class UnsupportedFormat extends SpreadsheetException
	{

		public UnsupportedFormat( String _message )
		{
			super( _message );
		}

	}


	/**
	 * An internal spreadsheet model could not be saved.
	 * 
	 * @author peo
	 */
	public static class SaveError extends SpreadsheetException
	{

		public SaveError( Throwable _cause )
		{
			super( _cause.getMessage(), _cause );
		}

	}


	/**
	 * An internal spreadsheet model could not be saved.
	 * 
	 * @author peo
	 */
	public static class LoadError extends SpreadsheetException
	{

		public LoadError( String _message, Throwable _cause )
		{
			super( _message + "\n" + _cause.getMessage(), _cause );
		}

		public LoadError( String _message )
		{
			super( _message );
		}

	}


	/**
	 * You attempted to define an input or output cell or range within a section, but the cell or
	 * range is not fully contained with said section.
	 * 
	 * @author peo
	 */
	public static class NotInSection extends CompilerException
	{

		public NotInSection( String _eltName, String _eltAt, String _sectionName, String _sectionAt )
		{
			super( "Element "
					+ _eltName + " at " + _eltAt + " is not fully contained by its parent section " + _sectionName + " at "
					+ _sectionAt + "." );
		}

	}


	/**
	 * You are compiling a spreadsheet with an aggregate function over a range that overlaps, but
	 * does not exactly match the variable extent of a section.
	 * 
	 * @author peo
	 */
	public static class SectionExtentNotCovered extends CompilerException
	{

		public SectionExtentNotCovered( String _rangeAt, String _sectionName, Orientation _orientation )
		{
			super( _rangeAt
					+ " does not fully cover the " + (_orientation == Orientation.VERTICAL ? "height" : "width")
					+ " of its parent section " + _sectionName + "." );
		}

	}


	/**
	 * You attempted to aggregate a range that spans cells and/or subsections in an unsupported way.
	 * 
	 * @author peo
	 */
	public static class SectionSpan extends CompilerException
	{

		public SectionSpan( String _rangeName, String _sectionName )
		{
			super( "Range " + _rangeName + " overlaps section " + _sectionName + " in an unsupported way." );
		}

	}


	/**
	 * You attempted to define a section that overlaps another.
	 * 
	 * @author peo
	 */
	public static class SectionOverlap extends CompilerException
	{

		public SectionOverlap( String _message )
		{
			super( _message );
		}

	}


}
