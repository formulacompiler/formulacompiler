/*
 * Copyright (c) 2006-2009 by Abacus Research AG, Switzerland.
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


public class ByteCodeEngine implements Engine
{
	public static final String GEN_PACKAGE_NAME = "org.formulacompiler.gen.";
	public static final String GEN_FACTORY_NAME = "$Factory";

	private final Map<String, byte[]> classNamesAndBytes = New.map();
	private final Class<ComputationFactory> factoryClass;
	private final Constructor<ComputationFactory> factoryConstructor;
	private final ComputationFactory defaultFactory;

	
	private final class EngineClassLoader extends ClassLoader
	{

		private EngineClassLoader( ClassLoader _parent )
		{
			super( _parent );
		}

		@Override
		public Class<?> findClass( String _name ) throws ClassNotFoundException
		{
			final byte[] bytes = getClassNamesAndBytes().get( _name );
			if (bytes != null) {
				return defineClass( _name, bytes, 0, bytes.length );
			}
			else {
				return super.findClass( _name );
			}
		}

	}


	@SuppressWarnings( "unchecked" )
	public ByteCodeEngine( ClassLoader _parentClassLoader, Map<String, byte[]> _classNamesAndBytes )
			throws EngineException
	{
		super();
		assert _classNamesAndBytes != null;
		this.classNamesAndBytes.putAll( _classNamesAndBytes );

		final EngineClassLoader classLoader = new EngineClassLoader( _parentClassLoader );
		try {

			/*
			 * Force all classes to be loaded from this context, overriding similarly named classes in
			 * the parent class loader.
			 */
			for (String className : _classNamesAndBytes.keySet()) {
				classLoader.findClass( className );
			}

			final String factoryClassName = GEN_PACKAGE_NAME + GEN_FACTORY_NAME;
			this.factoryClass = (Class<ComputationFactory>) classLoader.loadClass( factoryClassName );
			assert this.factoryClass.getClassLoader() == classLoader: "Class loader mismatch";
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


}
