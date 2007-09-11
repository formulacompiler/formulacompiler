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
package org.formulacompiler.spreadsheet.internal.compiler;

import java.lang.reflect.Method;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.compiler.internal.engine.ModelToEngineCompiler;
import org.formulacompiler.compiler.internal.engine.ModelToEngineCompilerImpl;
import org.formulacompiler.compiler.internal.model.ComputationModel;
import org.formulacompiler.runtime.EngineException;
import org.formulacompiler.spreadsheet.SpreadsheetBinding;
import org.formulacompiler.spreadsheet.SpreadsheetToEngineCompiler;

public final class SpreadsheetToEngineCompilerImpl implements SpreadsheetToEngineCompiler
{
	private final SpreadsheetBinding binding;
	private final NumericType numericType;
	private final Class factoryClass;
	private final Method factoryMethod;
	private boolean fullCaching;
	private final ClassLoader parentClassLoader;
	private final boolean compileToReadableCode;


	public SpreadsheetToEngineCompilerImpl( Config _config )
	{
		super();

		_config.validate();

		this.binding = _config.binding;
		this.numericType = _config.numericType;
		this.factoryClass = _config.factoryClass;
		this.factoryMethod = _config.factoryMethod;
		this.fullCaching = _config.fullCaching;
		this.parentClassLoader = _config.parentClassLoader;
		this.compileToReadableCode = _config.compileToReadableCode;
	}

	public static final class Factory implements SpreadsheetToEngineCompiler.Factory
	{
		public SpreadsheetToEngineCompiler newInstance( Config _config )
		{
			return new SpreadsheetToEngineCompilerImpl( _config );
		}
	}


	public SaveableEngine compile() throws CompilerException, EngineException
	{
		final SpreadsheetToModelCompiler cc = new SpreadsheetToModelCompiler( this.binding, this.numericType,
				this.compileToReadableCode );
		ComputationModel cm = cc.compile();

		final ModelToEngineCompiler.Config ecc = new ModelToEngineCompiler.Config();
		ecc.model = cm;
		ecc.numericType = this.numericType;
		ecc.factoryClass = this.factoryClass;
		ecc.factoryMethod = this.factoryMethod;
		ecc.parentClassLoader = this.parentClassLoader;
		ecc.fullCaching = this.fullCaching;
		ecc.compileToReadableCode = this.compileToReadableCode;
		final ModelToEngineCompiler ec = new ModelToEngineCompilerImpl( ecc );

		return ec.compile();
	}

}
