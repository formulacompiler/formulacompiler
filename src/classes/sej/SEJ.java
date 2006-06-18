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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;

import sej.internal.NumericTypeImpl;
import sej.internal.Util;
import sej.internal.bytecode.compiler.ByteCodeEngineCompiler;
import sej.internal.spreadsheet.binder.SpreadsheetBinderImpl;
import sej.internal.spreadsheet.builder.SpreadsheetBuilderImpl;
import sej.internal.spreadsheet.compiler.SpreadsheetCompilerImpl;
import sej.internal.spreadsheet.loader.AnyFormatSpreadsheetLoader;
import sej.internal.util.EngineBuilderImpl;
import sej.internal.util.SpreadsheetByNameBinderImpl;
import sej.internal.util.SpreadsheetNameCreatorImpl;
import sej.runtime.EngineError;
import sej.runtime.SEJRuntime;


/**
 * Static class defining factory methods for the various elements of SEJ. This class is extends by
 * {@link sej.runtime.SEJRuntime} which provides factory methods for the run-time-only elements. You
 * normally use {@link #newEngineBuilder()} to get an engine builder with which to compile a new
 * engine from a spreadsheet.
 * 
 * @author peo
 */
public class SEJ extends SEJRuntime
{

	static {
		ByteCodeEngineCompiler.register();
	}


	/**
	 * Returns a new instance of an engine builder, which handles the most typical use-cases for
	 * compiling a spreadsheet to an computation engine. You will rarely need any of the other
	 * factory methods of {@code SEJ}.
	 * 
	 * @return the new instance.
	 */
	public static EngineBuilder newEngineBuilder()
	{
		return new EngineBuilderImpl();
	}


	/**
	 * Loads a spreadsheet from a file and constructs an internal representation of it.
	 * 
	 * @param _fileName is the name of the spreadsheet file to load.
	 * @return the loaded spreadsheet representation.
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws SpreadsheetError
	 */
	public static Spreadsheet loadSpreadsheet( String _fileName ) throws FileNotFoundException, IOException,
			SpreadsheetError
	{
		return loadSpreadsheet( new File( _fileName ) );
	}

	/**
	 * Loads a spreadsheet from a file and constructs an internal representation of it.
	 * 
	 * @param _file is the file to load.
	 * @return the loaded spreadsheet representation.
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws SpreadsheetError
	 */
	public static Spreadsheet loadSpreadsheet( File _file ) throws FileNotFoundException, IOException, SpreadsheetError
	{
		return loadSpreadsheet( _file.getName(), new FileInputStream( _file ) );
	}

	/**
	 * Loads a spreadsheet from a file and constructs an internal representation of it.
	 * 
	 * @param _originalFileName is the name of the spreadsheet file to which the input stream
	 *           corresponds.
	 * @param _stream is the stream from which to load the spreadsheet.
	 * @return the loaded spreadsheet representation.
	 * 
	 * @throws IOException
	 * @throws SpreadsheetError
	 */
	public static Spreadsheet loadSpreadsheet( String _originalFileName, InputStream _stream ) throws IOException,
			SpreadsheetError
	{
		return AnyFormatSpreadsheetLoader.loadSpreadsheet( _originalFileName, _stream );
	}


	/**
	 * Returns a new instance of a spreadsheet builder, which can be used to build a spreadsheet
	 * representation in memory from scratch.
	 * 
	 * @return the new instance.
	 */
	public static SpreadsheetBuilder newSpreadsheetBuilder()
	{
		return new SpreadsheetBuilderImpl();
	}


	/**
	 * Returns a new instance of a spreadsheet binder, which is used to associate input and output
	 * cells of a spreadsheet with Java methods.
	 * 
	 * @param _config contains the configuration data for the new instance.
	 * @return the new instance.
	 * 
	 * @see #newSpreadsheetByNameBinder(sej.SpreadsheetByNameBinder.Config)
	 */
	public static SpreadsheetBinder newSpreadsheetBinder( SpreadsheetBinder.Config _config )
	{
		return new SpreadsheetBinderImpl( _config );
	}

	/**
	 * Returns a new instance of a spreadsheet binder, which is used to associate input and output
	 * cells of a spreadsheet with Java methods.
	 * 
	 * @param _spreadsheet see {@link sej.SpreadsheetBinder.Config#spreadsheet}.
	 * @param _inputClass see {@link sej.SpreadsheetBinder.Config#inputClass}.
	 * @param _outputClass see {@link sej.SpreadsheetBinder.Config#outputClass}.
	 * @return the new instance.
	 * 
	 * @see #newSpreadsheetByNameBinder(SpreadsheetBinder)
	 */
	public static SpreadsheetBinder newSpreadsheetBinder( Spreadsheet _spreadsheet, Class _inputClass, Class _outputClass )
	{
		SpreadsheetBinder.Config cfg = new SpreadsheetBinder.Config();
		cfg.spreadsheet = _spreadsheet;
		cfg.inputClass = _inputClass;
		cfg.outputClass = _outputClass;
		return newSpreadsheetBinder( cfg );
	}


	/**
	 * Returns a new instance of a spreadsheet binder utility class, which uses reflection and cell
	 * names defined in the spreadsheet to associate cells with methods.
	 * 
	 * @param _config contains the configuration for the new instance.
	 * @return the new instance.
	 */
	private static SpreadsheetByNameBinder newSpreadsheetByNameBinder( SpreadsheetByNameBinder.Config _config )
	{
		return new SpreadsheetByNameBinderImpl( _config );
	}

	/**
	 * Returns a new instance of a spreadsheet binder utility class, which uses reflection and cell
	 * names defined in the spreadsheet to associate cells with methods.
	 * 
	 * @param _binder see {@link sej.SpreadsheetByNameBinder.Config#binder}.
	 * @return the new instance.
	 */
	public static SpreadsheetByNameBinder newSpreadsheetByNameBinder( SpreadsheetBinder _binder )
	{
		SpreadsheetByNameBinder.Config cfg = new SpreadsheetByNameBinder.Config();
		cfg.binder = _binder;
		return newSpreadsheetByNameBinder( cfg );
	}


	/**
	 * Returns a new instance of a spreadsheet cell name creator utility class, which names
	 * spreadsheet cells according to their row titles. This is typically used before applying a
	 * {@link sej.SpreadsheetByNameBinder}.
	 * 
	 * @param _config contains the configuration for the new instance.
	 * @return the new instance.
	 */
	private static SpreadsheetNameCreator newSpreadsheetCellNameCreator( SpreadsheetNameCreator.Config _config )
	{
		return new SpreadsheetNameCreatorImpl( _config );
	}

	/**
	 * Returns a new instance of a spreadsheet cell name creator utility class, which names
	 * spreadsheet cells according to their row titles. This is typically used before applying a
	 * {@link sej.SpreadsheetByNameBinder}.
	 * 
	 * @param _sheet is the single sheet of a spreadsheet representation in which to name cells.
	 * @return the new instance.
	 */
	public static SpreadsheetNameCreator newSpreadsheetCellNameCreator( Spreadsheet.Sheet _sheet )
	{
		SpreadsheetNameCreator.Config cfg = new SpreadsheetNameCreator.Config();
		cfg.sheet = _sheet;
		return newSpreadsheetCellNameCreator( cfg );
	}


	/**
	 * Returns a new instance of a spreadsheet compiler, which is used to compile a spreadsheet
	 * representation, together with a binding associating cells with input and output methods, to an
	 * excecutable computation engine.
	 * 
	 * @param _config contains the configuration for the new instance.
	 * @return the new instance.
	 */
	public static SpreadsheetCompiler newSpreadsheetCompiler( SpreadsheetCompiler.Config _config )
	{
		return new SpreadsheetCompilerImpl( _config );
	}

	/**
	 * Returns a new instance of a spreadsheet compiler, which is used to compile a spreadsheet
	 * representation, together with a binding associating cells with input and output methods, to an
	 * excecutable computation engine.
	 * 
	 * @param _binding see {@link sej.SpreadsheetCompiler.Config#binding}.
	 * @param _numericType see {@link sej.SpreadsheetCompiler.Config#numericType}.
	 * @param _factoryClass see {@link sej.SpreadsheetCompiler.Config#factoryClass}.
	 * @param _factoryMethod see {@link sej.SpreadsheetCompiler.Config#factoryMethod}.
	 * @return the new instance.
	 * 
	 * @throws CompilerError
	 * @throws EngineError
	 */
	public static SaveableEngine compileEngine( SpreadsheetBinding _binding, NumericType _numericType,
			Class _factoryClass, Method _factoryMethod ) throws CompilerError, EngineError
	{
		final SpreadsheetCompiler.Config cfg = new SpreadsheetCompiler.Config();
		cfg.binding = _binding;
		cfg.numericType = _numericType;
		cfg.factoryClass = _factoryClass;
		cfg.factoryMethod = _factoryMethod;
		return newSpreadsheetCompiler( cfg ).compile();
	}

	/**
	 * Same as {@link #compileEngine(SpreadsheetBinding, NumericType, Class, Method)}, leaving the
	 * factory class and method unspecified.
	 */
	public static SaveableEngine compileEngine( SpreadsheetBinding _binding, NumericType _numericType )
			throws CompilerError, EngineError
	{
		final SpreadsheetCompiler.Config cfg = new SpreadsheetCompiler.Config();
		cfg.binding = _binding;
		cfg.numericType = _numericType;
		return newSpreadsheetCompiler( cfg ).compile();
	}


	/**
	 * Returns the numeric type instance with the specified attributes.
	 * 
	 * @param _valueType must be either {@code Double.TYPE}, {@code Long.TYPE}, or
	 *           {@code BigDecimal.class}.
	 * @param _scale defines the number of decimal places after the point to which the base type is
	 *           scaled. Only supported for Long and BigDecimal.
	 * @param _roundingMode defines the rounding mode to use when establishing the scale. Only
	 *           supported for BigDecimal.
	 * @return the instance.
	 */
	public static NumericType getNumericType( Class _valueType, int _scale, int _roundingMode )
	{
		return NumericTypeImpl.getInstance( _valueType, _scale, _roundingMode );
	}

	/**
	 * Same as {@link #getNumericType(Class, int, int)} with an undefined scale and truncating
	 * results.
	 */
	public static NumericType getNumericType( Class _valueType )
	{
		return getNumericType( _valueType, NumericType.UNDEFINED_SCALE, BigDecimal.ROUND_DOWN );
	}

	/**
	 * Same as {@link #getNumericType(Class, int, int)} and truncating results.
	 */
	public static NumericType getNumericType( Class _valueType, int _scale )
	{
		return getNumericType( _valueType, _scale, BigDecimal.ROUND_DOWN );
	}


	// ------------------------------------------------ Util access for config records


	static void validateIsAccessible( Class _class, String _role )
	{
		Util.validateIsAccessible( _class, _role );
	}

	static void validateIsAccessible( Method _method, String _role )
	{
		Util.validateIsAccessible( _method, _role );
	}

	static void validateIsImplementable( Class _class, String _role )
	{
		Util.validateIsImplementable( _class, _role );
	}

	static void validateIsImplementable( Method _method, String _role )
	{
		Util.validateIsImplementable( _method, _role );
	}

	static void validateCallable( Class _class, Method _method )
	{
		Util.validateCallable( _class, _method );
	}

	static void validateFactory( Class _factoryClass, Method _factoryMethod, Class _inputClass, Class _outputClass )
	{
		Util.validateFactory( _factoryClass, _factoryMethod, _inputClass, _outputClass );
	}

}
