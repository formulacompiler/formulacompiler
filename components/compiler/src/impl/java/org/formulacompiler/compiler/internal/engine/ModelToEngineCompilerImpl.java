/*
 * Copyright (c) 2006-2009 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * This file is part of the Abacus Formula Compiler (AFC).
 *
 * For commercial licensing, please contact sales(at)formulacompiler.com.
 *
 * AFC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AFC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AFC.  If not, see <http://www.gnu.org/licenses/>.
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

	public ModelToEngineCompilerImpl( Config _config )
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
		final Config cfg = config();
		mtcfg.model = cfg.model;
		mtcfg.numericType = cfg.numericType;
		mtcfg.constExprCellListenerSupport = cfg.constExprCellListenerSupport;
		mtcfg.computationListenerEnabled = cfg.computationListenerEnabled;
		final ComputationModelTransformer mt = TRANSFORMER_FACTORY.newInstance( mtcfg );
		final ComputationModel transformed = mt.destructiveTransform();

		final Config eccfg = cfg.clone();
		eccfg.model = transformed;
		final OptimizedModelToEngineCompiler ec = COMPILER_FACTORY.newInstance( eccfg );
		return ec.compile();
	}

	private static final ComputationModelTransformer.Factory TRANSFORMER_FACTORY = ImplementationLocator
			.getInstance( ComputationModelTransformer.Factory.class );

	private static final OptimizedModelToEngineCompiler.Factory COMPILER_FACTORY = ImplementationLocator
			.getInstance( OptimizedModelToEngineCompiler.Factory.class );

}
