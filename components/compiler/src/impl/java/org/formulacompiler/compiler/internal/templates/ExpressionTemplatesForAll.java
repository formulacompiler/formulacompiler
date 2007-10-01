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
