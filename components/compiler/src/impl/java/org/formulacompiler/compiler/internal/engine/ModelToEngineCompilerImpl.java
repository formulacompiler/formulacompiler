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

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.compiler.internal.model.ComputationModel;
import org.formulacompiler.compiler.internal.model.ComputationModelTransformer;
import org.formulacompiler.runtime.EngineException;
import org.formulacompiler.runtime.ImplementationLocator;


public final class ModelToEngineCompilerImpl extends AbstractOptimizedModelToEngineCompiler implements
		ModelToEngineCompiler
{

	public ModelToEngineCompilerImpl(Config _config)
	{
		super( _config );
	}

	public static final class Factory implements OptimizedModelToEngineCompiler.Factory
	{
		public OptimizedModelToEngineCompiler newInstance( Config _config )
		{
			return new ModelToEngineCompilerImpl( _config );
		}
	}


	@Override
	public SaveableEngine compile() throws CompilerException, EngineException
	{
		final ComputationModelTransformer.Config mtcfg = new ComputationModelTransformer.Config();
		mtcfg.model = getModel();
		mtcfg.numericType = getNumericType();
		final ComputationModelTransformer mt = TRANSFORMER_FACTORY.newInstance( mtcfg );
		final ComputationModel transformed = mt.destructiveTransform();

		final OptimizedModelToEngineCompiler.Config eccfg = new OptimizedModelToEngineCompiler.Config();
		eccfg.model = transformed;
		eccfg.numericType = getNumericType();
		eccfg.factoryClass = getFactoryClass();
		eccfg.factoryMethod = getFactoryMethod();
		eccfg.parentClassLoader = getParentClassLoader();
		final OptimizedModelToEngineCompiler ec = COMPILER_FACTORY.newInstance( eccfg );
		return ec.compile();
	}

	private static final ComputationModelTransformer.Factory TRANSFORMER_FACTORY = ImplementationLocator
			.getInstance( ComputationModelTransformer.Factory.class );

	private static final OptimizedModelToEngineCompiler.Factory COMPILER_FACTORY = ImplementationLocator
			.getInstance( OptimizedModelToEngineCompiler.Factory.class );

}
