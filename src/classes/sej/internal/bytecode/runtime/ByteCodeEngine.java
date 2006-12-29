/*
 * Copyright © 2006 by Abacus Research AG, Switzerland.
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
package sej.internal.bytecode.runtime;

import java.util.HashMap;
import java.util.Map;

import sej.runtime.ComputationFactory;
import sej.runtime.Engine;
import sej.runtime.EngineException;

public class ByteCodeEngine extends ClassLoader implements Engine
{
	public static final String GEN_PACKAGE_NAME = "sej.gen.";
	public static final String GEN_FACTORY_NAME = "$Factory";

	protected final Map<String, byte[]> classNamesAndBytes = new HashMap<String, byte[]>();
	private final Class factoryClass;
	private final ComputationFactory factory;


	public ByteCodeEngine(ClassLoader _parentClassLoader, Map<String, byte[]> _classNamesAndBytes) throws EngineException
	{
		super( _parentClassLoader );
		assert _classNamesAndBytes != null;
		this.classNamesAndBytes.putAll( _classNamesAndBytes );
		try {
			this.factoryClass = loadClass( GEN_PACKAGE_NAME + GEN_FACTORY_NAME );
			this.factory = (ComputationFactory) this.factoryClass.newInstance();
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
	}


	public ComputationFactory getComputationFactory()
	{
		return this.factory;
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
