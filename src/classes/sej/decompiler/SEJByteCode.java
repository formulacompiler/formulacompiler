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
package sej.decompiler;

import java.io.IOException;

import sej.runtime.Engine;
import sej.runtime.ImplementationLocator;
import sej.runtime.SEJRuntime;

/**
 * Provides methods specific to the JVM bytecode generating backend of SEJ.
 * 
 * @author peo
 */
public final class SEJByteCode extends SEJRuntime
{

	/**
	 * Returns an object describing a compiled engine as decompiled Java source code.
	 */
	public static final ByteCodeEngineSource decompile( Engine _engine ) throws IOException
	{
		final ByteCodeEngineDecompiler.Config cfg = new ByteCodeEngineDecompiler.Config();
		cfg.engine = _engine;
		return DECOMPILER_FACTORY.newInstance( cfg ).decompile();
	}

	private static final ByteCodeEngineDecompiler.Factory DECOMPILER_FACTORY = ImplementationLocator
			.getInstance( ByteCodeEngineDecompiler.Factory.class );

}
