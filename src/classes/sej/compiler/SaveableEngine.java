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
package sej.compiler;

import java.io.IOException;
import java.io.OutputStream;

import sej.runtime.Engine;


/**
 * Defines the {@link #saveTo(OutputStream)} method for engines returned by an engine compiler. This
 * allows you to save a compiled engine to persistent storage and then later re-instatiate it using
 * {@link sej.runtime.SEJRuntime#loadEngine(java.io.InputStream)}.
 * 
 * The {@link sej.runtime.Engine} interface does not have save support so as not to burden the
 * run-time-only support with it.
 * 
 * @author peo
 */
public interface SaveableEngine extends Engine
{

	/**
	 * Saves a compiled engine to a stream. You can later re-instantiate the engine from the stream
	 * using {@link sej.runtime.SEJRuntime#loadEngine(java.io.InputStream)}. The engine is saved in
	 * the format of a compressed .jar file containing .class entries for all the classes generated
	 * by SEJ.
	 * 
	 * @param _stream to save the engine to.
	 * @throws IOException
	 */
	public void saveTo( OutputStream _stream ) throws IOException;

}
