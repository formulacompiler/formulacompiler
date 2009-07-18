/*
 * Copyright (c) 2006, 2008 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * This file is part of the Abacus Formula Compiler (AFC).
 *
 * For commercial licensing, please contact sales(at)formulacompiler.com.
 *
 * AFC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AFC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AFC.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.formulacompiler.spreadsheet.internal.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.runtime.Computation;
import org.formulacompiler.runtime.ComputationMode;
import org.formulacompiler.runtime.EngineException;
import org.formulacompiler.spreadsheet.ConstantExpressionOptimizationListener;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBinder;
import org.formulacompiler.spreadsheet.SpreadsheetByNameBinder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.SpreadsheetLoader;
import org.formulacompiler.spreadsheet.SpreadsheetNameCreator;
import org.formulacompiler.spreadsheet.SpreadsheetToEngineCompiler;
import org.formulacompiler.spreadsheet.SpreadsheetBinder.Config;


public class EngineBuilderImpl implements EngineBuilder
{
	private Spreadsheet spreadsheet;
	private NumericType numericType = SpreadsheetCompiler.DEFAULT_NUMERIC_TYPE;
	private ComputationMode computationMode = null;
	private Class inputClass;
	private Class outputClass;
	private Class factoryClass;
	private Method factoryMethod;
	private boolean fullCaching;
	private Computation.Config compileTimeConfig;
	private SpreadsheetBinder binder;
	private SpreadsheetByNameBinder byNameBinder;
	private ClassLoader parentClassLoaderForEngine = ClassLoader.getSystemClassLoader();
	private boolean compileToReadableCode = false;
	private boolean computationListenerEnabled = false;
	private ConstantExpressionOptimizationListener constExprOptListener;


	public static final class Factory implements EngineBuilder.Factory
	{
		public EngineBuilder newInstance()
		{
			return new EngineBuilderImpl();
		}
	}


	// ------------------------------------------------ Numeric Type


	public NumericType getNumericType()
	{
		return this.numericType;
	}

	public void setNumericType( NumericType _type )
	{
		this.numericType = _type;
	}


	// ------------------------------------------------ Computation Mode


	public ComputationMode getComputationMode()
	{
		return this.computationMode;
	}

	public void setComputationMode( final ComputationMode _computationMode )
	{
		this.computationMode = _computationMode;
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


	private boolean loadAllCellValues = false;

	public void setLoadAllCellValues( boolean _value )
	{
		this.loadAllCellValues = _value;
	}


	public void loadSpreadsheet( File _file ) throws FileNotFoundException, IOException, SpreadsheetException
	{
		final SpreadsheetLoader.Config cfg = new SpreadsheetLoader.Config();
		cfg.loadAllCellValues = this.loadAllCellValues;
		setSpreadsheet( SpreadsheetCompiler.loadSpreadsheet( _file.getName(), new FileInputStream( _file ), cfg ) );
	}


	public void loadSpreadsheet( String _fileName ) throws FileNotFoundException, IOException, SpreadsheetException
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

	protected void configureClasses() throws CompilerException
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
	 * @throws CompilerException
	 */
	private Method determineFactoryMethod( Class _factoryClass ) throws CompilerException
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
		throw new CompilerException.FactoryMethodMissing( _factoryClass );
	}

	/**
	 * Look for a method with the signature "(input)output". Return the first abstract one, or else
	 * the last one that matches. Fail if none found.
	 * 
	 * @throws CompilerException
	 */
	private Method determineFactoryMethod( Class _factoryClass, Class _inputClass, Class _outputClass )
			throws CompilerException
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
		throw new CompilerException.FactoryMethodMissing( _factoryClass, _inputClass, _outputClass );
	}


	private void validateClasses()
	{
		if (this.inputClass == null) throw new IllegalStateException( "InputClass not specified" );
		if (this.outputClass == null) throw new IllegalStateException( "OutputClass not specified" );
		if (this.factoryClass != null) {
			if (this.factoryMethod == null) throw new IllegalStateException( "FactoryMethod not specified" );
		}
	}


	// ------------------------------------------------ Caching


	public boolean getFullCaching()
	{
		return this.fullCaching;
	}

	public void setFullCaching( boolean _enabled )
	{
		this.fullCaching = _enabled;
	}


	// ------------------------------------------------ Compile-time configuration


	public Computation.Config getCompileTimeConfig()
	{
		if (this.compileTimeConfig == null) {
			this.compileTimeConfig = new Computation.Config();
		}
		return this.compileTimeConfig;
	}

	public void setCompileTimeConfig( Computation.Config _value )
	{
		this.compileTimeConfig = _value;
	}


	// ------------------------------------------------ Name creation


	public boolean areAnyNamesDefined()
	{
		return (getSpreadsheet().getRangeNames().size() > 0);
	}


	public void createCellNamesFromRowTitles()
	{
		final SpreadsheetNameCreator creator = SpreadsheetCompiler.newSpreadsheetCellNameCreator( getSpreadsheet()
				.getSheets()[ 0 ] );
		creator.createCellNamesFromRowTitles();
	}


	// ------------------------------------------------ Binding


	protected SpreadsheetBinder getBinder() throws CompilerException
	{
		if (this.binder == null) this.binder = makeBinder();
		return this.binder;
	}

	private SpreadsheetBinder makeBinder() throws CompilerException
	{
		configureClasses();
		validateClasses();
		final Config cfg = new SpreadsheetBinder.Config();
		cfg.spreadsheet = this.spreadsheet;
		cfg.inputClass = this.inputClass;
		cfg.outputClass = this.outputClass;
		cfg.compileTimeConfig = this.compileTimeConfig;
		return SpreadsheetCompiler.newSpreadsheetBinder( cfg );
	}

	private void failIfBinderExists()
	{
		if (this.binder != null) throw new IllegalStateException( "Binder already exists" );
	}

	public SpreadsheetBinder.Section getRootBinder() throws CompilerException
	{
		return getBinder().getRoot();
	}

	public CallFrame newCallFrame( Method _method, Object... _args )
	{
		return SpreadsheetCompiler.newCallFrame( _method, _args );
	}

	public SpreadsheetByNameBinder getByNameBinder() throws CompilerException
	{
		if (this.byNameBinder == null) this.byNameBinder = makeByNameBinder();
		return this.byNameBinder;
	}

	private SpreadsheetByNameBinder makeByNameBinder() throws CompilerException
	{
		return SpreadsheetCompiler.newSpreadsheetByNameBinder( getBinder() );
	}


	public void bindAllByName() throws CompilerException
	{
		// ---- bindAllByName
		if (!areAnyNamesDefined()) {
			createCellNamesFromRowTitles();
		}
		SpreadsheetByNameBinder bn = getByNameBinder();
		bn.outputs().bindAllMethodsToNamedCells();
		bn.inputs().bindAllNamedCellsToMethods();
		// ---- bindAllByName
	}

	public void failIfByNameBindingLeftNamedCellsUnbound() throws CompilerException
	{
		getByNameBinder().failIfCellNamesAreStillUnbound();
	}


	public void bindAllByName( String _inputPrefix, String _outputPrefix ) throws CompilerException
	{
		// ---- bindAllByNamePrefixed
		if (!areAnyNamesDefined()) {
			createCellNamesFromRowTitles();
		}
		SpreadsheetByNameBinder bn = getByNameBinder();
		bn.outputs().bindAllMethodsToPrefixedNamedCells( _outputPrefix );
		bn.inputs().bindAllPrefixedNamedCellsToMethods( _inputPrefix );
		// ---- bindAllByNamePrefixed
	}

	public void failIfByNameBindingLeftNamedCellsUnbound( String _inputPrefix, String _outputPrefix )
			throws CompilerException
	{
		getByNameBinder().inputs().failIfCellNamesAreStillUnbound( _inputPrefix );
		getByNameBinder().outputs().failIfCellNamesAreStillUnbound( _outputPrefix );
	}


	// ------------------------------------------------ Compilation


	public ClassLoader getParentClassLoaderForEngine()
	{
		return this.parentClassLoaderForEngine;
	}

	public void setParentClassLoaderForEngine( ClassLoader _value )
	{
		this.parentClassLoaderForEngine = _value;
	}


	public boolean getCompileToReadableCode()
	{
		return this.compileToReadableCode;
	}

	public void setCompileToReadableCode( boolean _value )
	{
		this.compileToReadableCode = _value;
	}


	public boolean getComputationListenerEnabled()
	{
		return this.computationListenerEnabled;
	}

	public void setComputationListenerEnabled( boolean _enabled )
	{
		this.computationListenerEnabled = _enabled;
	}


	public ConstantExpressionOptimizationListener getConstantExpressionOptimizationListener()
	{
		return this.constExprOptListener;
	}

	public void setConstantExpressionOptimizationListener( ConstantExpressionOptimizationListener _listener )
	{
		this.constExprOptListener = _listener;
	}


	public SaveableEngine compile() throws CompilerException, EngineException
	{
		final SpreadsheetToEngineCompiler.Config cfg = new SpreadsheetToEngineCompiler.Config();
		cfg.binding = getBinder().getBinding();
		cfg.numericType = this.numericType;
		cfg.computationMode = this.computationMode;
		cfg.factoryClass = this.factoryClass;
		cfg.factoryMethod = this.factoryMethod;
		cfg.fullCaching = this.fullCaching;
		cfg.parentClassLoader = this.parentClassLoaderForEngine;
		cfg.compileToReadableCode = this.compileToReadableCode;
		cfg.computationListenerEnabled = this.computationListenerEnabled;
		cfg.constantExpressionOptimizationListener = this.constExprOptListener;
		return SpreadsheetCompiler.newSpreadsheetCompiler( cfg ).compile();
	}


}
