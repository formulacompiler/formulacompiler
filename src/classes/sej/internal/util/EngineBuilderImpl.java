package sej.internal.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import sej.CompilerError;
import sej.EngineBuilder;
import sej.NumericType;
import sej.SEJ;
import sej.SaveableEngine;
import sej.Spreadsheet;
import sej.SpreadsheetBinder;
import sej.SpreadsheetBinding;
import sej.SpreadsheetByNameBinder;
import sej.SpreadsheetError;
import sej.SpreadsheetNameCreator;
import sej.runtime.EngineError;


public class EngineBuilderImpl implements EngineBuilder
{
	private Spreadsheet spreadsheet;
	private NumericType numericType = NumericType.DEFAULT;
	private Class inputClass;
	private Class outputClass;
	private Class factoryClass;
	private Method factoryMethod;
	private SpreadsheetBinder binder;
	private SpreadsheetByNameBinder byNameBinder;


	// ------------------------------------------------ Numeric Type


	public NumericType getNumericType()
	{
		return this.numericType;
	}

	public void setNumericType( NumericType _type )
	{
		this.numericType = _type;
	}


	// ------------------------------------------------ Spreadsheet


	public Spreadsheet getSpreadsheet()
	{
		return this.spreadsheet;
	}

	public void setSpreadsheet( Spreadsheet _sheet )
	{
		failIfBinderExists();
		this.spreadsheet = _sheet;
	}


	public void loadSpreadsheet( File _file ) throws FileNotFoundException, IOException, SpreadsheetError
	{
		setSpreadsheet( SEJ.loadSpreadsheet( _file ) );
	}


	public void loadSpreadsheet( String _fileName ) throws FileNotFoundException, IOException, SpreadsheetError
	{
		loadSpreadsheet( new File( _fileName ) );
	}


	// ------------------------------------------------ Classes


	public Class getInputClass()
	{
		return this.inputClass;
	}

	public void setInputClass( Class _inputClass )
	{
		failIfBinderExists();
		this.inputClass = _inputClass;
	}

	public Class getOutputClass()
	{
		return this.outputClass;
	}

	public void setOutputClass( Class _outputClass )
	{
		failIfBinderExists();
		this.outputClass = _outputClass;
	}

	public Class getFactoryClass()
	{
		return this.factoryClass;
	}

	public void setFactoryClass( Class _class )
	{
		failIfBinderExists();
		this.factoryClass = _class;
	}

	public Method getFactoryMethod()
	{
		return this.factoryMethod;
	}

	public void setFactoryMethod( Method _factoryMethod )
	{
		failIfBinderExists();
		this.factoryMethod = _factoryMethod;
	}

	protected void configureClasses() throws CompilerError
	{
		if (this.factoryMethod != null) {
			if (this.factoryClass == null) {
				this.factoryClass = this.factoryMethod.getDeclaringClass();
			}
		}
		else if (this.factoryClass != null) {
			if (this.inputClass == null || this.outputClass == null) {
				this.factoryMethod = determineFactoryMethod( this.factoryClass );
			}
			else {
				this.factoryMethod = determineFactoryMethod( this.factoryClass, this.inputClass, this.outputClass );
			}
		}
		if (this.inputClass == null && this.factoryMethod != null && this.factoryMethod.getParameterTypes().length == 1) {
			this.inputClass = this.factoryMethod.getParameterTypes()[ 0 ];
		}
		if (this.outputClass == null && this.factoryMethod != null) {
			this.outputClass = this.factoryMethod.getReturnType();
		}
	}

	/**
	 * Look for a method with a signature "(any)any". Return the first abstract one, or else the last
	 * one that matches.
	 * 
	 * @throws CompilerError
	 */
	private Method determineFactoryMethod( Class _factoryClass ) throws CompilerError
	{
		final Method[] methods = _factoryClass.getMethods();
		Method candidate = null;
		for (Method method : methods) {
			final int mod = method.getModifiers();
			if (Modifier.isPublic( mod ) && !Modifier.isStatic( mod ) && !Modifier.isFinal( mod )) {
				if (method.getReturnType() != null && method.getParameterTypes().length == 1) {
					candidate = method;
					if (Modifier.isAbstract( mod )) {
						return candidate;
					}
				}
			}
		}
		if (candidate != null) return candidate;
		throw new CompilerError.FactoryMethodMissing( _factoryClass );
	}

	/**
	 * Look for a method with the signature "(input)output". Return the first abstract one, or else
	 * the last one that matches. Fail if none found.
	 * 
	 * @throws CompilerError
	 */
	private Method determineFactoryMethod( Class _factoryClass, Class _inputClass, Class _outputClass )
			throws CompilerError
	{
		final Method[] methods = _factoryClass.getMethods();
		Method candidate = null;
		for (Method method : methods) {
			final int mod = method.getModifiers();
			if (Modifier.isPublic( mod ) && !Modifier.isStatic( mod ) && !Modifier.isFinal( mod )) {
				if (method.getReturnType() == _outputClass) {
					Class[] params = method.getParameterTypes();
					if (params.length == 1 && params[ 0 ] == _inputClass) {
						candidate = method;
						if (Modifier.isAbstract( method.getModifiers() )) {
							return candidate;
						}
					}
				}
			}
		}
		if (candidate != null) return candidate;
		throw new CompilerError.FactoryMethodMissing( _factoryClass, _inputClass, _outputClass );
	}


	private void validateClasses()
	{
		if (this.inputClass == null) throw new IllegalStateException( "InputClass not specified" );
		if (this.outputClass == null) throw new IllegalStateException( "OutputClass not specified" );
		if (this.factoryClass != null) {
			if (this.factoryMethod == null) throw new IllegalStateException( "FactoryMethod not specified" );
		}
	}


	// ------------------------------------------------ Name creation


	public boolean areAnyNamesDefined()
	{
		return (getSpreadsheet().getDefinedNames().length > 0);
	}


	public void createCellNamesFromRowTitles()
	{
		final SpreadsheetNameCreator creator = SEJ.newSpreadsheetCellNameCreator( getSpreadsheet().getSheets()[ 0 ] );
		creator.createCellNamesFromRowTitles();
	}


	// ------------------------------------------------ Binding


	protected SpreadsheetBinder getBinder() throws CompilerError
	{
		if (this.binder == null) this.binder = makeBinder();
		return this.binder;
	}

	private SpreadsheetBinder makeBinder() throws CompilerError
	{
		configureClasses();
		validateClasses();
		return SEJ.newSpreadsheetBinder( this.spreadsheet, this.inputClass, this.outputClass );
	}

	private void failIfBinderExists()
	{
		if (this.binder != null) throw new IllegalStateException( "Binder already exists" );
	}

	public SpreadsheetBinder.Section getRootBinder() throws CompilerError
	{
		return getBinder().getRoot();
	}

	public SpreadsheetByNameBinder getByNameBinder() throws CompilerError
	{
		if (this.byNameBinder == null) this.byNameBinder = makeByNameBinder();
		return this.byNameBinder;
	}

	private SpreadsheetByNameBinder makeByNameBinder() throws CompilerError
	{
		return SEJ.newSpreadsheetByNameBinder( getBinder() );
	}


	public void bindAllByName() throws CompilerError
	{
		if (!areAnyNamesDefined()) {
			createCellNamesFromRowTitles();
		}
		SpreadsheetByNameBinder bn = getByNameBinder();
		bn.outputs().bindAllMethodsToNamedCells();
		bn.inputs().bindAllNamedCellsToMethods();
	}


	// ------------------------------------------------ Compilation


	public SaveableEngine compile() throws CompilerError, EngineError
	{
		final SpreadsheetBinding binding = getBinder().getBinding();
		return SEJ.compileEngine( binding, this.numericType, this.factoryClass, this.factoryMethod );
	}


}
