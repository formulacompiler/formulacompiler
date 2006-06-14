package sej;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;

import sej.api.CompilerError;
import sej.api.Spreadsheet;
import sej.api.SpreadsheetBinder;
import sej.api.SpreadsheetBinding;
import sej.api.SpreadsheetBuilder;
import sej.api.SpreadsheetByNameBinder;
import sej.api.SpreadsheetCompiler;
import sej.api.SpreadsheetError;
import sej.api.SpreadsheetNameCreator;
import sej.internal.EngineBuilderImpl;
import sej.internal.NumericTypeImpl;
import sej.internal.bytecode.compiler.ByteCodeEngineCompiler;
import sej.internal.spreadsheet.binder.SpreadsheetBinderImpl;
import sej.internal.spreadsheet.binder.SpreadsheetByNameBinderImpl;
import sej.internal.spreadsheet.compiler.SpreadsheetCompilerImpl;
import sej.internal.spreadsheet.loader.AnyFormatSpreadsheetLoader;
import sej.internal.spreadsheet.namer.SpreadsheetNameCreatorImpl;

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

}
