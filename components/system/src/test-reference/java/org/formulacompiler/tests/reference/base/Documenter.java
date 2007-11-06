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
package org.formulacompiler.tests.reference.base;

public interface Documenter
{

	void beginFile( String _fileName ) throws Exception;
	void endFile() throws Exception;

	void beginNamedSection( Context _cx ) throws Exception;
	void endNamedSection() throws Exception;

	void newEngineRow( Context _cx ) throws Exception;
	void sameEngineRow( Context _cx ) throws Exception;


	final static class Mock implements Documenter
	{
		public static final Mock INSTANCE = new Mock();

		// DO NOT REFORMAT BELOW THIS LINE
		private Mock() { /* noop */ }
		public void beginFile( String _fileName ) { /* noop */ }
		public void endFile() { /* noop */ }
		public void beginNamedSection( Context _cx ) { /* noop */ }
		public void endNamedSection() { /* noop */ }
		public void newEngineRow( Context _cx ) { /* noop */ }
		public void sameEngineRow( Context _cx ) { /* noop */ }
		// DO NOT REFORMAT ABOVE THIS LINE

	}

}