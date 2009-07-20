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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.formulacompiler.compiler.CallFrame;


/**
 * Represents a call to a method including the argument values for all of the method's parameters.
 * Can optionally represent a chain of calls. This class is immutable.
 * 
 * @author peo
 */
public final class CallFrameImpl extends AbstractDescribable implements CallFrame
{
	private static final Object[] NOARGS = new Object[ 0 ];
	private final CallFrameImpl prev;
	private final Method method;
	private final Object[] args;


	public static final class Factory implements CallFrame.Factory
	{
		public CallFrame newCallFrame( Method _method, Object... _args )
		{
			return new CallFrameImpl( _method, _args );
		}
	}


	private CallFrameImpl( final CallFrameImpl _prev, final Method _method, final Object... _args )
	{
		super();

		if (null == _method) throw new IllegalArgumentException( "Method cannot be null." );
		if (_prev != null) {
			if (!_method.getDeclaringClass().isAssignableFrom( _prev.getReturnType() )) {
				throw new IllegalArgumentException( "Method '"
						+ _method.getDeclaringClass().getName() + "." + _method.getName() + "' cannot be chained to method '"
						+ _prev.getMethod().getDeclaringClass().getName() + "." + _prev.getMethod().getName()
						+ "' returning '" + _prev.getReturnType().getName() + "'" );
			}
		}

		this.prev = _prev;
		this.method = _method;
		this.args = (null == _args) ? NOARGS : _args;

		final Class[] params = _method.getParameterTypes();
		if (this.args.length != params.length)
			throw new IllegalArgumentException( "Number of method arguments differs from number of parameters" );
		for (int i = 0; i < this.args.length; i++) {
			final Class<?> param = params[ i ];
			if (!param.isPrimitive()) {
				final Class<?> arg = this.args[ i ].getClass();
				if (!param.isAssignableFrom( arg ))
					throw new IllegalArgumentException( "Argument value of type '"
							+ arg + "' not assignable to the parameter of type '" + param + "'" );
			}
			else {
				// LATER Check assignment compatibility of primitive types
			}
		}
	}

	/**
	 * Constructs a call, possibly the initial call in a chain of calls.
	 * 
	 * @param _method is the method to be called.
	 * @param _args is the list of arguments for the method's parameters.
	 */
	public CallFrameImpl( final Method _method, final Object... _args )
	{
		this( null, _method, _args );
	}


	/**
	 * Checks if the method is callable on an object of the given class.
	 * 
	 * @param _context the class the method is to be called on.
	 * @param _method the method to be called.
	 * @return True if and only if the method can be called on an object of the given class.
	 */
	public static boolean canChain( final Class _context, final Method _method )
	{
		return _method.getDeclaringClass().isAssignableFrom( _context );
	}


	/**
	 * Fails if the method not is callable on an object of the given class.
	 * 
	 * @param _context the class the method is to be called on.
	 * @param _method the method to be called.
	 * @throws IllegalArgumentException
	 */
	public static void failIfCannotChain( final Class _context, final Method _method ) throws IllegalArgumentException
	{
		if (!canChain( _context, _method ))
			throw new IllegalArgumentException( "Method "
					+ _method.getName() + " is not a member of type " + _context.getName() + "." );
	}


	/**
	 * Constructs a chained call.
	 * 
	 * @param _method is the method to be called; must be callable on objects of the class returned
	 *           by the current call.
	 * @param _args is the list of arguments for the method's parameters.
	 * @return A new frame that links back to this one.
	 */
	public CallFrameImpl chain( final Method _method, final Object... _args )
	{
		failIfCannotChain( this.method.getReturnType(), _method );
		return new CallFrameImpl( this, _method, _args );
	}


	/**
	 * The method to call.
	 * 
	 * @return The method. Never null.
	 */
	public Method getMethod()
	{
		return this.method;
	}


	/**
	 * The list of argument values for the method's parameters.
	 * 
	 * @return The list. Never null.
	 */
	public Object[] getArgs()
	{
		assert (null != this.args);
		return this.args.clone();
	}


	/**
	 * The return type of the final call in the chain.
	 * 
	 * @return The type.
	 */
	public Class getReturnType()
	{
		return getMethod().getReturnType();
	}


	/**
	 * The previous call in the chain of calls.
	 * 
	 * @return The previous call, or {@code null}.
	 */
	public CallFrameImpl getPrev()
	{
		return this.prev;
	}


	/**
	 * The first call in the chain of calls.
	 * 
	 * @return The first call. Never null.
	 */
	public CallFrameImpl getHead()
	{
		CallFrameImpl result = this;
		while (null != result.prev)
			result = result.prev;
		return result;
	}


	/**
	 * The call frames arranged in proper order to be called one by one, starting with an object of
	 * the head's class.
	 * 
	 * @return A new array of frames. Never null.
	 * @see #call(CallFrameImpl[], Object)
	 */
	public CallFrameImpl[] getFrames()
	{
		int n = 0;
		for (CallFrameImpl f = this; null != f; f = f.prev)
			n++;

		CallFrameImpl[] result = new CallFrameImpl[ n ];

		int i = n - 1;
		for (CallFrameImpl f = this; null != f; f = f.prev)
			result[ i-- ] = f;

		return result;
	}


	/**
	 * Executes all the calls in the frame array, starting with the given object, and passing the
	 * result on to subsequent calls in the chain.
	 * 
	 * @param _frames List of call frames to call, one by one.
	 * @param _context Initial object to call the head of the chain on.
	 * @return The result of the last call in the chain.
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	public static Object call( final CallFrameImpl[] _frames, final Object _context ) throws InvocationTargetException,
			IllegalAccessException
	{
		Object result = _context;
		for (CallFrameImpl frame : _frames) {
			if (null != frame) {
				result = frame.method.invoke( result, frame.args );
			}
		}
		return result;
	}


	@Override
	public void describeTo( DescriptionBuilder _to )
	{
		final CallFrameImpl[] frames = getFrames();
		boolean chained = false;
		for (CallFrameImpl frame : frames) {
			if (chained) _to.append( '.' );
			frame.describeThisFrameTo( _to );
			chained = true;
		}
	}


	private void describeThisFrameTo( DescriptionBuilder _to )
	{
		_to.append( this.method.getName() );
		if (null == this.args || 0 == this.args.length) {
			_to.append( "()" );
		}
		else {
			_to.append( "( " );
			_to.append( this.args[ 0 ] );
			for (int i = 1; i < this.args.length; i++) {
				_to.append( ", " );
				_to.append( this.args[ i ] );
			}
			_to.append( " )" );
		}
	}


	@Override
	public int hashCode()
	{
		// Final, immutable class, so this should be SMP safe.
		if (this.hashCode == 0) {
			int hc = getMethod().hashCode();
			for (Object arg : getArgs()) {
				hc = hc * 59 + arg.hashCode();
			}
			if (null != this.prev) {
				hc = hc * 59 + this.prev.hashCode();
			}
			this.hashCode = hc;
		}
		return this.hashCode;
	}

	private transient int hashCode;


	@Override
	public boolean equals( Object _obj )
	{
		if (this == _obj) return true;
		if (_obj instanceof CallFrameImpl) {
			CallFrameImpl other = (CallFrameImpl) _obj;
			if (this.method.equals( other.method ) && this.args.length == other.args.length) {
				if (Arrays.equals( this.args, other.args )) {
					return this.prev == other.prev || (null != this.prev && this.prev.equals( other.prev ));
				}
			}
		}
		return false;
	}

}
