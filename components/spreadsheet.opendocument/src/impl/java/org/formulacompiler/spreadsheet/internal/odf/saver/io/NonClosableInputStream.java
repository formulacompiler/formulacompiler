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

package org.formulacompiler.spreadsheet.internal.odf.saver.io;

import java.io.IOException;
import java.io.InputStream;

public class NonClosableInputStream extends InputStream
{
	private final InputStream inputStream;

	public NonClosableInputStream( final InputStream _inputStream )
	{
		this.inputStream = _inputStream;
	}

	@Override
	public int read() throws IOException
	{
		return this.inputStream.read();
	}

	@Override
	public int read( final byte b[] ) throws IOException
	{
		return this.inputStream.read( b );
	}

	@Override
	public int read( final byte b[], final int off, final int len ) throws IOException
	{
		return this.inputStream.read( b, off, len );
	}

	@Override
	public long skip( final long n ) throws IOException
	{
		return this.inputStream.skip( n );
	}

	@Override
	public int available() throws IOException
	{
		return this.inputStream.available();
	}

	@Override
	public void close() throws IOException
	{
		// Do not close.
	}

	@Override
	public synchronized void mark( final int readlimit )
	{
		this.inputStream.mark( readlimit );
	}

	@Override
	public synchronized void reset() throws IOException
	{
		this.inputStream.reset();
	}

	@Override
	public boolean markSupported()
	{
		return this.inputStream.markSupported();
	}
}
