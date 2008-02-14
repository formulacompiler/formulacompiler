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


/**
 * Interface implemented by all self-describing classes in AFC. These descriptions are meant for
 * human consumption and should not be stored or reprocessed. They may change without announcement.
 * 
 * @author peo
 */
public interface Describable
{


	/**
	 * Describes this object in a string meant for human consumption.
	 * <p>
	 * This method is separate from {@link #toString()} so implementing classes can choose to have
	 * more defined (and possibly concise) versions of {@link #toString()} while still returning more
	 * complete information for this method.
	 * 
	 * @return The description, possibly with multiple lines of text. Meant for human consumption.
	 *         Should not be stored or reprocessed. May change without announcement.
	 * 
	 * @see #toString()
	 */
	public String describe();


	/**
	 * Describes this object in a string meant for human consumption.
	 * 
	 * @return The description, possibly with multiple lines of text. Meant for human consumption.
	 *         Should not be stored or reprocessed. May change without announcement.
	 * 
	 * @see #describe()
	 */
	public String toString();


}