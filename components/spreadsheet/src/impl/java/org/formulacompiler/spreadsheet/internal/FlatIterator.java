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

package org.formulacompiler.spreadsheet.internal;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

abstract class FlatIterator<T, P> implements Iterator<T>
{
	private final Iterator<? extends P> parentIterator;

	private Iterator<? extends T> childIterator = Collections.<T>emptySet().iterator();
	private T currentNext;

	protected FlatIterator( final Iterator<? extends P> _parentIterator )
	{
		this.parentIterator = _parentIterator;
	}

	public boolean hasNext()
	{
		if (this.currentNext != null) return true;
		else {
			while (this.currentNext == null) {
				if (this.childIterator.hasNext()) {
					this.currentNext = this.childIterator.next();
				}
				else if (this.parentIterator.hasNext()) {
					final P parent = this.parentIterator.next();
					this.childIterator = getChildIterator( parent );
				}
				else {
					return false;
				}
			}
			return true;
		}
	}

	public T next()
	{
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		try {
			return this.currentNext;
		}
		finally {
			this.currentNext = null;
		}
	}

	public void remove()
	{
		throw new UnsupportedOperationException();
	}

	protected abstract Iterator<? extends T> getChildIterator( P _parent );
}
