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
package org.formulacompiler.tests.reference.base;

abstract class SameExprRowSequenceTestSuite extends AbstractContextTestSuite
{

	public SameExprRowSequenceTestSuite( Context _cx )
	{
		super( _cx );
	}

	@Override
	protected String getOwnName()
	{
		final StringBuilder testSuiteName = new StringBuilder( "Row " );
		testSuiteName.append( cx().getRowIndex() + 1 );
		final String expr = cx().getOutputExpr();
		if (expr != null) {
			testSuiteName.append( ": " ).append( expr.replace( '(', '[' ).replace( ')', ']' ) );
		}
		return testSuiteName.toString();
	}


	@Override
	protected void setUp() throws Throwable
	{
		super.setUp();
		cx().getRowSetup().setupValues();
	}

	@Override
	protected void tearDown() throws Throwable
	{
		cx().releaseInputs();
		super.tearDown();
	}

}
