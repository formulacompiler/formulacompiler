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
package org.formulacompiler.compiler;

import java.lang.reflect.Method;


/**
 * Represents a call to a method including the argument values for all of the method's parameters.
 * Can optionally represent a chain of calls. Implementations must be immutable.
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
