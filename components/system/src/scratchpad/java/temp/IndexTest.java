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
package temp;

public class IndexTest
{

	
	public static final class UsingArray
	{
		private double[] arr;
		
		private final double[] arr()
		{
			if (this.arr == null) {
				this.arr = new double[] {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
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
				case 0: return 10;
				case 1: return 20;
				case 2: return 30;
				case 3: return 40;
				case 4: return 50;
				case 5: return 60;
				case 6: return 70;
				case 7: return 80;
				case 8: return 90;
				case 9: return 100;
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
