package sej.internal.spreadsheet.compiler;

import java.lang.reflect.Method;

import sej.CompilerError;
import sej.NumericType;
import sej.SaveableEngine;
import sej.SpreadsheetBinding;
import sej.SpreadsheetCompiler;
import sej.internal.engine.compiler.AbstractEngineCompiler;
import sej.internal.engine.compiler.EngineCompiler;
import sej.internal.model.ComputationModel;
import sej.internal.model.compiler.ComputationModelCompiler;
import sej.runtime.EngineError;


public class SpreadsheetCompilerImpl implements SpreadsheetCompiler
{
	private final SpreadsheetBinding binding;
	private final NumericType numericType;
	private final Class factoryClass;
	private final Method factoryMethod;

	
	public SpreadsheetCompilerImpl(Config _config)
	{
		super();

		_config.validate();

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
