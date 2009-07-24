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

package org.formulacompiler.tutorials;

import java.math.BigDecimal;

import org.formulacompiler.tests.MultiFormatTestFactory;

public abstract class AbstractUsingBigDecimalTest extends MultiFormatTestFactory.SpreadsheetFormatTestCase
{

	protected String getPath()
	{
		return "src/test/data/org/formulacompiler/tutorials/UsingNumericTypes" + getSpreadsheetExtension();
	}


	// DO NOT REFORMAT BELOW THIS LINE
	// ---- IO
	public static class Input
	{
		public Input( double a, double b ) { this.a = BigDecimal.valueOf( a ); this.b = BigDecimal.valueOf( b ); }
		public /**/BigDecimal/**/ getA() { return this.a; }
		public /**/BigDecimal/**/ getB() { return this.b; }
		private final BigDecimal a;
		private final BigDecimal b;
	}

	public static interface Output
	{
		/**/BigDecimal/**/ getResult();
		/**/BigDecimal/**/ getNegated();
	}
	// ---- IO
	// DO NOT REFORMAT ABOVE THIS LINE

	public static interface Factory
	{
		Output newInstance( Input _input );
	}

}
