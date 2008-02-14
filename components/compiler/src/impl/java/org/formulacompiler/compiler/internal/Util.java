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
		final Map<String, Method> result = New.map();
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
