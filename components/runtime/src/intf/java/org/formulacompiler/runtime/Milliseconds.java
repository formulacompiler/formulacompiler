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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates to AFC that a {@code long} value is actually a number of milliseconds for an Excel
 * time-of-day or time duration cell. You should never use {@link java.util.Date} for pure time
 * cells.
 * 
 * <p>
 * See the <a target="_top" href="{@docRoot}/../tutorial/numeric_type.htm#long">tutorial</a> for
 * details.
 * 
 * @author peo
 */
@Retention( RetentionPolicy.RUNTIME )
public @interface Milliseconds
{
	// Indicator only.
}
