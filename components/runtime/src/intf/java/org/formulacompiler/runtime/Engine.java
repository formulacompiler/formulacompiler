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
package org.formulacompiler.runtime;


/**
 * API to a computation engine. You normally use a
 * {@link org.formulacompiler.spreadsheet.EngineBuilder} to build one from a given spreadsheet file,
 * or else instantiate one constructed earlier from persistent storage using
 * {@link FormulaRuntime#loadEngine(java.io.InputStream)}, without requiring access to the original
 * spreadsheet file. An engine manages a computation factory, from which you instantiate individual
 * computations.
 * 
 * @author peo
 * @see org.formulacompiler.spreadsheet.EngineBuilder
 * @see #getComputationFactory()
 */
public interface Engine
{

	/**
	 * Returns the factory for computations implemented by this engine.
	 * 
	 * @return The generated factory. Besides AFC's own factory interface, the returned factory also
	 *         implements your own factory interface, if you specified one (which is recommended). So
	 *         you can simply cast the returned factory to your own interface.
	 */
	public ComputationFactory getComputationFactory();

	/**
	 * Returns a factory with explicit computation environment configuration for computations
	 * implemented by this engine.
	 * 
	 * @param _cfg is configuration for the execution environment for the computations produced by
	 *           the returned factory.
	 * @return The generated factory. Besides AFC's own factory interface, the returned factory also
	 *         implements your own factory interface, if you specified one (which is recommended). So
	 *         you can simply cast the returned factory to your own interface.
	 */
	public ComputationFactory getComputationFactory( Computation.Config _cfg );


}
