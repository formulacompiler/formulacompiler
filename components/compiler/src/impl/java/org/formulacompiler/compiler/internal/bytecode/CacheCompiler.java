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

package org.formulacompiler.compiler.internal.bytecode;


import org.formulacompiler.compiler.CompilerException;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

abstract class CacheCompiler
{
	private final SectionCompiler section;
	private final GeneratorAdapter mv;
	private final Type type;
	private final String cachedIndicatorName;
	private final String cacheName;

	CacheCompiler( SectionCompiler _section, GeneratorAdapter _mv, String _baseName, Type _type )
	{
		this.section = _section;
		this.mv = _mv;
		this.cachedIndicatorName = "h$" + _baseName;
		this.cacheName = "c$" + _baseName;
		this.type = _type;
	}

	abstract void compileValue() throws CompilerException;

	void compile() throws CompilerException
	{
		// private boolean h$<x>
		cw().visitField( Opcodes.ACC_PRIVATE, this.cachedIndicatorName, Type.BOOLEAN_TYPE.getDescriptor(), null, null )
				.visitEnd();

		// private <type> c$<x>
		cw().visitField( Opcodes.ACC_PRIVATE, this.cacheName, this.type.getDescriptor(), null, null ).visitEnd();

		// if (!h$<x>) {
		final Label skipCachedComputation = mv().newLabel();
		mv().loadThis();
		mv().getField( classType(), this.cachedIndicatorName, Type.BOOLEAN_TYPE );
		mv().visitJumpInsn( Opcodes.IFNE, skipCachedComputation );

		// c$<x> = <value>;
		mv().loadThis();
		compileValue();
		mv().putField( classType(), this.cacheName, this.type );

		// h$<x> = true;
		mv().loadThis();
		mv().push( true );
		mv().putField( classType(), this.cachedIndicatorName, Type.BOOLEAN_TYPE );

		// }
		// return c$<x>;
		mv().mark( skipCachedComputation );
		mv().loadThis();
		mv().getField( classType(), this.cacheName, this.type );

		// In reset(), do:
		if (this.section.hasReset()) {
			// h$<x> = false;
			final GeneratorAdapter r = this.section.resetter();
			r.loadThis();
			r.push( false );
			r.putField( classType(), this.cachedIndicatorName, Type.BOOLEAN_TYPE );
		}
	}

	private Type classType()
	{
		return this.section.classType();
	}

	private ClassWriter cw()
	{
		return this.section.cw();
	}

	private GeneratorAdapter mv()
	{
		return this.mv;
	}
}
