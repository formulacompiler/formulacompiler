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

import java.lang.reflect.Method;


public class CompilerError extends SEJError
{

	public CompilerError(String _message)
	{
		super( _message );
	}

	public CompilerError(String _message, Throwable _cause)
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


	public static class CellRangeNotUniDimensional extends CompilerError
	{

		public CellRangeNotUniDimensional(String _message)
		{
			super( _message );
		}

	}


	public static class DuplicateDefinition extends CompilerError
	{

		public DuplicateDefinition(String _message)
		{
			super( _message );
		}

	}


	public static class NotInSection extends CompilerError
	{

		public NotInSection(String _eltName, String _eltAt, String _sectionName, String _sectionAt)
		{
			super( "Element "
					+ _eltName + " at " + _eltAt + " is not fully contained by its parent section " + _sectionName + " at "
					+ _sectionAt + "." );
		}

	}


	public static class SectionExtentNotCovered extends CompilerError
	{

		public SectionExtentNotCovered(String _rangeAt, String _sectionName, String _sectionAt)
		{
			super( "Range "
					+ _rangeAt + " does not fully cover the variable extent of its parent section " + _sectionName + " at "
					+ _sectionAt + "." );
		}
	}


	public static class SectionOrientation extends CompilerError
	{

		public SectionOrientation(String _message)
		{
			super( _message );
		}

	}


	public static class SectionOverlap extends CompilerError
	{

		public SectionOverlap(String _message)
		{
			super( _message );
		}

	}


	public static class UnsupportedOperator extends CompilerError
	{

		public UnsupportedOperator(String _message)
		{
			super( _message );
		}

	}


	public static class UnsupportedExpression extends CompilerError
	{

		public UnsupportedExpression(String _message)
		{
			super( _message );
		}

	}


	public static class UnsupportedDataType extends CompilerError
	{

		public UnsupportedDataType(String _message)
		{
			super( _message );
		}

	}


	public static class ConstructorMissing extends CompilerError
	{

		public ConstructorMissing(String _message, Throwable _cause)
		{
			super( _message, _cause );
		}

	}


	public static final class FactoryMethodMissing extends CompilerError
	{

		public FactoryMethodMissing(Class _factoryClass, Class _inputClass, Class _outputClass)
		{
			super( "The factory class '"
					+ _factoryClass + "' does not have a suitable factory method prototype '" + _outputClass + " <method>( "
					+ _inputClass + " )'" );
		}

		public FactoryMethodMissing(Class _factoryClass)
		{
			super( "The factory class '"
					+ _factoryClass + "' does not have a suitable factory method prototype '<output> <method>( <input> )'" );
		}

	}


	public static final class MethodNotImplemented extends CompilerError
	{

		public MethodNotImplemented(Method _m)
		{
			super( "The abstract method '" + _m + "' is not implemented; you should bind it to an element (cell or section)" );
		}

	}


}
