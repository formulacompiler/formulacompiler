package sej;

import java.lang.reflect.Method;

import sej.runtime.ImplementationLocator;

interface Validation
{
	static final Validation SINGLETON = ImplementationLocator.getInstance( Factory.class ).getSingleton();

	void validateIsAccessible( Class _class, String _role );
	void validateIsAccessible( Method _method, String _role );
	void validateIsImplementable( Class _class, String _role );
	void validateIsImplementable( Method _method, String _role );
	void validateCallable( Class _class, Method _method );
	void validateFactory( Class _factoryClass, Method _factoryMethod, Class _inputClass, Class _outputClass );

	interface Factory
	{
		Validation getSingleton();
	}
}
