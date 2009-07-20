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

package temp;

public class IndexTest
{


	public static final class UsingArray
	{
		private double[] arr;

		private final double[] arr()
		{
			if (this.arr == null) {
				this.arr = new double[] { 10, 20, 30, 40, 50, 60, 70, 80, 90, 100 };
			}
			return this.arr;
		}

		public final double get1()
		{
			int i;
			return ((i = get2() - 1) >= 0 && i < 10) ? arr()[ i ] : 0;
		}

		private int get2()
		{
			return 5;
		}

	}


	public static final class UsingSwitch
	{
		public final double get1()
		{
			switch ((int) get2() - 1) {
				case 0:
					return 10;
				case 1:
					return 20;
				case 2:
					return 30;
				case 3:
					return 40;
				case 4:
					return 50;
				case 5:
					return 60;
				case 6:
					return 70;
				case 7:
					return 80;
				case 8:
					return 90;
				case 9:
					return 100;
				default:
					return 0;
			}
		}

		private double get2()
		{
			return 5;
		}

	}

}
