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

package org.formulacompiler.runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;

/**
 * Very simple service locator for AFC. Uses the same mechanism as Java 6's
 * {@code java.util.ServiceLoader} to locate implementation classes, that is, files of the desired
 * class's name in "META-INF/services/" containing lines which specify the implementor class names.
 * <p>
 * It currently does not use {@code java.util.ServiceLoader} internally because I do not yet want to
 * assume Java 6.
 * 
 * @author peo
 */
public final class ImplementationLocator
{
	private static final Map<Class, Collection> implementationClasses = New.map();


	/**
	 * Returns a newly constructed instance of the implementor class of the given abstract class or
	 * interface. The implementor is looked up in a file with the name
	 * "META-INF/services/[class-name]". All non-comment lines should specify fully qualified class
	 * names. Comments start with "#".
	 * 
	 * @param <T> is the desired return type.
	 * @param _class is the class of the desired return type.
	 * @return an instance of T.
	 * 
	 * @throws ConfigurationException for all internal declared exceptions, with the cause set to the
	 *            internal exception.
	 * @throws ConfigurationMissingException when the configuration file is missing.
	 */
	public static <T> T getInstance( Class<T> _class ) throws ConfigurationException, ConfigurationMissingException
	{
		try {
			return getImplementationClass( _class ).newInstance();
		}
		catch (InstantiationException e) {
			throw new ConfigurationException( _class, e );
		}
		catch (IllegalAccessException e) {
			throw new ConfigurationException( _class, e );
		}
	}


	/**
	 * Like {@link #getInstance(Class)}, but returns a collection of all matching implementations.
	 * 
	 * @param <T> is the desired return type.
	 * @param _class is the class of the desired return type.
	 * @return a collection of instances of T. Never null, never shared, mutable.
	 * 
	 * @throws ConfigurationException for all internal declared exceptions, with the cause set to the
	 *            internal exception.
	 */
	public static <T> Collection<T> getInstances( Class<T> _class ) throws ConfigurationException
	{
		final Collection<T> result = New.collection();
		final Collection<Class<T>> clazzes = getImplementationClasses( _class );
		try {
			for (Class<T> clazz : clazzes) {
				result.add( clazz.newInstance() );
			}
		}
		catch (InstantiationException e) {
			throw new ConfigurationException( _class, e );
		}
		catch (IllegalAccessException e) {
			throw new ConfigurationException( _class, e );
		}
		return result;
	}


	/**
	 * Exception thrown when configuration errors occur.
	 */
	public static class ConfigurationException extends RuntimeException
	{

		ConfigurationException( Class _class, Throwable _throwable )
		{
			super( "Configuration bad for " + _class.getName() + "; " + _throwable.getMessage(), _throwable );
		}

		ConfigurationException( Class _class, String _message )
		{
			super( "Configuration bad for " + _class.getName() + "; " + _message );
		}

	}


	/**
	 * Exception thrown when a configuration is missing for a single instance request.
	 */
	public static final class ConfigurationMissingException extends ConfigurationException
	{

		ConfigurationMissingException( Class _class, String _message )
		{
			super( _class, _message );
		}

	}


	private static synchronized <T, I extends T> Class<I> getImplementationClass( Class<T> _class )
	{
		final Collection<Class<I>> clazzes = getImplementationClasses( _class );
		if (clazzes.size() == 1) {
			return clazzes.iterator().next();
		}
		else if (clazzes.size() == 0) {
			throw new ConfigurationMissingException( _class, "no implementor configured." );
		}
		else {
			throw new ConfigurationException( _class, "ambiguous request; multiple implementors configured." );
		}
	}


	@SuppressWarnings( "unchecked" )
	private static synchronized <T, I extends T> Collection<Class<I>> getImplementationClasses( Class<T> _class )
	{
		/*
		 * I synchronize only during actual map access so reentrant accesses to the locator caused by
		 * class loading do not block. This might conceivably lead to two threads loading a config in
		 * parallel, but so what.
		 */
		synchronized (implementationClasses) {
			final Collection cached = implementationClasses.get( _class );
			if (cached != null) {
				return cached;
			}
		}

		final Collection<Class<I>> loaded = loadImplementationClasses( _class );

		synchronized (implementationClasses) {
			implementationClasses.put( _class, loaded );
		}
		return loaded;
	}


	private static <T, I extends T> Collection<Class<I>> loadImplementationClasses( Class<T> _class )
	{
		try {
			final Collection<Class<I>> result = New.collection();
			final String configName = "META-INF/services/" + _class.getName();
			final ClassLoader classLoader = ImplementationLocator.class.getClassLoader();
			final Enumeration<URL> resources = classLoader.getResources( configName );
			while (resources.hasMoreElements()) {
				loadImplementationClassesFrom( _class, resources.nextElement(), result );
			}
			return result;
		}
		catch (IOException e) {
			throw new ConfigurationException( _class, e );
		}
	}


	@SuppressWarnings( "unchecked" )
	private static <T, I extends T> void loadImplementationClassesFrom( Class<T> _class, URL _configURL,
			Collection<Class<I>> _into )
	{
		try {
			final InputStream configStream = _configURL.openStream();
			try {
				final BufferedReader configReader = new BufferedReader( new InputStreamReader( configStream, "utf-8" ) );
				String line;
				while ((line = configReader.readLine()) != null) {
					final int commentAt = line.indexOf( '#' );
					if (commentAt >= 0) line = line.substring( 0, commentAt );
					line = line.trim();
					if (line.length() != 0) {
						final String className = line;
						_into.add( (Class<I>) Class.forName( className ) );
					}
				}
			}
			finally {
				configStream.close();
			}
		}
		catch (IOException e) {
			throw new ConfigurationException( _class, e );
		}
		catch (ClassNotFoundException e) {
			throw new ConfigurationException( _class, e );
		}
	}


}
