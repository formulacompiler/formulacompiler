package sej.api;

import java.lang.reflect.Method;

import sej.EngineError;
import sej.NumericType;
import sej.SaveableEngine;


public interface SpreadsheetCompiler
{

	public static class Config 
	{
		public SpreadsheetBinding binding;
		public NumericType numericType = NumericType.DEFAULT;
		public Class factoryClass = null;
		public Method factoryMethod = null;
	}

	public SaveableEngine compile() throws CompilerError, EngineError;

}
