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

import junit.framework.TestCase;

abstract class AbstractContextTestCase extends TestCase
{
	private final Context cx;

	public AbstractContextTestCase( Context _cx )
	{
		super( null );
		this.cx = _cx;
	}

	public final Context cx()
	{
		return this.cx;
	}

	public final TestCase init()
	{
		return this;
	}

	protected final void assertContains( final String _string, final String _part )
	{
		if (!_string.contains( _part )) {
			fail( "<" + _string + "> does not contain <" + _part + ">" );
		}
	}


	@Override
	public final String getName()
	{
		return getOwnName();
	}

	/**
	 * Accessed by Ant's test error formatter, so return the full name.
	 */
	@Override
	public String toString()
	{
		return getOwnName() + " in " + cx().getDescription();
	}

	protected abstract String getOwnName();

}
