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
import java.lang.reflect.Modifier;
import java.util.Map;

import org.formulacompiler.runtime.New;
import org.objectweb.asm.Type;


public final class Util
{

	private Util()
	{
		super();
	}


	public static void validateIsAccessible( Class _class, String _role )
	{
		ValidationImpl.SINGLETON_IMPL.validateIsAccessible( _class, _role );
	}

	public static void validateIsAccessible( Method _method, String _role )
	{
		ValidationImpl.SINGLETON_IMPL.validateIsAccessible( _method, _role );
	}

	public static void validateIsImplementable( Class _class, String _role )
	{
		ValidationImpl.SINGLETON_IMPL.validateIsImplementable( _class, _role );
	}

	public static void validateIsImplementable( Method _method, String _role )
	{
		ValidationImpl.SINGLETON_IMPL.validateIsImplementable( _method, _role );
	}

	public static void validateCallable( Class _class, Method _method )
	{
		ValidationImpl.SINGLETON_IMPL.validateCallable( _class, _method );
	}

	public static void validateFactory( Class _factoryClass, Method _factoryMethod, Class _inputClass, Class _outputClass )
	{
		ValidationImpl.SINGLETON_IMPL.validateFactory( _factoryClass, _factoryMethod, _inputClass, _outputClass );
	}


	public static String signatureOf( Method _m )
	{
		return Type.getMethodDescriptor( _m );
	}

	public static String nameAndSignatureOf( Method _m )
	{
		return _m.getName() + signatureOf( _m );
	}


	public static Map<String, Method> abstractMethodsOf( final Class _class )
	{
		final Map<String, Method> result = New.newMap();
		collectAbstractMethods( _class, result );
		return result;
	}

	private static void collectAbstractMethods( Class _class, Map<String, Method> _result )
	{
		final Class superclass = _class.getSuperclass();
		if (superclass != Object.class && superclass != null) {
			collectAbstractMethods( superclass, _result );
		}
		for (Class intf : _class.getInterfaces()) {
			collectInterfaceMethods( intf, _result );
		}
		for (Method m : _class.getDeclaredMethods()) {
			if (!Modifier.isStatic( m.getModifiers() )) {
				if (Modifier.isAbstract( m.getModifiers() )) {
					_result.put( Util.nameAndSignatureOf( m ), m );
				}
				// Assumption: checking the size is cheaper than building the signature:
				else if (_result.size() > 0) {
					_result.remove( Util.nameAndSignatureOf( m ) );
				}
			}
		}
	}

	private static void collectInterfaceMethods( Class _intf, Map<String, Method> _result )
	{
		for (Class intf : _intf.getInterfaces()) {
			collectInterfaceMethods( intf, _result );
		}
		for (Method m : _intf.getDeclaredMethods()) {
			_result.put( Util.nameAndSignatureOf( m ), m );
		}
	}


	/**
	 * Assert that we are running JUnit tests. Used for public methods that should only be used by
	 * tests. Simply tests that JUnit is loaded.
	 */
	public static void assertTesting()
	{
		try {
			Class.forName( "junit.framework.TestCase" );
		}
		catch (ClassNotFoundException e) {
			throw new IllegalStateException( "This method must only be used by JUnit tests." );
		}
	}

}
