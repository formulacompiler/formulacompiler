package sej.internal.engine.compiler;

import java.lang.reflect.Method;

import sej.CompilerError;
import sej.NumericType;
import sej.SaveableEngine;
import sej.internal.Util;
import sej.internal.model.ComputationModel;
import sej.runtime.EngineError;

public interface EngineCompiler
{

	public static class Config
	{
		public ComputationModel model;
		public NumericType numericType = NumericType.DEFAULT;
		public Class factoryClass;
		public Method factoryMethod;

		public void validate()
		{
			if (this.numericType == null) throw new IllegalArgumentException( "numericType is null" );
			if (this.model == null) throw new IllegalArgumentException( "model is null" );
			if (this.model.getInputClass() == null) throw new IllegalArgumentException( "model.inputClass is null" );
			if (this.model.getOutputClass() == null) throw new IllegalArgumentException( "model.outputClass is null" );

			Util.validateFactory( this.factoryClass, this.factoryMethod, this.model.getInputClass(), this.model
					.getOutputClass() );
		}
	}


	public abstract SaveableEngine compile() throws CompilerError, EngineError;

}
