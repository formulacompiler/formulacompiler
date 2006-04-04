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


/**
 * API to a computation engine. You normally use a {@link Compiler} to build one from a given
 * spreadsheet file, or else instantiate one constructed earlier from persistent storage. An engine
 * is like a Java class which can be instantiated multiple times as individual computations. This is
 * to make engines thread-safe. The individual computations, on the other hand, are not. Engines do
 * not depend on the spreadsheet model used to construct them, so they normally have a fairly low
 * memory footprint.
 * 
 * @author peo
 * @see Compiler
 */
public interface Engine
{

	/**
	 * Thread-safe factory method for specific, non-thread-safe computations.
	 * 
	 * @param _inputs is an instance of the input class passed to the engine's compiler during engine
	 *        definition. May only be null if the input class passed to the compiler was null, too.
	 * @return A new, non-thread safe computation of results for a given set of input values, like an
	 *         instance of an engine. Computations are not thread-safe, while engines are. You have
	 *         to cast the computation to the output type you supplied when defining the engine to
	 *         get at the computed values.
	 * 
	 */
	public abstract Object newComputation( Object _inputs );


}