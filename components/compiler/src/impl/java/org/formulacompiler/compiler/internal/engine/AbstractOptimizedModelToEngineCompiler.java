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

package org.formulacompiler.compiler.internal.engine;

import java.lang.reflect.Method;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.compiler.internal.model.ComputationModel;
import org.formulacompiler.runtime.EngineException;


public abstract class AbstractOptimizedModelToEngineCompiler implements OptimizedModelToEngineCompiler
{
	private final Config config;

	protected AbstractOptimizedModelToEngineCompiler( OptimizedModelToEngineCompiler.Config _config )
	{
		super();
		_config.validate();
		this.config = _config.clone();
	}


	public Config config()
	{
		return this.config;
	}

	public NumericType getNumericType()
	{
		return config().numericType;
	}

	public ComputationModel getModel()
	{
		return config().model;
	}

	public Class getFactoryClass()
	{
		return config().factoryClass;
	}

	public Method getFactoryMethod()
	{
		return config().factoryMethod;
	}

	public boolean isFullyCaching()
	{
		return config().fullCaching;
	}

	public boolean getCompileToReadableCode()
	{
		return config().compileToReadableCode;
	}

	public ClassLoader getParentClassLoader()
	{
		return config().parentClassLoader;
	}

	public boolean isComputationListenerEnabled()
	{
		return config().computationListenerEnabled;
	}


	public abstract SaveableEngine compile() throws CompilerException, EngineException;


}
