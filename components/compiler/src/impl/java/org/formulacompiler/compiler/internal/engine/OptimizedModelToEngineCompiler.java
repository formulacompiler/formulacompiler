/*
 * Copyright (c) 2006, 2008 by Abacus Research AG, Switzerland.
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

import java.lang.reflect.Method;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.compiler.internal.Util;
import org.formulacompiler.compiler.internal.model.ComputationModel;
import org.formulacompiler.runtime.EngineException;


public interface OptimizedModelToEngineCompiler
{

	public static class Config implements Cloneable
	{
		public ClassLoader parentClassLoader = getClass().getClassLoader();
		public ComputationModel model;
		public NumericType numericType;
		public Class factoryClass;
		public Method factoryMethod;
		public boolean fullCaching;
		public boolean compileToReadableCode;

		public void validate()
		{
			if (this.numericType == null) throw new IllegalArgumentException( "numericType is null" );
			if (this.model == null) throw new IllegalArgumentException( "model is null" );
			if (this.model.getInputClass() == null) throw new IllegalArgumentException( "model.inputClass is null" );
			if (this.model.getOutputClass() == null) throw new IllegalArgumentException( "model.outputClass is null" );

			Util.validateFactory( this.factoryClass, this.factoryMethod, this.model.getInputClass(), this.model
					.getOutputClass() );
		}

		@Override
		protected Config clone()
		{
			try {
				return (Config) super.clone();
			}
			catch (CloneNotSupportedException e) {
				throw new InternalError();
			}
		}

	}


	public abstract SaveableEngine compile() throws CompilerException, EngineException;


	public static interface Factory
	{
		public OptimizedModelToEngineCompiler newInstance( Config _config );
	}

}
