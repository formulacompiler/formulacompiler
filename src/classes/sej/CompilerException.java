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

import sej.runtime.Resettable;
import sej.runtime.SEJException;


/**
 * Defines all the errors raised by the spreadsheet compiler of SEJ.
 * 
 * @see sej.SpreadsheetCompiler
 * 
 * @author peo
 */
public class CompilerException extends SEJException
{

	public CompilerException(String _message)
	{
		super( _message );
	}

	public CompilerException(String _message, Throwable _cause)
	{
		super( _message, _cause );
	}


	/**
	 * Indicates that a required named element was not found.
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
	 * You attempted to define an element twice. For example, you give two different input
	 * definitions for the same cell.
	 * 
	 * @author peo
	 */
	public static class DuplicateDefinition extends CompilerException
	{

		public DuplicateDefinition(String _message)
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

		public NotInSection(String _eltName, String _eltAt, String _sectionName, String _sectionAt)
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

		public SectionExtentNotCovered(String _rangeAt, String _sectionName, String _sectionAt)
		{
			super( "Range "
					+ _rangeAt + " does not fully cover the variable extent of its parent section " + _sectionName + " at "
					+ _sectionAt + "." );
		}
	}


	/**
	 * You attempted to nest sections of the same variable dimension.
	 * 
	 * @author peo
	 */
	public static class SectionOrientation extends CompilerException
	{

		public SectionOrientation(String _message)
		{
			super( _message );
		}

	}


	/**
	 * You attempted to define a section that overlaps another.
	 * 
	 * @author peo
	 */
	public static class SectionOverlap extends CompilerException
	{

		public SectionOverlap(String _message)
		{
			super( _message );
		}

	}


	/**
	 * You attempted to compile a spreadsheet containing an expression with an unsupported operator.
	 * 
	 * @author peo
	 */
	public static class UnsupportedOperator extends CompilerException
	{

		public UnsupportedOperator(String _message)
		{
			super( _message );
		}

	}


	/**
	 * You attempted to compile a spreadsheet containing an unsupported expression.
	 * 
	 * @author peo
	 */
	public static class UnsupportedExpression extends CompilerException
	{

		public UnsupportedExpression(String _message)
		{
			super( _message );
		}

	}


	/**
	 * You attempted to bind a spreadsheet cell to a method with an unsupported return type, or an
	 * unsupported parameter type; or you attempted to compile a spreadsheet with an unsupported
	 * constant value in a cell referenced by the computation.
	 * 
	 * @author peo
	 */
	public static class UnsupportedDataType extends CompilerException
	{

		public UnsupportedDataType(String _message)
		{
			super( _message );
		}

	}


	/**
	 * You specified a factory or output class with no suitable constructor for SEJ to call. Note
	 * that the constructors must be {@code public}.
	 * 
	 * @author peo
	 */
	public static class ConstructorMissing extends CompilerException
	{

		public ConstructorMissing(String _message, Throwable _cause)
		{
			super( _message, _cause );
		}

	}


	/**
	 * You specified a factory type with no suitable factory method.
	 * 
	 * @author peo
	 */
	public static final class FactoryMethodMissing extends CompilerException
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


	/**
	 * You did not bind all of the abstract methods of the output type to cells.
	 * 
	 * @author peo
	 */
	public static final class MethodNotImplemented extends CompilerException
	{

		public MethodNotImplemented(Method _m)
		{
			super( "The abstract method '"
					+ _m + "' is not implemented; you should bind it to an element (cell or section)" );
		}

	}


	/**
	 * Your output type does not implement the {@link sej.runtime.Resettable} interface, as is
	 * required by computations with repeating sections.
	 * 
	 * @author peo
	 */
	public static final class MustBeResettable extends CompilerException
	{

		public MustBeResettable(Class _class)
		{
			super( "The output type "
					+ _class + " must implement the " + Resettable.class + " interface to support repeating sections." );
		}

	}


}
