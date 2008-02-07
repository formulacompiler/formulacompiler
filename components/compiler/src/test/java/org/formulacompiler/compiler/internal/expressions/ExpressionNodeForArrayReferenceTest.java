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
package org.formulacompiler.compiler.internal.expressions;

import junit.framework.TestCase;

public class ExpressionNodeForArrayReferenceTest extends TestCase
{
	private ExpressionNodeForArrayReference array;


	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		final int nRow = 4;
		final int nCol = 5;
		this.array = new ExpressionNodeForArrayReference( new ArrayDescriptor( 1, nRow, nCol ) );
		for (int iRow = 1; iRow <= nRow; iRow++) {
			for (int iCol = 1; iCol <= nCol; iCol++) {
				this.array.addArgument( new ExpressionNodeForConstantValue( Integer.valueOf( iRow * 10 + iCol ) ) );
			}
		}
	}


	public void testSubArray()
	{
		assertEquals( "#(1,4,5){11, 12, 13, 14, 15, 21, 22, 23, 24, 25, 31, 32, 33, 34, 35, 41, 42, 43, 44, 45}",
				this.array.subArray( 0, 4, 0, 5 ).toString() );

		assertEquals( "#(1,1,5){11, 12, 13, 14, 15}", this.array.subArray( 0, 1, 0, 5 ).toString() );
		assertEquals( "#(1,1,5){31, 32, 33, 34, 35}", this.array.subArray( 2, 1, 0, 5 ).toString() );

		assertEquals( "#(1,4,1){11, 21, 31, 41}", this.array.subArray( 0, 4, 0, 1 ).toString() );
		assertEquals( "#(1,4,1){13, 23, 33, 43}", this.array.subArray( 0, 4, 2, 1 ).toString() );

		assertEquals( "#(1,2,2){23, 24, 33, 34}", this.array.subArray( 1, 2, 2, 2 ).toString() );
	}

}
