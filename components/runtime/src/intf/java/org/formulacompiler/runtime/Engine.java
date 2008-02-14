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
