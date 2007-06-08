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
package org.formulacompiler.decompiler;

import java.io.File;
import java.io.IOException;
import java.util.Map;


/**
 * Exposes the reverse-engineered source code of a compiled bytecode engine.
 * 
 *  @author peo
 */
public interface ByteCodeEngineSource
{

	/**
	 * Returns a map with class names and class sources, sorted by class name. 
	 */
	public Map<String, String> getSortedClasses();
	
	/**
	 * Saves the decompiled engine's source to a target folder into which a proper java
	 * package/source structure is written.
	 * 
	 * @param _targetFolder is the folder to save to. Created if it does not exist yet.
	 * 
	 * @throws IOException
	 */
	public void saveTo( File _targetFolder ) throws IOException;

	/**
	 * Like {@link #saveTo(File)}, but takes a string path instead of a file.
	 */
	public void saveTo( String _targetPath ) throws IOException;

}
