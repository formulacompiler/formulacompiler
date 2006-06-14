package sej.internal;

import java.lang.reflect.Method;

import sej.EngineError;
import sej.NumericType;
import sej.SaveableEngine;
import sej.api.CompilerError;
import sej.internal.model.ComputationModel;

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


	public abstract SaveableEngine compile() throws CompilerError, EngineError;


}
