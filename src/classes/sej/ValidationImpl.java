package sej;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import sej.internal.Util;

/**
 * This class is in package "sej" because it needs to be able to inherit "Validation", which is
 * package visible.
 */
public final class ValidationImpl implements Validation
{
	public static final ValidationImpl SINGLETON_IMPL = new ValidationImpl();
	private static final String ACCESSIBLE = "; cannot be accessed by SEJ";
	private static final String IMPLEMENTABLE = "; cannot be implemented by SEJ";

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
		validate( Modifier.isPublic( mods ), _class, _role, "not public", ACCESSIBLE );
	}

	public void validateIsAccessible( Method _method, String _role )
	{
		final int mods = _method.getModifiers();
		validate( Modifier.isPublic( mods ), _method, _role, "not public", ACCESSIBLE );
	}


	public void validateIsImplementable( Class _class, String _role )
	{
		final int mods = _class.getModifiers();
		validate( Modifier.isPublic( mods ), _class, _role, "not public", IMPLEMENTABLE );
		validate( !Modifier.isFinal( mods ), _class, _role, "final", IMPLEMENTABLE );
	}

	public void validateIsImplementable( Method _method, String _role )
	{
		final int mods = _method.getModifiers();
		validate( Modifier.isPublic( mods ), _method, _role, "not public", IMPLEMENTABLE );
		validate( !Modifier.isStatic( mods ), _method, _role, "static", IMPLEMENTABLE );
		validate( !Modifier.isFinal( mods ), _method, _role, "final", IMPLEMENTABLE );
	}

	private void validate( boolean _mustBeTrue, Class _class, String _role, String _what, String _whatFor )
	{
		if (!_mustBeTrue) throw new IllegalArgumentException( _role + " " + _class + " is " + _what + _whatFor );
	}

	private void validate( boolean _mustBeTrue, Method _method, String _role, String _what, String _whatFor )
	{
		if (!_mustBeTrue) throw new IllegalArgumentException( _role + " " + _method + " is " + _what + _whatFor );
	}


	public void validateCallable( Class _class, Method _method )
	{
		if (!_method.getDeclaringClass().isAssignableFrom( _class ))
			throw new IllegalArgumentException( "Method '"
					+ _method + "' cannot be called on an object of type '" + _class + "'" );
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
			abstracts.remove( "newComputation(Ljava/lang/Object;)Lsej/Computation;" );
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


}
