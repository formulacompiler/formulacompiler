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

package org.formulacompiler.tests.time;

import java.util.Date;

import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.tests.utils.MultiFormatAndNumericType;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;

@RunWith( Enclosed.class )
public class TimeTest
{
	@RunWith( MultiFormatAndNumericType.class )
	public static class NonCachedTimeTest extends AbstractTimeTest
	{
		public NonCachedTimeTest( String _spreadsheetExtension, NumericType _numericType )
		{
			super( _spreadsheetExtension, _numericType, Outputs.class );
		}
	}


	@RunWith( MultiFormatAndNumericType.class )
	public static class CachedTimeTest extends AbstractTimeTest
	{
		public CachedTimeTest( String _spreadsheetExtension, NumericType _numericType )
		{
			super( _spreadsheetExtension, _numericType, ResettableOutputs.class );
		}

		@Test
		public void testResetSameCell() throws Exception
		{
			final ResettableOutputs output = (ResettableOutputs) getOutputs();
			final Date date1 = output.now1();
			Thread.sleep( 1000 );
			output.reset();
			final Date date2 = output.now1();
			assertFalse( "Times must be different", date1.equals( date2 ) );
		}

		@Test
		public void testResetDifferentCells() throws Exception
		{
			final ResettableOutputs output = (ResettableOutputs) getOutputs();
			final Date date1 = output.now1();
			Thread.sleep( 1000 );
			output.reset();
			final Date date2 = output.now2();
			assertFalse( "Times must be different", date1.equals( date2 ) );
		}
	}
}
