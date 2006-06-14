package sej;

import java.lang.reflect.Method;


public interface SpreadsheetCompiler
{

	public static class Config
	{
		public SpreadsheetBinding binding;
		public NumericType numericType = NumericType.DEFAULT;
		public Class factoryClass = null;
		public Method factoryMethod = null;

		public void validate()
		{
			if (this.binding == null) throw new IllegalArgumentException( "binding is null" );
			if (this.numericType == null) throw new IllegalArgumentException( "numericType is null" );

			SEJ.validateFactory( this.factoryClass, this.factoryMethod, this.binding.getInputClass(), this.binding
					.getOutputClass() );
		}
	}

	public SaveableEngine compile() throws CompilerError, EngineError;

}
