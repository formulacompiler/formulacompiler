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


	public abstract SaveableEngine compile() throws CompilerException, EngineException;


}
