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
package sej;

import sej.runtime.SEJException;


/**
 * Base class for all spreadsheet-related errors thrown by SEJ.
 * 
 * @author peo
 */
public class SpreadsheetException extends SEJException
{

	public SpreadsheetException(String _message)
	{
		super( _message );
	}

	public SpreadsheetException(String _message, Throwable _cause)
	{
		super( _message, _cause );
	}

	public SpreadsheetException(Throwable _cause)
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

		public NameNotFound(String _message)
		{
			super( _message );
		}

		public NameNotFound(String _message, Throwable _cause)
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

		public CellRangeNotUniDimensional(String _message)
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

		public UnsupportedExpression(Throwable _cause)
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

		public UnsupportedFormat(String _message)
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

		public SaveError(Throwable _cause)
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

		public LoadError(Throwable _cause)
		{
			super( _cause.getMessage(), _cause );
		}

	}


}
