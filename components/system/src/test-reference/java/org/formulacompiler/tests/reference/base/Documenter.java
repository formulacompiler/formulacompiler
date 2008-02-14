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