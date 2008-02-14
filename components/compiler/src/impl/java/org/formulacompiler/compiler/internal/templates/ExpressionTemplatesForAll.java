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

package org.formulacompiler.compiler.internal.templates;

import org.formulacompiler.runtime.internal.Runtime_v2;


public abstract class ExpressionTemplatesForAll
{


	// ------------------------------------------------ Utils


	byte util_unboxByte( Byte a )
	{
		return Runtime_v2.unboxByte( a );
	}

	short util_unboxShort( Short a )
	{
		return Runtime_v2.unboxShort( a );
	}

	int util_unboxInteger( Integer a )
	{
		return Runtime_v2.unboxInteger( a );
	}

	long util_unboxLong( Long a )
	{
		return Runtime_v2.unboxLong( a );
	}

	float util_unboxFloat( Float a )
	{
		return Runtime_v2.unboxFloat( a );
	}

	double util_unboxDouble( Double a )
	{
		return Runtime_v2.unboxDouble( a );
	}

	char util_unboxCharacter( Character a )
	{
		return Runtime_v2.unboxCharacter( a );
	}

	boolean util_unboxBoolean( Boolean a )
	{
		return Runtime_v2.unboxBoolean( a );
	}


	Byte util_boxByte( byte a )
	{
		return Byte.valueOf( a );
	}

	Short util_boxShort( short a )
	{
		return Short.valueOf( a );
	}

	Integer util_boxInteger( int a )
	{
		return Integer.valueOf( a );
	}

	Long util_boxLong( long a )
	{
		return Long.valueOf( a );
	}

	Float util_boxFloat( float a )
	{
		return Float.valueOf( a );
	}

	Double util_boxDouble( double a )
	{
		return Double.valueOf( a );
	}

	Character util_boxCharacter( char a )
	{
		return Character.valueOf( a );
	}

	Boolean util_boxBoolean( boolean a )
	{
		return Boolean.valueOf( a );
	}


	// ------------------------------------------------ Array Access


	/**
	 * Used for _FOLDL. Scans the internal array of section objects for a section, returning each in
	 * turn. {@code scanElement()} marks the position where we compile in the actual folding step.
	 */
	void scanArray( Object[] xs )
	{
		for (Object x : xs) {
			scanElement( x );
		}
	}

	abstract void scanElement( Object x );


}
