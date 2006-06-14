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
import sej.internal.spreadsheet.compiler.SpreadsheetCompilerImpl;
import sej.internal.spreadsheet.loader.AnyFormatSpreadsheetLoader;
import sej.internal.util.EngineBuilderImpl;
import sej.internal.util.SpreadsheetByNameBinderImpl;
import sej.internal.util.SpreadsheetNameCreatorImpl;
import sej.runtime.EngineError;
import sej.runtime.SEJRuntime;

public class SEJ extends SEJRuntime
{

	static {
		ByteCodeEngineCompiler.register();
	}
	
	
	public static EngineBuilder newEngineBuilder()
	{
		return new EngineBuilderImpl();
	}

	
	public static Spreadsheet loadSpreadsheet( String _fileName ) throws FileNotFoundException, IOException,
			SpreadsheetError
	{
		return loadSpreadsheet( new File( _fileName ) );
	}

	public static Spreadsheet loadSpreadsheet( File _file ) throws FileNotFoundException, IOException, SpreadsheetError
	{
		return loadSpreadsheet( _file.getName(), new FileInputStream( _file ) );
	}

	public static Spreadsheet loadSpreadsheet( String _originalFileName, InputStream _stream ) throws IOException,
			SpreadsheetError
	{
		return AnyFormatSpreadsheetLoader.loadSpreadsheet( _originalFileName, _stream );
	}

	public static SpreadsheetBuilder newSpreadsheetBuilder()
	{
		return null;
	}

	
	public static SpreadsheetBinder newSpreadsheetBinder( SpreadsheetBinder.Config _config )
	{
		return new SpreadsheetBinderImpl( _config );
	}

	public static SpreadsheetBinder newSpreadsheetBinder( Spreadsheet _spreadsheet, Class _inputClass, Class _outputClass )
	{
		SpreadsheetBinder.Config cfg = new SpreadsheetBinder.Config();
		cfg.spreadsheet = _spreadsheet;
		cfg.inputClass = _inputClass;
		cfg.outputClass = _outputClass;
		return newSpreadsheetBinder( cfg );
	}


	private static SpreadsheetByNameBinder newSpreadsheetByNameBinder( SpreadsheetByNameBinder.Config _cfg )
	{
		return new SpreadsheetByNameBinderImpl( _cfg );
	}

	public static SpreadsheetByNameBinder newSpreadsheetByNameBinder( SpreadsheetBinder _binder )
	{
		SpreadsheetByNameBinder.Config cfg = new SpreadsheetByNameBinder.Config();
		cfg.binder = _binder;
		return newSpreadsheetByNameBinder( cfg );
	}

	
	private static SpreadsheetNameCreator newSpreadsheetCellNameCreator( SpreadsheetNameCreator.Config _cfg )
	{
		return new SpreadsheetNameCreatorImpl( _cfg );
	}

	public static SpreadsheetNameCreator newSpreadsheetCellNameCreator( Spreadsheet.Sheet _sheet )
	{
		SpreadsheetNameCreator.Config cfg = new SpreadsheetNameCreator.Config();
		cfg.sheet = _sheet;
		return newSpreadsheetCellNameCreator( cfg );
	}

	
	public static SpreadsheetCompiler newSpreadsheetCompiler( SpreadsheetCompiler.Config _config )
	{
		return new SpreadsheetCompilerImpl( _config );
	}

	public static SaveableEngine compileEngine( SpreadsheetBinding _binding, NumericType _numericType, Class _factoryClass,
			Method _factoryMethod ) throws CompilerError, EngineError
	{
		final SpreadsheetCompiler.Config cfg = new SpreadsheetCompiler.Config();
		cfg.binding = _binding;
		cfg.numericType = _numericType;
		cfg.factoryClass = _factoryClass;
		cfg.factoryMethod = _factoryMethod;
		return newSpreadsheetCompiler( cfg ).compile();
	}

	public static SaveableEngine compileEngine( SpreadsheetBinding _binding, NumericType _numericType ) throws CompilerError, EngineError
	{
		final SpreadsheetCompiler.Config cfg = new SpreadsheetCompiler.Config();
		cfg.binding = _binding;
		cfg.numericType = _numericType;
		return newSpreadsheetCompiler( cfg ).compile();
	}

	
	/**
	 * Returns the numeric type instance with the specified attributes.
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
