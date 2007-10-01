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
package org.formulacompiler.compiler;

import java.lang.reflect.Method;

import org.formulacompiler.runtime.FormulaCompilerException;


/**
 * Defines all the errors raised by the functional model compiler of AFC.
 * 
 * @see org.formulacompiler.compiler.FormulaCompiler
 * 
 * @author peo
 */
public class CompilerException extends FormulaCompilerException
{

	public CompilerException( String _message )
	{
		super( _message );
	}

	public CompilerException( String _message, Throwable _cause )
	{
		super( _message, _cause );
	}

	public CompilerException( Throwable _cause )
	{
		super( _cause );
	}

	public CompilerException()
	{
		super();
	}


	/**
	 * Indicates that a required named element was not found.
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
	 * You attempted to define an element twice. For example, you give two different input
	 * definitions for the same cell.
	 * 
	 * @author peo
	 */
	public static class DuplicateDefinition extends CompilerException
	{

		public DuplicateDefinition( String _message )
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

		public UnsupportedOperator( String _message )
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

		public UnsupportedExpression( String _message )
		{
			super( _message );
		}

		public UnsupportedExpression( Throwable _cause )
		{
			super( _cause.getMessage(), _cause );
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

		public UnsupportedDataType( String _message )
		{
			super( _message );
		}

	}


	/**
	 * You specified a factory or output class with no suitable constructor for AFC to call. Note
	 * that the constructors must be {@code public}.
	 * 
	 * @author peo
	 */
	public static class ConstructorMissing extends CompilerException
	{

		public ConstructorMissing( String _message, Throwable _cause )
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

		public FactoryMethodMissing( Class _factoryClass, Class _inputClass, Class _outputClass )
		{
			super( "The factory class '"
					+ _factoryClass + "' does not have a suitable factory method prototype '" + _outputClass + " <method>( "
					+ _inputClass + " )'" );
		}

		public FactoryMethodMissing( Class _factoryClass )
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

		public MethodNotImplemented( Method _m )
		{
			super( "The abstract method '"
					+ _m + "' is not implemented; you should bind it to an element (cell or section)" );
		}

	}


	/**
	 * An expression references an array (range) without aggregating it. For example,
	 * {@code A3 = A1:A2} is illegal. This error could also be caused by an outer reference to a cell
	 * within a repeating section.
	 * 
	 * @author peo
	 * 
	 * @see ReferenceToInnerCellNotAggregated
	 */
	public static final class ReferenceToArrayNotAggregated extends CompilerException
	{

		public ReferenceToArrayNotAggregated()
		{
			super( "Cannot reference an array without aggregating it." );
		}

	}


	/**
	 * An outer cell references an inner cell of a section without aggregating it. For example,
	 * {@code A2 = A1} is illegal if A1 is within a section, but A2 is not. The aggregation
	 * {@code A2 = sum(A1)} would be legal.
	 * 
	 * @author peo
	 */
	public static final class ReferenceToInnerCellNotAggregated extends CompilerException
	{

		public ReferenceToInnerCellNotAggregated()
		{
			super( "Cannot reference an inner cell of a section from an outer cell without aggregating it." );
		}

	}


	/**
	 * A cell references another cell which is itside the first cell's section, but contained within
	 * a sibling subsection of their common parent section. This is not yet supported, even in
	 * aggregations. You should instead reference an outer aggregation over the sibling section.
	 * 
	 * @author peo
	 */
	public static final class ReferenceToOuterInnerCell extends CompilerException
	{

		public ReferenceToOuterInnerCell()
		{
			super( "Cannot reference a cell in a sibling section of the current one." );
		}

	}


	/**
	 * The aggregator function you used does not support repeating sections of the given orientation.
	 * Typically, database aggregators don't accept horizontally repeating sections.
	 * 
	 * @author peo
	 */
	public static class SectionOrientation extends CompilerException
	{

		public SectionOrientation( String _message )
		{
			super( _message );
		}

	}


	/**
	 * The formula you tried to parse contains an error or an unsupported element.
	 * 
	 * @author peo
	 */
	public static class UnsupportedExpressionSource extends CompilerException
	{

		private static final int CONTEXT_CHARS = 100;

		private static String addPositionInfoTo( String _message, String _source, int _atPosition )
		{
			int at = _atPosition;
			if (at < 0) at = 0;
			if (at >= _source.length()) at = _source.length() - 1;
			final String leadingEllipsis = (at > CONTEXT_CHARS)? "..." : "";
			final String sourceBeforeError = _source.substring( Math.max( at - CONTEXT_CHARS, 0 ), at );
			final String sourceAfterError = (at < _source.length())? _source.substring( at, Math.min( at + CONTEXT_CHARS,
					_source.length() ) ) : "";
			final String finalEllipsis = (at + CONTEXT_CHARS < _source.length())? "..." : "";

			StringBuilder result = new StringBuilder( _message );
			result.append( " in expression " ).append( leadingEllipsis ).append( sourceBeforeError ).append( " <<? " )
					.append( sourceAfterError ).append( finalEllipsis ).append( "; error location indicated by <<?." );
			return result.toString();
		}


		public UnsupportedExpressionSource( Throwable _originalError, String _source, int _atPosition )
		{
			super( addPositionInfoTo( _originalError.getMessage(), _source, _atPosition ), _originalError );
		}

		public UnsupportedExpressionSource( String _message, String _source, int _atPosition )
		{
			super( addPositionInfoTo( _message, _source, _atPosition ) );
		}

	}


	/**
	 * You aggregated two parallel vectors (using {@code COVAR}, for example), but they cross
	 * different subsections, or cross them differently.
	 * 
	 * @author peo
	 */
	public static class ParallelVectorsSpanDifferentSubSections extends CompilerException
	{

		public ParallelVectorsSpanDifferentSubSections( String _message )
		{
			super( _message );
		}

	}
	
	
}
