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
package sej;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import sej.describable.AbstractDescribable;
import sej.describable.DescriptionBuilder;


/**
 * Represents a call to a method including the argument values for all of the method's parameters.
 * Can optionally represent a chain of calls. This class is immutable.
 * 
 * @author peo
 */
public final class CallFrame extends AbstractDescribable
{
	private static final Object[] NOARGS = new Object[ 0 ];
	private final CallFrame prev;
	private final Method method;
	private final Object[] args;


	private CallFrame(final CallFrame _prev, final Method _method, final Object... _args)
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
					throw new IllegalArgumentException( "Argument value of type '" + arg + "' not assignable to the parameter of type '" + param + "'" );
			}
			else {
				// LATER Check assignment compatibility of primitive types
			}
		}
	}

	/**
	 * Constructs an call, possibly the initial call in a chain of calls.
	 * 
	 * @param _method is the method to be called.
	 * @param _args is the list of arguments for the method's parameters.
	 */
	public CallFrame(final Method _method, final Object... _args)
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
	public CallFrame chain( final Method _method, final Object... _args )
	{
		failIfCannotChain( this.method.getReturnType(), _method );
		return new CallFrame( this, _method, _args );
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
		Object[] result = new Object[ this.args.length ];
		System.arraycopy( this.args, 0, result, 0, this.args.length );
		return result;
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
	 * The first call in the chain of calls.
	 * 
	 * @return The first call. Never null.
	 */
	public CallFrame getHead()
	{
		CallFrame result = this;
		while (null != result.prev)
			result = result.prev;
		return result;
	}


	/**
	 * The call frames arranged in proper order to be called one by one, starting with an object of
	 * the head's class.
	 * 
	 * @return A new array of frames. Never null.
	 * @see #call(CallFrame[], Object)
	 */
	public CallFrame[] getFrames()
	{
		int n = 0;
		for (CallFrame f = this; null != f; f = f.prev)
			n++;

		CallFrame[] result = new CallFrame[ n ];

		int i = n - 1;
		for (CallFrame f = this; null != f; f = f.prev)
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
	public static Object call( final CallFrame[] _frames, final Object _context ) throws InvocationTargetException,
			IllegalAccessException
	{
		Object result = _context;
		for (CallFrame frame : _frames) {
			if (null != frame) {
				result = frame.method.invoke( result, frame.args );
			}
		}
		return result;
	}


	@Override
	public void describeTo( DescriptionBuilder _to )
	{
		final CallFrame[] frames = getFrames();
		boolean chained = false;
		for (CallFrame frame : frames) {
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
		int result = getMethod().hashCode();
		for (Object arg : getArgs()) {
			result ^= arg.hashCode();
		}
		if (null != this.prev) {
			result ^= this.prev.hashCode();
		}
		return result;
	}


	@Override
	public boolean equals( Object _obj )
	{
		if (_obj instanceof CallFrame) {
			CallFrame other = (CallFrame) _obj;
			if (this.method.equals( other.method ) && this.args.length == other.args.length) {
				if (Arrays.equals( this.args, other.args )) {
					return this.prev == other.prev || (null != this.prev && this.prev.equals( other.prev ));
				}
			}
		}
		return false;
	}

}
