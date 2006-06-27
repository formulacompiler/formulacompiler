/*
 * Copyright � 2006 by Abacus Research AG, Switzerland.
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
package sej.internal.engine.compiler;

import java.lang.reflect.Method;

import sej.CompilerException;
import sej.NumericType;
import sej.SaveableEngine;
import sej.internal.model.ComputationModel;
import sej.runtime.EngineException;

public abstract class AbstractEngineCompiler implements EngineCompiler
{

	// ------------------------------------------------ Configuration & Factory

	private static Factory factory;

	public static EngineCompiler newInstance( EngineCompiler.Config _config )
	{
		return factory.newInstance( _config );
	}

	protected static void setFactory( Factory _factory )
	{
		factory = _factory;
	}

	protected static abstract class Factory
	{
		protected abstract EngineCompiler newInstance( EngineCompiler.Config _config );
	}


	// ------------------------------------------------ Interface

	private final ComputationModel model;
	private final NumericType numericType;
	private final Class factoryClass;
	private final Method factoryMethod;


	protected AbstractEngineCompiler(EngineCompiler.Config _config)
	{
		super();
		_config.validate();
		this.model = _config.model;
		this.numericType = _config.numericType;
		this.factoryClass = _config.factoryClass;
		this.factoryMethod = _config.factoryMethod;
	}


	public ComputationModel getModel()
	{
		return this.model;
	}

	public NumericType getNumericType()
	{
		return this.numericType;
	}

	public Class getFactoryClass()
	{
		return this.factoryClass;
	}

	public Method getFactoryMethod()
	{
		return this.factoryMethod;
	}


	public abstract SaveableEngine compile() throws CompilerException, EngineException;


}
