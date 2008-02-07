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

import org.formulacompiler.compiler.Validation;
import org.formulacompiler.runtime.Computation;
import org.objectweb.asm.Type;


public final class ValidationImpl implements Validation
{
	private static final String DEFAULT_NEWCOMPUTATION = "newComputation(Ljava/lang/Object;)"
			+ Type.getDescriptor( Computation.class );
	public static final ValidationImpl SINGLETON_IMPL = new ValidationImpl();
	private static final String ACCESSIBLE = "; cannot be accessed by Abacus Formula Compiler";
	private static final String IMPLEMENTABLE = "; cannot be implemented by Abacus Formula Compiler";

	public final static class Factory implements Validation.Factory
	{
		public Validation getSingleton()
		{
			return SINGLETON_IMPL;
		}
	}

	public void validateIsAccessible( Class _class, String _role )
	{
		final int mods = _class.getModifiers();
		validate( Modifier.isPublic( mods ), _class, _role, "is not public", ACCESSIBLE );
	}

	public void validateIsAccessible( Method _method, String _role )
	{
		final int mods = _method.getModifiers();
		validate( Modifier.isPublic( mods ), _method, _role, "is not public", ACCESSIBLE );
		validateNoCheckedExceptions( _method, _role );
	}


	public void validateIsImplementable( Class _class, String _role )
	{
		final int mods = _class.getModifiers();
		validate( Modifier.isPublic( mods ), _class, _role, "is not public", IMPLEMENTABLE );
		validate( !Modifier.isFinal( mods ), _class, _role, "is final", IMPLEMENTABLE );
	}

	public void validateIsImplementable( Method _method, String _role )
	{
		final int mods = _method.getModifiers();
		validate( Modifier.isPublic( mods ), _method, _role, "is not public", IMPLEMENTABLE );
		validate( !Modifier.isStatic( mods ), _method, _role, "is static", IMPLEMENTABLE );
		validate( !Modifier.isFinal( mods ), _method, _role, "is final", IMPLEMENTABLE );
	}

	private void validate( boolean _mustBeTrue, Class _class, String _role, String _what, String _whatFor )
	{
		if (!_mustBeTrue) throw new IllegalArgumentException( _role + " " + _class + " " + _what + _whatFor );
	}

	private void validate( boolean _mustBeTrue, Method _method, String _role, String _what, String _whatFor )
	{
		if (!_mustBeTrue) throw new IllegalArgumentException( _role + " " + _method + " " + _what + _whatFor );
	}


	public void validateCallable( Class _class, Method _method )
	{
		if (!_method.getDeclaringClass().isAssignableFrom( _class ))
			throw new IllegalArgumentException( "Method '"
					+ _method + "' cannot be called on an object of type '" + _class + "'" );
		validateNoCheckedExceptions( _method, "Input" );
	}


	public void validateFactory( Class _factoryClass, Method _factoryMethod, Class _inputClass, Class _outputClass )
	{
		if (_factoryClass != null) {
			validateIsImplementable( _factoryClass, "factoryClass" );

			if (_factoryMethod == null) throw new IllegalArgumentException( "factoryMethod not specified" );
			validateIsImplementable( _factoryMethod, "factoryMethod" );

			if (!_factoryMethod.getReturnType().isAssignableFrom( _outputClass ))
				throw new IllegalArgumentException( "factoryMethod '"
						+ _factoryMethod + "' does not return outputClass '" + _outputClass + "'" );
			final Class[] params = _factoryMethod.getParameterTypes();
			if (params.length != 1 || params[ 0 ] != _inputClass)
				throw new IllegalArgumentException( "factoryMethod '"
						+ _factoryMethod + "' does not have single parameter of type inputClass '" + _inputClass + "'" );

			final Map<String, Method> abstracts = Util.abstractMethodsOf( _factoryClass );
			abstracts.remove( DEFAULT_NEWCOMPUTATION );
			abstracts.remove( Util.nameAndSignatureOf( _factoryMethod ) );
			if (abstracts.size() > 0) {
				throw new IllegalArgumentException(
						"factoryClass is still abstract after implementing factoryMethod; offending method is '"
								+ abstracts.values().iterator().next() + "'" );
			}
		}
		else if (_factoryMethod != null) {
			throw new IllegalArgumentException( "factoryMethod is set, but factoryClass is not" );
		}
	}


	@SuppressWarnings( "unchecked" )
	private void validateNoCheckedExceptions( Method _method, String _role )
	{
		final Class<Throwable>[] thrown = (Class<Throwable>[]) _method.getExceptionTypes();
		for (int i = 0; i < thrown.length; i++) {
			final Class<Throwable> t = thrown[ i ];
			validate( RuntimeException.class.isAssignableFrom( t ), _method, _role, "throws checked exceptions",
					ACCESSIBLE );
		}
	}

}
