/*
 * Copyright (c) 2006-2009 by Abacus Research AG, Switzerland.
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

// The following comment is a source code citation marker for JCite. Please leave it as is.
// ---- resettable
/**
 * Interface that must be implemented by an output class (or extended by an output interface) of
 * computations that need to reset internal caches of values - typically for reuse on modified input
 * values.
 * 
 * @author peo
 */
public interface Resettable
{

	/**
	 * Clears all internal caches of the computation so it can be reused with changed input values.
	 * You do not need to implement this method yourself. As long as you declare it, AFC will
	 * implement it for you. If you do implement it, AFC will call it prior to resetting the
	 * computation.
	 */
	void reset();

}
// ---- resettable
// The comment above is a source code citation marker for JCite. Please leave it as is.
