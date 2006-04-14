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


/**
 * Factory for the {@link Compiler} class. Use a compiler's {@code registerAsDefault()} method to
 * register it as the global default used by {@code newDefaultCompiler()}.
 * 
 * @see sej.engine.standard.compiler.StandardCompiler
 * 
 * @author peo
 */
public abstract class CompilerFactory
{
	private static CompilerFactory defaultFactory = null;


	/**
	 * Sets the global default factory to be used by {@code newDefaultCompiler()}. It is ususally
	 * easier to call the compiler's {@code registerAsDefault()} method instead.
	 * 
	 * @param _defaultFactory is an instance of a compiler factory.
	 */
	public static void setDefaultFactory( CompilerFactory _defaultFactory )
	{
		defaultFactory = _defaultFactory;
	}


	/**
	 * Like {@link #newDefaultCompiler(Spreadsheet, Class, Class, NumericType)} where the numeric
	 * type is set to the default, {@link NumericType#DOUBLE}.
	 */
	public static Compiler newDefaultCompiler( Spreadsheet _model, Class _inputs, Class _outputs )
	{
		return newDefaultCompiler( _model, _inputs, _outputs, NumericType.DOUBLE );
	}


	/**
	 * Returns a new compiler constructed by the default compiler factory. You can set the default
	 * using {@code setDefaultFactory}.
	 * 
	 * @param _model is the spreadsheet model to be used as input for constructing the engine. It can
	 *        be obtained loading it from an existing spreadsheet file using
	 *        {@link SpreadsheetLoader#loadFromFile(String)}, or by constructing a
	 *        {@link sej.model.Workbook} from scratch.
	 * @param _inputs is a public class or interface to which input cells can later be bound (see
	 *        {@link Compiler.Section#defineInputCell(Spreadsheet.Cell, CallFrame)}). Can be a
	 *        public static inner class.
	 * @param _outputs is a public class or interface to which output cells can later be bound (see
	 *        {@link Compiler.Section#defineOutputCell(Spreadsheet.Cell, CallFrame)}). Can be a
	 *        public static inner class.
	 * @param _numericType The numeric type to be used by all numeric computations by engines
	 *        compiled with this compiler. Must not be <code>null</code>. For financial
	 *        computations, use {@link NumericType#BIGDECIMAL} or {@link NumericType#CURRENCY}.
	 * @return The newly constructed compiler.
	 */
	public static Compiler newDefaultCompiler( Spreadsheet _model, Class _inputs, Class _outputs,
			NumericType _numericType )
	{
		if (null == defaultFactory) throw new NullPointerException( "CompilerFactory.defaultFactory is empty" );
		return defaultFactory.newCompiler( _model, _inputs, _outputs, _numericType );
	}


	protected abstract Compiler newCompiler( Spreadsheet _model, Class _inputs, Class _outputs, NumericType _numericType );


}
