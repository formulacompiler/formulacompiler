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

package org.formulacompiler.decompiler;

import java.io.File;
import java.io.IOException;
import java.util.Map;


/**
 * Exposes the reverse-engineered source code of a compiled bytecode engine.
 * 
 * @author peo
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
