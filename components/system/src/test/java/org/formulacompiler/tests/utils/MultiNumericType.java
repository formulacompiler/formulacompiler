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

package org.formulacompiler.tests.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.formulacompiler.compiler.FormulaCompiler;
import org.formulacompiler.compiler.NumericType;
import org.junit.runner.Runner;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

public final class MultiNumericType extends Suite
{
	static final NumericType[] NUMERIC_TYPES = new NumericType[]{
			FormulaCompiler.DOUBLE,
			FormulaCompiler.BIGDECIMAL128,
			FormulaCompiler.BIGDECIMAL_SCALE8,
			FormulaCompiler.LONG_SCALE6,
	};

	private final List<Runner> runners = new ArrayList<Runner>();

	/**
	 * Only called reflectively. Do not use programmatically.
	 */
	public MultiNumericType( Class<?> _class ) throws Throwable
	{
		super( _class, Collections.<Runner>emptyList() );
		for (NumericType numericType : NUMERIC_TYPES) {
			runners.add( new MultiNumericTypeClassRunner( getTestClass().getJavaClass(), numericType ) );
		}
	}

	@Override
	protected List<Runner> getChildren()
	{
		return this.runners;
	}

	private static class MultiNumericTypeClassRunner extends BlockJUnit4ClassRunner
	{
		private final NumericType numericType;

		public MultiNumericTypeClassRunner( Class<?> _class, NumericType _numericType ) throws InitializationError
		{
			super( _class );
			this.numericType = _numericType;
		}

		@Override
		public Object createTest() throws Exception
		{
			return getTestClass().getOnlyConstructor().newInstance( this.numericType );
		}

		@Override
		protected String getName()
		{
			return this.numericType.toString();
		}

		@Override
		protected String testName( final FrameworkMethod method )
		{
			return String.format( "%s[%s]", method.getName(), getName() );
		}

		@Override
		protected void validateConstructor( List<Throwable> errors )
		{
			validateOnlyOneConstructor( errors );
		}
	}
}
