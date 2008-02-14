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

package org.formulacompiler.compiler.internal;

import junit.framework.TestCase;

public class DescriptionBuilderTest extends TestCase
{


	public void testIndentation()
	{
		DescriptionBuilder d = new DescriptionBuilder( "\t" );
		d.appendLine( "Hello" );
		d.append( "world" );
		d.append( '!' );

		// indent/outdent after newline
		d.newLine();
		d.indent();
		d.append( "first indent" );
		d.newLine();
		d.append( "..." );
		d.newLine();
		d.outdent();

		d.append( "normal" );

		// newline after indent/outdent
		d.indent();
		d.newLine();
		d.append( "second indent" );
		d.newLine();
		d.append( "..." );
		d.outdent();
		d.newLine();

		d.appendLine( "normal again" );

		String s = d.toString();

		assertEquals( "Hello\nworld!\n\tfirst indent\n\t...\nnormal\n\tsecond indent\n\t...\nnormal again\n", s );
	}


	public void testContext() throws Exception
	{
		DescriptionBuilder d = new DescriptionBuilder();
		String s1 = "One";
		String s2 = "Two";
		Integer i1 = 1;
		Integer i2 = 2;

		assertNull( d.getContext( String.class ) );
		assertNull( d.getContext( Integer.class ) );

		d.pushContext( i1 );
		assertNull( d.getContext( String.class ) );
		assertEquals( i1, d.getContext( Integer.class ) );

		d.pushContext( s1 );
		assertEquals( s1, d.getContext( String.class ) );
		assertEquals( i1, d.getContext( Integer.class ) );

		d.pushContext( i2 );
		assertEquals( s1, d.getContext( String.class ) );
		assertEquals( i2, d.getContext( Integer.class ) );

		d.pushContext( s2 );
		assertEquals( s2, d.getContext( String.class ) );
		assertEquals( i2, d.getContext( Integer.class ) );

		d.popContext();
		assertEquals( s1, d.getContext( String.class ) );
		assertEquals( i2, d.getContext( Integer.class ) );

		d.popContext();
		assertEquals( s1, d.getContext( String.class ) );
		assertEquals( i1, d.getContext( Integer.class ) );

		d.popContext();
		assertNull( d.getContext( String.class ) );
		assertEquals( i1, d.getContext( Integer.class ) );

		d.popContext();
		assertNull( d.getContext( String.class ) );
		assertNull( d.getContext( Integer.class ) );

	}


}
