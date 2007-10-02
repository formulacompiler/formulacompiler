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
package org.formulacompiler.runtime.internal.bytecode;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;

import org.formulacompiler.runtime.Computation;
import org.formulacompiler.runtime.ComputationFactory;
import org.formulacompiler.runtime.Engine;
import org.formulacompiler.runtime.EngineException;
import org.formulacompiler.runtime.New;
import org.formulacompiler.runtime.internal.Environment;


public class ByteCodeEngine extends ClassLoader implements Engine
{
	public static final String GEN_PACKAGE_NAME = "org.formulacompiler.gen.";
	public static final String GEN_FACTORY_NAME = "$Factory";

	private final Map<String, byte[]> classNamesAndBytes = New.map();
	private final Class<ComputationFactory> factoryClass;
	private final Constructor<ComputationFactory> factoryConstructor;
	private final ComputationFactory defaultFactory;


	@SuppressWarnings("unchecked")
	public ByteCodeEngine(ClassLoader _parentClassLoader, Map<String, byte[]> _classNamesAndBytes)
			throws EngineException
	{
		super( _parentClassLoader );
		assert _classNamesAndBytes != null;
		this.classNamesAndBytes.putAll( _classNamesAndBytes );
		try {

			/*
			 * Force all classes to be loaded from this context, overriding similarly named classes in
			 * the parent class loader.
			 */
			for (String className : _classNamesAndBytes.keySet()) {
				findClass( className );
			}

			final String factoryClassName = GEN_PACKAGE_NAME + GEN_FACTORY_NAME;
			this.factoryClass = (Class<ComputationFactory>) loadClass( factoryClassName );
			assert this.factoryClass.getClassLoader() == this : "Class loader mismatch";
			this.factoryConstructor = this.factoryClass.getDeclaredConstructor( Environment.class );
			this.defaultFactory = this.factoryConstructor.newInstance( Environment.DEFAULT );
		}
		catch (ClassNotFoundException e) {
			throw new EngineException( e );
		}
		catch (InstantiationException e) {
			throw new EngineException( e );
		}
		catch (IllegalAccessException e) {
			throw new EngineException( e );
		}
		catch (SecurityException e) {
			throw new EngineException( e );
		}
		catch (NoSuchMethodException e) {
			throw new EngineException( e );
		}
		catch (InvocationTargetException e) {
			throw new EngineException( e );
		}
	}


	public ComputationFactory getComputationFactory()
	{
		return this.defaultFactory;
	}


	public ComputationFactory getComputationFactory( Computation.Config _cfg )
	{
		try {
			return this.factoryConstructor.newInstance( Environment.getInstance( _cfg ) );
		}
		/*
		 * I'm throwing IllegalStateException here because the instantiation already worked once in
		 * the constructor above, so it's not supposed to fail here.
		 */
		catch (InstantiationException e) {
			throw new IllegalStateException( e );
		}
		catch (IllegalAccessException e) {
			throw new IllegalStateException( e );
		}
		catch (InvocationTargetException e) {
			throw new IllegalStateException( e );
		}
	}


	public Map<String, byte[]> getClassNamesAndBytes()
	{
		return Collections.unmodifiableMap( this.classNamesAndBytes );
	}


	@Override
	protected Class<?> findClass( String _name ) throws ClassNotFoundException
	{
		final byte[] bytes = this.classNamesAndBytes.get( _name );
		if (bytes != null) {
			return defineClass( _name, bytes, 0, bytes.length );
		}
		else {
			return super.findClass( _name );
		}
	}

}
