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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.runtime.EngineException;
import org.formulacompiler.spreadsheet.Spreadsheet.Cell;


/**
 * Simplified interface to AFC's spreadsheet compiler functionality for the most typical use-cases.
 * Typical example: {@.jcite org.formulacompiler.tutorials.Basics:---- CompileFactory}
 * <p>
 * Please refer to the <a target="_top" href="{@docRoot}/../tutorial/basics.htm"
 * target="_top">tutorial</a> for details.
 * </p>
 * 
 * @author peo
 */
public interface EngineBuilder
{

	/**
	 * Returns the numeric type used by computations compiled by this builder.
	 */
	public NumericType getNumericType();

	/**
	 * Sets the numeric type to be used by computations compiled by this builder.
	 * 
	 * <p>
	 * See the <a target="_top" href="{@docRoot}/../tutorial/numeric_type.htm">tutorial</a> for
	 * details.
	 */
	public void setNumericType( NumericType _type );


	/**
	 * Gets the spreadsheet representation that this builder compiles. This is mostly useful after
	 * having called {@link #loadSpreadsheet(File)}.
	 */
	public Spreadsheet getSpreadsheet();

	/**
	 * Sets the spreadsheet representation that this builder compiles. You normally do this if you
	 * have loaded the spreadsheet using one of the loading methods of {@link SpreadsheetCompiler},
	 * or after having constructed the spreadsheet in-memory using a {@link SpreadsheetBuilder}.
	 * 
	 * @param _sheet is the spreadsheet representation to use as input for spreadsheet compilation.
	 * 
	 * @see #loadSpreadsheet(File)
	 */
	public void setSpreadsheet( Spreadsheet _sheet );

	/**
	 * Loads the input spreadsheet from a file.
	 * 
	 * @param _file is the spreadsheet file to load.
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws SpreadsheetException
	 * 
	 * @see #setSpreadsheet(Spreadsheet)
	 * @see #setLoadAllCellValues(boolean)
	 */
	public void loadSpreadsheet( File _file ) throws FileNotFoundException, IOException, SpreadsheetException;

	/**
	 * Loads the input spreadsheet from a file.
	 * 
	 * @param _fileName is the name of the spreadsheet file to load.
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws SpreadsheetException
	 * 
	 * @see #setSpreadsheet(Spreadsheet)
	 * @see #setLoadAllCellValues(boolean)
	 */
	public void loadSpreadsheet( String _fileName ) throws FileNotFoundException, IOException, SpreadsheetException;


	/**
	 * Controls whether {@link Cell#getValue()} should return valid values instead of {@code null}
	 * for computed cells. The default is {@code false}.
	 * 
	 * @see #loadSpreadsheet(File)
	 * @see #loadSpreadsheet(String)
	 */
	public void setLoadAllCellValues( boolean _value );


	/**
	 * Returns the application-specific type defining the methods which input cells can call.
	 * 
	 * @see #setInputClass(Class)
	 */
	public Class getInputClass();

	/**
	 * Sets the application-specific type defining the methods which input cells can call. Must be
	 * {@code public}. Can only be left {@code null} if a factory type is provided (see
	 * {@link #setFactoryClass(Class)}), from which it is then inferred. If the factory method is
	 * provided, then this class must be assignable from the factory method's single argument.
	 * 
	 * <p>
	 * See the <a target="_top" href="{@docRoot}/../tutorial/basics.htm#Convention">tutorial</a>
	 * for details.
	 * 
	 * @see #setFactoryMethod(Method)
	 */
	public void setInputClass( Class _inputClass );


	/**
	 * Returns the application-specific type defining the methods which output cells can implement.
	 * 
	 * @see #setOutputClass(Class)
	 */
	public Class getOutputClass();

	/**
	 * Sets the application-specific type defining the methods output cells can implement. If a
	 * class, then the generated computation extends this class. If an interface, then the generated
	 * computation implements this interface. Must be {@code public}. Can only be left {@code null}
	 * if a factory type is provided (see {@link #setFactoryClass(Class)}), from which it is then
	 * inferred. If the factory method is set, then the factory method's return type must be
	 * assignable from this class.
	 * 
	 * <p>
	 * See the <a target="_top" href="{@docRoot}/../tutorial/basics.htm#Convention">tutorial</a>
	 * for details.
	 * 
	 * @see #setFactoryMethod(Method)
	 */
	public void setOutputClass( Class _outputClass );


	/**
	 * Returns the application-specific type to be used for the generated factory.
	 * 
	 * @see #setFactoryClass(Class)
	 */
	public Class getFactoryClass();

	/**
	 * Sets either an application-specific class from which to descend the generated computation
	 * factory, or an application-specific interface which the generated factory should implement.
	 * Must be {@code public}. Can be left {@code null}. If set, it must be {@code public} and have
	 * at most a single abstract method.
	 * 
	 * <p>
	 * If you don't specify the factory method, {@link #setFactoryMethod(Method)}, then AFC infers
	 * it as the sole abstract method, or else the first overridable method of the type that has the
	 * proper application-specific computation factory signature.
	 * 
	 * <p>
	 * See the <a target="_top" href="{@docRoot}/../tutorial/basics.htm#Convention">tutorial</a>
	 * for details.
	 */
	public void setFactoryClass( Class _class );


	/**
	 * Returns the application-specific factory method.
	 * 
	 * @see #setFactoryMethod(Method)
	 */
	public Method getFactoryMethod();

	/**
	 * Sets the application-specific method of the given factory type,
	 * {@link #setFactoryClass(Class)}, which AFC should implement to return new computation
	 * instances. Must be {@code public} and have a single parameter of the input type of the
	 * spreadsheet binding, and return the output type of the spreadsheet binding.
	 * 
	 * <p>
	 * If you specify the factory type, then this must be a method of it. If not, then the factory
	 * type is taken to be the declaring type of this method.
	 * 
	 * <p>
	 * If you don't specify the input and output types, then they are inferred from this factory
	 * method. If you do, then they must be compatible with the type of the single argument and the
	 * return type of this factory method, respectively. More precisely, the input type must be
	 * assignable from the type of the single argument, and the return type must be assignable from
	 * the output type.
	 * 
	 * <p>
	 * See the <a target="_top" href="{@docRoot}/../tutorial/basics.htm#Convention">tutorial</a>
	 * for details.
	 */
	public void setFactoryMethod( Method _factoryMethod );


	/**
	 * Checks whether the spreadsheet contains any named cells or ranges.
	 */
	public boolean areAnyNamesDefined();


	/**
	 * Uses a {@link SpreadsheetNameCreator} to create cell names from row titles in the first sheet
	 * of the spreadsheet. This method is called by {@link #bindAllByName()} automatically if
	 * {@link #areAnyNamesDefined()} returns {@code false}.
	 * 
	 * <p>
	 * See the <a target="_top" href="{@docRoot}/../tutorial/basics.htm#Convention">tutorial</a>
	 * for details.
	 */
	public void createCellNamesFromRowTitles();


	/**
	 * Returns the root of the {@link SpreadsheetBinder} used by this engine builder to associate
	 * cells with Java methods. You only need to access it if neither the behaviour of
	 * {@link #bindAllByName()}, nor that of the {@link #getByNameBinder()} is sufficient for you.
	 * 
	 * @return the root section binder.
	 * 
	 * @throws CompilerException
	 */
	public SpreadsheetBinder.Section getRootBinder() throws CompilerException;


	/**
	 * Returns the {@link SpreadsheetByNameBinder} used by this engine builder to associate cells
	 * with Java methods. You only need to access it if the behaviour of {@link #bindAllByName()} is
	 * not sufficient for you.
	 * 
	 * @return the binder.
	 * 
	 * @throws CompilerException
	 */
	public SpreadsheetByNameBinder getByNameBinder() throws CompilerException;


	/**
	 * Uses reflection to bind all named cells in the spreadsheet to corresponding methods on the
	 * input and output types.
	 * 
	 * <p>
	 * See the <a target="_top" href="{@docRoot}/../tutorial/basics.htm#Convention">tutorial</a>
	 * for details.
	 * 
	 * @throws CompilerException
	 */
	public void bindAllByName() throws CompilerException;


	/**
	 * Returns the parent class loader used for the compiled engine (which is itself class loader).
	 * 
	 * @return {@link ClassLoader#getSystemClassLoader()} if the value was never modified, or else
	 *         the value you have last set.
	 * 
	 * @see #setParentClassLoaderForEngine(ClassLoader)
	 * @see #compile()
	 */
	public ClassLoader getParentClassLoaderForEngine();

	/**
	 * Sets the parent class loader to be used for the compiled engine (which is itself a class
	 * loader).
	 * 
	 * @param _value is the new parent class loader. It probably never makes sense to pass
	 *           {@code null} here.
	 * 
	 * @see #getParentClassLoaderForEngine()
	 * @see #compile()
	 */
	public void setParentClassLoaderForEngine( ClassLoader _value );


	/**
	 * Compiles an executable computation engine from the inputs to this builder. In particular, you
	 * must have loaded a spreadsheet, set the input and output types, or the factory type, and bound
	 * spreadsheet cells to methods.
	 * 
	 * <p>
	 * See the <a target="_top" href="{@docRoot}/../tutorial/basics.htm">tutorial</a> for details.
	 * 
	 * @return the compiled engine, ready to be used immediately, or saved to persistent storage for
	 *         later use.
	 * 
	 * @throws CompilerException
	 * @throws EngineException
	 * 
	 * @see #loadSpreadsheet(File)
	 * @see #setInputClass(Class)
	 * @see #setOutputClass(Class)
	 * @see #setFactoryClass(Class)
	 * @see #bindAllByName()
	 * @see #setParentClassLoaderForEngine(ClassLoader)
	 */
	public SaveableEngine compile() throws CompilerException, EngineException;


	/**
	 * Factory interface for
	 * {@link org.formulacompiler.runtime.ImplementationLocator#getInstance(Class)}.
	 */
	public interface Factory
	{
		/**
		 * Factory method.
		 */
		EngineBuilder newInstance();
	}

}