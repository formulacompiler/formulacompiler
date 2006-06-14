package sej.internal.spreadsheet.compiler;

import java.lang.reflect.Method;

import sej.EngineError;
import sej.NumericType;
import sej.SaveableEngine;
import sej.api.CompilerError;
import sej.api.SpreadsheetBinding;
import sej.api.SpreadsheetCompiler;
import sej.internal.AbstractEngineCompiler;
import sej.internal.EngineCompiler;
import sej.internal.model.ComputationModel;
import sej.internal.model.compiler.ComputationModelCompiler;


public class SpreadsheetCompilerImpl implements SpreadsheetCompiler
{
	private final SpreadsheetBinding binding;
	private final NumericType numericType;
	private final Class factoryClass;
	private final Method factoryMethod;

	
	public SpreadsheetCompilerImpl(Config _config)
	{
		super();

		assert _config.binding != null : "Binding must not be null";

		this.binding = _config.binding;
		this.numericType = _config.numericType;
		this.factoryClass = _config.factoryClass;
		this.factoryMethod = _config.factoryMethod;
	}


	public SaveableEngine compile() throws CompilerError, EngineError
	{
		ComputationModelCompiler cc = new ComputationModelCompiler( this.binding, this.numericType );
		ComputationModel cm = cc.compile();

		EngineCompiler.Config ecc = new EngineCompiler.Config();
		ecc.model = cm;
		ecc.numericType = this.numericType;
		ecc.factoryClass = this.factoryClass;
		ecc.factoryMethod = this.factoryMethod;
		EngineCompiler ec = AbstractEngineCompiler.newInstance( ecc );

		return ec.compile();
	}

}
