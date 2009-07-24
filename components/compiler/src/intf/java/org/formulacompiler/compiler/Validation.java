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

package org.formulacompiler.compiler;

import java.lang.reflect.Method;

import org.formulacompiler.runtime.ImplementationLocator;


/**
 * PRIVATE INTERFACE - DO NOT USE.
 * 
 * @author peo
 */
public interface Validation
{
	static final Validation SINGLETON = ImplementationLocator.getInstance( Factory.class ).getSingleton();

	void validateIsAccessible( Class _class, String _role );
	void validateIsAccessible( Method _method, String _role );
	void validateIsImplementable( Class _class, String _role );
	void validateIsImplementable( Method _method, String _role );
	void validateCallable( Class _class, Method _method );
	void validateFactory( Class _factoryClass, Method _factoryMethod, Class _inputClass, Class _outputClass );

	/**
	 * PRIVATE INTERFACE - DO NOT USE.
	 * 
	 * @author peo
	 */
	interface Factory
	{
		Validation getSingleton();
	}
}
