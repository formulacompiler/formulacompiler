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
package org.formulacompiler.spreadsheet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.FormulaCompiler;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.runtime.EngineException;
import org.formulacompiler.runtime.ImplementationLocator;


/**
 * Static class defining factory methods for the various elements of AFC. This class is extends by
 * {@link org.formulacompiler.compiler.FormulaCompiler} which provides factory methods for the base
 * model-compiler-only elements. You normally use
 * {@link org.formulacompiler.spreadsheet.SpreadsheetCompiler#newEngineBuilder()} to get an engine
 * builder with which to compile a new engine from a spreadsheet.
 * 
 * @author peo
 */
public class SpreadsheetCompiler extends FormulaCompiler
{


	/**
	 * Returns a new instance of an engine builder, which handles the most typical use-cases for
	 * compiling a spreadsheet to an computation engine. You will rarely need any of the other
	 * factory methods of this class.
	 * 
	 * @return the new instance.
	 */
	public static EngineBuilder newEngineBuilder()
	{
		return ENGINE_BUILDER_FACTORY.newInstance();
	}

	private static final EngineBuilder.Factory ENGINE_BUILDER_FACTORY = ImplementationLocator
			.getInstance( EngineBuilder.Factory.class );


	/**
	 * Loads a spreadsheet from a file and constructs an internal representation of it.
	 * 
	 * @param _fileName is the name of the spreadsheet file to load.
	 * @return the loaded spreadsheet representation.
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws SpreadsheetException
	 */
	public static Spreadsheet loadSpreadsheet( String _fileName ) throws FileNotFoundException, IOException,
			SpreadsheetException
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
	 * @throws SpreadsheetException
	 */
	public static Spreadsheet loadSpreadsheet( File _file ) throws FileNotFoundException, IOException,
			SpreadsheetException
	{
		return loadSpreadsheet( _file.getName(), new FileInputStream( _file ) );
	}

	/**
	 * Loads a spreadsheet from a file and constructs an internal representation of it. Like
	 * {@link #loadSpreadsheet(String, InputStream, org.formulacompiler.spreadsheet.SpreadsheetLoader.Config)}
	 * with {@code _config} set to {@code null}.
	 * 
	 * @param _originalFileName is the name of the spreadsheet file to which the input stream
	 *           corresponds.
	 * @param _stream is the stream from which to load the spreadsheet.
	 * @return the loaded spreadsheet representation.
	 * 
	 * @throws IOException
	 * @throws SpreadsheetException
	 */
	public static Spreadsheet loadSpreadsheet( String _originalFileName, InputStream _stream ) throws IOException,
			SpreadsheetException
	{
		return LOADER_FACTORY.newInstance( null ).loadFrom( _originalFileName, _stream );
	}

	/**
	 * Loads a spreadsheet from a file and constructs an internal representation of it.
	 * 
	 * @param _originalFileName is the name of the spreadsheet file to which the input stream
	 *           corresponds.
	 * @param _stream is the stream from which to load the spreadsheet.
	 * @param _config configures the loader; can be set to {@code null}.
	 * @return the loaded spreadsheet representation.
	 * 
	 * @throws IOException
	 * @throws SpreadsheetException
	 */
	public static Spreadsheet loadSpreadsheet( String _originalFileName, InputStream _stream,
			SpreadsheetLoader.Config _config ) throws IOException, SpreadsheetException
	{
		return LOADER_FACTORY.newInstance( _config ).loadFrom( _originalFileName, _stream );
	}

	private static final SpreadsheetLoader.Factory LOADER_FACTORY = ImplementationLocator
			.getInstance( SpreadsheetLoader.Factory.class );


	/**
	 * Returns a new instance of a spreadsheet builder, which can be used to build a spreadsheet
	 * representation in memory from scratch.
	 * 
	 * @return the new instance.
	 */
	public static SpreadsheetBuilder newSpreadsheetBuilder()
	{
		return SHEET_BUILDER_FACTORY.newInstance();
	}

	private static final SpreadsheetBuilder.Factory SHEET_BUILDER_FACTORY = ImplementationLocator
			.getInstance( SpreadsheetBuilder.Factory.class );


	/**
	 * Saves a spreadsheet model to a new spreadsheet file. Use this primarily to build an initial
	 * spreadsheet file for users wanting to customize a particular aspect of your application using
	 * AFC. See the <a target="_top" href="{@docRoot}/../tutorial/generatesheet.htm"
	 * target="_top">tutorial</a> for details.
	 * 
	 * @param _model is the internal spreadsheet model that defines the file to be written. Use
	 *           {@link #newSpreadsheetBuilder()} to build this model.
	 * @param _outputFileName is the name of the spreadsheet file to be written. Its extension is
	 *           used to determine the file format to write (.xls for Excel, etc.).
	 * @param _templateFileNameOrNull is an optional name of a template file. If given, AFC uses it
	 *           to format the generated cells (again, see the tutorial for details).
	 * @throws IOException
	 * @throws SpreadsheetException
	 */
	public static void saveSpreadsheet( Spreadsheet _model, String _outputFileName, String _templateFileNameOrNull )
			throws IOException, SpreadsheetException
	{
		saveSpreadsheet( _model, new File( _outputFileName ), (null == _templateFileNameOrNull)? null : new File(
				_templateFileNameOrNull ) );
	}

	/**
	 * Like {@link #saveSpreadsheet(Spreadsheet, String, String)}, but taking {@code File}s instead
	 * of {@code String}s as input.
	 * 
	 * @throws IOException
	 * @throws SpreadsheetException
	 */
	public static void saveSpreadsheet( Spreadsheet _model, File _outputFile, File _templateFileOrNull )
			throws IOException, SpreadsheetException
	{
		SpreadsheetSaver.Config cfg = new SpreadsheetSaver.Config();
		cfg.spreadsheet = _model;
		cfg.typeExtension = extensionOf( _outputFile.getName() );
		cfg.outputStream = new BufferedOutputStream( new FileOutputStream( _outputFile ) );
		try {
			try {
				if (null != _templateFileOrNull) {
					cfg.templateInputStream = new BufferedInputStream( new FileInputStream( _templateFileOrNull ) );
				}
				newSpreadsheetSaver( cfg ).save();
			}
			finally {
				if (null != cfg.templateInputStream) cfg.templateInputStream.close();
			}
		}
		finally {
			cfg.outputStream.close();
		}
	}

	private static String extensionOf( String _name )
	{
		final int lastDotAt = _name.lastIndexOf( '.' );
		if (0 <= lastDotAt) {
			return _name.substring( lastDotAt );
		}
		return "";
	}

	/**
	 * Returns a new instance of a spreadsheet saver, which is used to save a spreadsheet
	 * representation to a spreadsheet file (to give users a something to start with).
	 * 
	 * @param _config contains the configuration for the new instance.
	 * @return the new instance.
	 */
	public static SpreadsheetSaver newSpreadsheetSaver( SpreadsheetSaver.Config _config )
	{
		return SAVER_FACTORY.newInstance( _config );
	}

	private static final SpreadsheetSaver.Factory SAVER_FACTORY = ImplementationLocator
			.getInstance( SpreadsheetSaver.Factory.class );


	/**
	 * Returns a new instance of a spreadsheet binder, which is used to associate input and output
	 * cells of a spreadsheet with Java methods.
	 * 
	 * @param _spreadsheet see
	 *           {@link org.formulacompiler.spreadsheet.SpreadsheetBinder.Config#spreadsheet}.
	 * @param _inputClass see
	 *           {@link org.formulacompiler.spreadsheet.SpreadsheetBinder.Config#inputClass}.
	 * @param _outputClass see
	 *           {@link org.formulacompiler.spreadsheet.SpreadsheetBinder.Config#outputClass}.
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
	 * Returns a new instance of a spreadsheet binder, which is used to associate input and output
	 * cells of a spreadsheet with Java methods.
	 * 
	 * @param _config contains the configuration data for the new instance.
	 * @return the new instance.
	 * 
	 * @see #newSpreadsheetByNameBinder(org.formulacompiler.spreadsheet.SpreadsheetByNameBinder.Config)
	 */
	public static SpreadsheetBinder newSpreadsheetBinder( SpreadsheetBinder.Config _config )
	{
		return BINDER_FACTORY.newInstance( _config );
	}

	private static final SpreadsheetBinder.Factory BINDER_FACTORY = ImplementationLocator
			.getInstance( SpreadsheetBinder.Factory.class );


	/**
	 * Returns a new instance of a spreadsheet binder utility class, which uses reflection and cell
	 * names defined in the spreadsheet to associate cells with methods.
	 * 
	 * @param _binder see
	 *           {@link org.formulacompiler.spreadsheet.SpreadsheetByNameBinder.Config#binder}.
	 * @return the new instance.
	 */
	public static SpreadsheetByNameBinder newSpreadsheetByNameBinder( SpreadsheetBinder _binder )
	{
		SpreadsheetByNameBinder.Config cfg = new SpreadsheetByNameBinder.Config();
		cfg.binder = _binder;
		return newSpreadsheetByNameBinder( cfg );
	}

	/**
	 * Returns a new instance of a spreadsheet binder utility class, which uses reflection and cell
	 * names defined in the spreadsheet to associate cells with methods.
	 * 
	 * @param _config contains the configuration for the new instance.
	 * @return the new instance.
	 */
	public static SpreadsheetByNameBinder newSpreadsheetByNameBinder( SpreadsheetByNameBinder.Config _config )
	{
		return BY_NAME_BINDER_FACTORY.newInstance( _config );
	}

	private static final SpreadsheetByNameBinder.Factory BY_NAME_BINDER_FACTORY = ImplementationLocator
			.getInstance( SpreadsheetByNameBinder.Factory.class );


	/**
	 * Returns a new instance of a spreadsheet cell name creator utility class, which names
	 * spreadsheet cells according to their row titles. This is typically used before applying a
	 * {@link org.formulacompiler.spreadsheet.SpreadsheetByNameBinder}.
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
	 * Returns a new instance of a spreadsheet cell name creator utility class, which names
	 * spreadsheet cells according to their row titles. This is typically used before applying a
	 * {@link org.formulacompiler.spreadsheet.SpreadsheetByNameBinder}.
	 * 
	 * @param _config contains the configuration for the new instance.
	 * @return the new instance.
	 */
	private static SpreadsheetNameCreator newSpreadsheetCellNameCreator( SpreadsheetNameCreator.Config _config )
	{
		return NAME_CREATOR_FACTORY.newInstance( _config );
	}

	private static final SpreadsheetNameCreator.Factory NAME_CREATOR_FACTORY = ImplementationLocator
			.getInstance( SpreadsheetNameCreator.Factory.class );


	/**
	 * Returns a new instance of a spreadsheet compiler, which is used to compile a spreadsheet
	 * representation, together with a binding associating cells with input and output methods, to an
	 * excecutable computation engine.
	 * 
	 * @param _binding see
	 *           {@link org.formulacompiler.spreadsheet.SpreadsheetToEngineCompiler.Config#binding}.
	 * @param _numericType see
	 *           {@link org.formulacompiler.spreadsheet.SpreadsheetToEngineCompiler.Config#numericType}.
	 * @param _factoryClass see
	 *           {@link org.formulacompiler.spreadsheet.SpreadsheetToEngineCompiler.Config#factoryClass}.
	 * @param _factoryMethod see
	 *           {@link org.formulacompiler.spreadsheet.SpreadsheetToEngineCompiler.Config#factoryMethod}.
	 * @param _parentClassLoader see
	 *           {@link org.formulacompiler.spreadsheet.SpreadsheetToEngineCompiler.Config#parentClassLoader}.
	 * @return the new instance.
	 * 
	 * @throws CompilerException
	 * @throws EngineException
	 */
	public static SaveableEngine compileEngine( SpreadsheetBinding _binding, NumericType _numericType,
			Class _factoryClass, Method _factoryMethod, ClassLoader _parentClassLoader ) throws CompilerException,
			EngineException
	{
		final SpreadsheetToEngineCompiler.Config cfg = new SpreadsheetToEngineCompiler.Config();
		cfg.binding = _binding;
		cfg.numericType = _numericType;
		cfg.factoryClass = _factoryClass;
		cfg.factoryMethod = _factoryMethod;
		cfg.parentClassLoader = _parentClassLoader;
		return newSpreadsheetCompiler( cfg ).compile();
	}

	/**
	 * Same as {@link #compileEngine(SpreadsheetBinding, NumericType, Class, Method, ClassLoader)},
	 * leaving the factory class and method unspecified.
	 */
	public static SaveableEngine compileEngine( SpreadsheetBinding _binding, NumericType _numericType )
			throws CompilerException, EngineException
	{
		final SpreadsheetToEngineCompiler.Config cfg = new SpreadsheetToEngineCompiler.Config();
		cfg.binding = _binding;
		cfg.numericType = _numericType;
		return newSpreadsheetCompiler( cfg ).compile();
	}

	/**
	 * Returns a new instance of a spreadsheet compiler, which is used to compile a spreadsheet
	 * representation, together with a binding associating cells with input and output methods, to an
	 * excecutable computation engine.
	 * 
	 * @param _config contains the configuration for the new instance.
	 * @return the new instance.
	 */
	public static SpreadsheetToEngineCompiler newSpreadsheetCompiler( SpreadsheetToEngineCompiler.Config _config )
	{
		return COMPILER_FACTORY.newInstance( _config );
	}

	private static final SpreadsheetToEngineCompiler.Factory COMPILER_FACTORY = ImplementationLocator
			.getInstance( SpreadsheetToEngineCompiler.Factory.class );


}
