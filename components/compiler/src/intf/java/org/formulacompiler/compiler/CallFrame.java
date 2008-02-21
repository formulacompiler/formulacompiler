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

package org.formulacompiler.compiler;

import java.lang.reflect.Method;


/**
 * Represents a call to a method including the argument values for all of the method's parameters.
 * Can optionally represent a chain of calls. Implementations must be immutable.
 * <p>
 * <em>This interface is an API only. Do not implement it yourself.</em>
 * 
 * @see org.formulacompiler.spreadsheet.EngineBuilder#newCallFrame(Method, Object...)
 * @see FormulaCompiler#newCallFrame(Method, Object...)
 * 
 * @author peo
 */
public interface CallFrame extends Describable
{


	/**
	 * Constructs instances of {@link CallFrame}.
	 * 
	 * @author peo
	 */
	public interface Factory
	{

		/**
		 * Constructs a call, possibly the initial call in a chain of calls.
		 * 
		 * @param _method is the method to be called.
		 * @param _args is the list of arguments for the method's parameters.
		 * 
		 * @see org.formulacompiler.spreadsheet.EngineBuilder#newCallFrame(Method, Object...)
		 * @see FormulaCompiler#newCallFrame(Method, Object...)
		 */
		public CallFrame newCallFrame( Method _method, Object... _args );

	}


	/**
	 * Constructs a chained call.
	 * 
	 * @param _method is the method to be called; must be callable on objects of the class returned
	 *           by the current call.
	 * @param _args is the list of arguments for the method's parameters.
	 * @return A new frame that links back to this one.
	 */
	public CallFrame chain( Method _method, Object... _args );


	/**
	 * The method to call.
	 * 
	 * @return The method. Never null.
	 */
	public Method getMethod();


	/**
	 * The list of argument values for the method's parameters.
	 * 
	 * @return The list. Never null.
	 */
	public Object[] getArgs();


	/**
	 * The return type of the final call in the chain.
	 * 
	 * @return The type.
	 */
	public Class getReturnType();


	/**
	 * The previous call in the chain of calls.
	 * 
	 * @return The previous call, or {@code null}.
	 */
	public CallFrame getPrev();


	/**
	 * The first call in the chain of calls.
	 * 
	 * @return The first call. Never null.
	 */
	public CallFrame getHead();


	/**
	 * The call frames arranged in proper order to be called one by one, starting with an object of
	 * the head's class.
	 * 
	 * @return A new array of frames. Never null.
	 */
	public CallFrame[] getFrames();


}
