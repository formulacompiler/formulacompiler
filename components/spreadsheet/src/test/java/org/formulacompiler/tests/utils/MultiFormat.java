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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

public class MultiFormat extends Suite
{
	static final String[][] FORMATS = new String[][]{
			new String[]{ ".xls", ".xlt" },
			new String[]{ ".xlsx", ".xltx" },
			new String[]{ ".ods", ".ots" },
	};

	private final List<Runner> runners = new ArrayList<Runner>();

	/**
	 * Only called reflectively. Do not use programmatically.
	 */
	public MultiFormat( Class<?> _class ) throws Throwable
	{
		super( _class, Collections.<Runner>emptyList() );
		for (String[] extensions : FORMATS) {
			this.runners.add( new MultiFormatClassRunner( getTestClass().getJavaClass(),
					extensions[ 0 ], extensions[ 1 ] ) );
		}
	}

	@Override
	protected List<Runner> getChildren()
	{
		return this.runners;
	}

	private static class MultiFormatClassRunner extends BlockJUnit4ClassRunner
	{
		private final String spreadsheetExtension;
		private final String templateExtension;

		public MultiFormatClassRunner( Class<?> _class, final String _spreadsheetExtension, final String _templateExtension ) throws InitializationError
		{
			super( _class );
			this.spreadsheetExtension = _spreadsheetExtension;
			this.templateExtension = _templateExtension;
		}

		@Override
		public Object createTest() throws Exception
		{
			final Constructor<?> constructor = getTestClass().getOnlyConstructor();
			if (constructor.getParameterTypes().length == 1) {
				return constructor.newInstance( this.spreadsheetExtension );
			}
			else {
				return constructor.newInstance( this.spreadsheetExtension, this.templateExtension );
			}
		}

		@Override
		protected void runChild( final FrameworkMethod method, final RunNotifier notifier )
		{
			final IgnoreFormat ignoreFormat = method.getAnnotation( IgnoreFormat.class );
			if (ignoreFormat != null) {
				for (String format : ignoreFormat.value()) {
					if (format.equals( this.spreadsheetExtension )) {
						final Description description = describeChild( method );
						notifier.fireTestIgnored( description );
						return;
					}
				}
			}
			super.runChild( method, notifier );
		}

		@Override
		protected String getName()
		{
			return this.spreadsheetExtension;
		}

		@Override
		protected String testName( final FrameworkMethod method )
		{
			return String.format( "%s[%s]", method.getName(), this.spreadsheetExtension );
		}

		@Override
		protected void validateConstructor( List<Throwable> errors )
		{
			validateOnlyOneConstructor( errors );
		}
	}
}
