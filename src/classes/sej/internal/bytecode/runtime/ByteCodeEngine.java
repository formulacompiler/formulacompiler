package sej.internal.bytecode.runtime;

import java.util.HashMap;
import java.util.Map;

import sej.ComputationFactory;
import sej.Engine;
import sej.EngineError;

public class ByteCodeEngine extends ClassLoader implements Engine
{
	public static final String GEN_PACKAGE_NAME = "sej.gen.";
	public static final String GEN_FACTORY_NAME = "$Factory";

	protected final Map<String, byte[]> classNamesAndBytes = new HashMap<String, byte[]>();
	private final Class factoryClass;
	private final ComputationFactory factory;


	public ByteCodeEngine(Map<String, byte[]> _classNamesAndBytes) throws EngineError
	{
		super();
		assert _classNamesAndBytes != null;
		this.classNamesAndBytes.putAll( _classNamesAndBytes );
		try {
			this.factoryClass = loadClass( GEN_PACKAGE_NAME + GEN_FACTORY_NAME );
			this.factory = (ComputationFactory) this.factoryClass.newInstance();
		}
		catch (ClassNotFoundException e) {
			throw new EngineError( e );
		}
		catch (InstantiationException e) {
			throw new EngineError( e );
		}
		catch (IllegalAccessException e) {
			throw new EngineError( e );
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
