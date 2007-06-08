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

import java.lang.reflect.Method;

import junit.framework.TestCase;

public class UtilTest extends TestCase
{
	private static final Method intfMethod = getMethod( TestIntf.class, "sth" );
	private static final Method moreMethod = getMethod( TestClass.class, "more" );
	private static final Method otherMethod = getMethod( TestDescendant.class, "other" );

	private static Method getMethod( Class<?> _class, String _name )
	{
		try {
			return _class.getMethod( _name );
		}
		catch (Exception e) {
			throw new RuntimeException( e );
		}
	}


	public void testValidateCallable() throws Exception
	{
		Util.validateCallable( TestIntf.class, intfMethod );
		Util.validateCallable( TestClass.class, intfMethod );
		Util.validateCallable( TestClass.class, moreMethod );
		Util.validateCallable( TestDescendant.class, intfMethod );
		Util.validateCallable( TestDescendant.class, moreMethod );
		Util.validateCallable( TestDescendant.class, otherMethod );

		try {
			Util.validateCallable( TestIntf.class, moreMethod );
			fail();
		}
		catch (IllegalArgumentException e) {
			//
		}

		try {
			Util.validateCallable( TestClass.class, otherMethod );
			fail();
		}
		catch (IllegalArgumentException e) {
			//
		}
	}

	static interface TestIntf
	{
		void sth();
	}

	static class TestClass implements TestIntf
	{
		public void sth()
		{
			//
		}
		public void more()
		{
			//
		}
	}

	static class TestDescendant extends TestClass
	{
		public void other()
		{
			//
		}
	}


	private static final Method[] sigIntfMethods = SigIntf.class.getMethods();

	public void testSignatureOf() throws Exception
	{
		assertSig( "()V", "none" );
		assertSig( "()I", "anInt" );
		assertSig( "(I)I", "withInt" );
		assertSig( "(II)I", "withInts" );
		assertSig( "(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;", "withObjStr" );
	}

	static interface SigIntf
	{
		void none();
		int anInt();
		int withInt( int a );
		int withInts( int a, int b );
		Object withObjStr( Object a, String b );
	}

	private void assertSig( String _sig, String _mtd )
	{
		assertEquals( _sig, Util.signatureOf( getSigMethod( _mtd ) ) );
	}

	private Method getSigMethod( String _name )
	{
		for (Method m : sigIntfMethods) {
			if (m.getName().equals( _name )) return m;
		}
		return null;
	}


	public void testAbstractMethodsOf() throws Exception
	{
		assertEquals( "AbstractTestClass", 3, Util.abstractMethodsOf( AbstractTestClass.class ).size() );
		assertEquals( "NonAbstractTestClass", 0, Util.abstractMethodsOf( NonAbstractTestClass.class ).size() );
	}

	static abstract class AbstractTestBase
	{
		abstract void notTangible();
	}

	static interface AbstractTestIntf extends TestIntf
	{
		void other();
	}

	static abstract class AbstractTestClass extends AbstractTestBase implements AbstractTestIntf
	{
		//
	}

	static class NonAbstractTestClass extends AbstractTestClass
	{
		@Override
		void notTangible()
		{
			// 
		}
		public void sth()
		{
			// 
		}
		public void other()
		{
			// 
		}
	}

}
