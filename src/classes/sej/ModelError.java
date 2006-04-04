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

public class ModelError extends Exception
{

	public ModelError(String _message)
	{
		super( _message );
	}

	public ModelError(String _message, Throwable _cause)
	{
		super( _message, _cause );
	}


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


	public static class CellRangeNotUniDimensional extends ModelError
	{

		public CellRangeNotUniDimensional(String _message)
		{
			super( _message );
		}

	}


	public static class DuplicateDefinition extends ModelError
	{

		public DuplicateDefinition(String _message)
		{
			super( _message );
		}

	}


	public static class NotInSection extends ModelError
	{

		public NotInSection(String _eltName, String _eltAt, String _sectionName, String _sectionAt)
		{
			super( "Element "
					+ _eltName + " at " + _eltAt + " is not fully contained by its parent section " + _sectionName + " at "
					+ _sectionAt + "." );
		}

	}


	public static class SectionExtentNotCovered extends ModelError
	{

		public SectionExtentNotCovered(String _rangeAt, String _sectionName, String _sectionAt)
		{
			super( "Range "
					+ _rangeAt + " does not fully cover the variable extent of its parent section " + _sectionName + " at "
					+ _sectionAt + "." );
		}
	}


	public static class SectionOrientation extends ModelError
	{

		public SectionOrientation(String _message)
		{
			super( _message );
		}

	}


	public static class SectionOverlap extends ModelError
	{

		public SectionOverlap(String _message)
		{
			super( _message );
		}

	}


	public static class UnsupportedOperator extends ModelError
	{

		public UnsupportedOperator(String _message)
		{
			super( _message );
		}

	}


	public static class UnsupportedExpression extends ModelError
	{

		public UnsupportedExpression(String _message)
		{
			super( _message );
		}

	}


	public static class UnsupportedDataType extends ModelError
	{

		public UnsupportedDataType(String _message)
		{
			super( _message );
		}

	}


	public static class ConstructorMissing extends ModelError
	{

		public ConstructorMissing(String _message, Throwable _cause)
		{
			super( _message, _cause );
		}

	}


}
