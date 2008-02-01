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
package org.formulacompiler.compiler.internal;

import org.formulacompiler.compiler.internal.DescriptionBuilder;

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
		assertEquals( i1, d.getContext( Integer.class ));
		
		d.pushContext( s1 );
		assertEquals( s1, d.getContext( String.class ));
		assertEquals( i1, d.getContext( Integer.class ));
		
		d.pushContext( i2 );
		assertEquals( s1, d.getContext( String.class ));
		assertEquals( i2, d.getContext( Integer.class ));

		d.pushContext( s2 );
		assertEquals( s2, d.getContext( String.class ));
		assertEquals( i2, d.getContext( Integer.class ));
		
		d.popContext();
		assertEquals( s1, d.getContext( String.class ));
		assertEquals( i2, d.getContext( Integer.class ));
		
		d.popContext();
		assertEquals( s1, d.getContext( String.class ));
		assertEquals( i1, d.getContext( Integer.class ));
		
		d.popContext();
		assertNull( d.getContext( String.class ) );
		assertEquals( i1, d.getContext( Integer.class ));
		
		d.popContext();
		assertNull( d.getContext( String.class ) );
		assertNull( d.getContext( Integer.class ) );

	}


}