package sej.internal;

import java.lang.reflect.Method;
import java.util.Map;

import sej.EngineError;
import sej.NumericType;
import sej.SaveableEngine;
import sej.api.CompilerError;
import sej.api.Util;
import sej.internal.model.ComputationModel;

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
			if (this.factoryClass != null) {
				Util.validateIsImplementable( this.factoryClass, "factoryClass" );

				if (this.factoryMethod == null) throw new IllegalArgumentException( "factoryMethod not specified" );
				Util.validateIsImplementable( this.factoryMethod, "factoryMethod" );

				if (this.factoryMethod.getReturnType() != this.model.getOutputClass())
					throw new IllegalArgumentException( "factoryMethod '"
							+ this.factoryMethod + "' does not return outputClass '" + this.model.getOutputClass() + "'" );
				final Class[] params = this.factoryMethod.getParameterTypes();
				if (params.length != 1 || params[ 0 ] != this.model.getInputClass())
					throw new IllegalArgumentException( "factoryMethod '"
							+ this.factoryMethod + "' does not have single parameter of type inputClass '"
							+ this.model.getInputClass() + "'" );

				final Map<String, Method> abstracts = Util.abstractMethodsOf( this.factoryClass );
				abstracts.remove( "newComputation(Ljava/lang/Object;)Lsej/Computation;" );
				abstracts.remove( Util.nameAndSignatureOf( this.factoryMethod ) );
				if (abstracts.size() > 0) {
					throw new IllegalArgumentException(
							"factoryClass is still abstract after implementing factoryMethod; offending method is '"
									+ abstracts.values().iterator().next() + "'" );
				}
			}
			else if (this.factoryMethod != null) {
				throw new IllegalArgumentException( "factoryMethod is set, but factoryClass is not" );
			}
		}
	}


	public abstract SaveableEngine compile() throws CompilerError, EngineError;

}
