/*
 * Copyright � 2006 by Abacus Research AG, Switzerland.
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
package sej.compiler;

import java.lang.reflect.Method;

import sej.runtime.ImplementationLocator;

/**
 * PRIVATE INTERFACE - DO NOT USE.
 * 
 * @author peo
 */
public interface Validation
{
	static final Validation SINGLETON = ImplementationLocator.getInstance( Factory.class ).getSingleton();

	void validateIsAccessible( Class _class, String _role );
	void validateIsAccessible( Method _method, String _role );
	void validateIsImplementable( Class _class, String _role );
	void validateIsImplementable( Method _method, String _role );
	void validateCallable( Class _class, Method _method );
	void validateFactory( Class _factoryClass, Method _factoryMethod, Class _inputClass, Class _outputClass );

	/**
	 * PRIVATE INTERFACE - DO NOT USE.
	 * 
	 * @author peo
	 */
	interface Factory
	{
		Validation getSingleton();
	}
}