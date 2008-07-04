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

package org.formulacompiler.spreadsheet.internal.compiler;

import java.lang.reflect.Method;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.compiler.internal.engine.ModelToEngineCompiler;
import org.formulacompiler.compiler.internal.engine.ModelToEngineCompilerImpl;
import org.formulacompiler.compiler.internal.model.ComputationModel;
import org.formulacompiler.runtime.ComputationMode;
import org.formulacompiler.runtime.EngineException;
import org.formulacompiler.spreadsheet.SpreadsheetBinding;
import org.formulacompiler.spreadsheet.SpreadsheetToEngineCompiler;

public final class SpreadsheetToEngineCompilerImpl implements SpreadsheetToEngineCompiler
{
	private final SpreadsheetBinding binding;
	private final NumericType numericType;
	private final ComputationMode computationMode;
	private final Class factoryClass;
	private final Method factoryMethod;
	private boolean fullCaching;
	private final ClassLoader parentClassLoader;
	private final boolean compileToReadableCode;


	public SpreadsheetToEngineCompilerImpl( Config _config )
	{
		super();

		_config.validate();

		this.binding = _config.binding;
		this.numericType = _config.numericType;
		this.computationMode = _config.computationMode;
		this.factoryClass = _config.factoryClass;
		this.factoryMethod = _config.factoryMethod;
		this.fullCaching = _config.fullCaching;
		this.parentClassLoader = _config.parentClassLoader;
		this.compileToReadableCode = _config.compileToReadableCode;
	}

	public static final class Factory implements SpreadsheetToEngineCompiler.Factory
	{
		public SpreadsheetToEngineCompiler newInstance( Config _config )
		{
			return new SpreadsheetToEngineCompilerImpl( _config );
		}
	}


	public SaveableEngine compile() throws CompilerException, EngineException
	{
		final SpreadsheetToModelCompiler cc = new SpreadsheetToModelCompiler( this.binding, this.numericType,
				this.computationMode, this.compileToReadableCode );
		ComputationModel cm = cc.compile();

		final ModelToEngineCompiler.Config ecc = new ModelToEngineCompiler.Config();
		ecc.model = cm;
		ecc.numericType = this.numericType;
		ecc.factoryClass = this.factoryClass;
		ecc.factoryMethod = this.factoryMethod;
		ecc.parentClassLoader = this.parentClassLoader;
		ecc.fullCaching = this.fullCaching;
		ecc.compileToReadableCode = this.compileToReadableCode;
		final ModelToEngineCompiler ec = new ModelToEngineCompilerImpl( ecc );

		return ec.compile();
	}

}
