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


import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FlatIteratorTest
{
	@Test
	public void testNonNulls()
	{
		final Iterator<String> iterator = new FlatIterator<String, Integer>( Arrays.asList( 1, 2, 3 ).iterator() )
		{
			@Override
			protected Iterator<? extends String> getChildIterator( final Integer parent )
			{
				return Arrays.asList( "A" + parent, "B" + parent ).iterator();
			}
		};

		final List<String> expected = Arrays.asList( "A1", "B1", "A2", "B2", "A3", "B3" );
		assertIteratorEquals( expected, iterator );
	}

	@Test
	public void testSkipNulls()
	{
		final Iterator<String> iterator = new FlatIterator<String, Integer>( Arrays.asList( 1, 2, 3 ).iterator() )
		{
			@Override
			protected Iterator<? extends String> getChildIterator( final Integer parent )
			{
				return Arrays.asList( "A" + parent, null, "B" + parent, null ).iterator();
			}
		};

		final List<String> expected = Arrays.asList( "A1", "B1", "A2", "B2", "A3", "B3" );
		assertIteratorEquals( expected, iterator );
	}

	@Test
	public void testSkipLeadingNull()
	{
		final Iterator<String> iterator = new FlatIterator<String, Integer>( Arrays.asList( 1, 2, 3 ).iterator() )
		{
			@Override
			protected Iterator<? extends String> getChildIterator( final Integer parent )
			{
				return Arrays.asList( null, "A" + parent, "B" + parent ).iterator();
			}
		};

		final List<String> expected = Arrays.asList( "A1", "B1", "A2", "B2", "A3", "B3" );
		assertIteratorEquals( expected, iterator );
	}

	@Test
	public void testSkipTrailingNull()
	{
		final Iterator<String> iterator = new FlatIterator<String, Integer>( Arrays.asList( 1, 2, 3 ).iterator() )
		{
			@Override
			protected Iterator<? extends String> getChildIterator( final Integer parent )
			{
				return Arrays.asList( "A" + parent, "B" + parent, null ).iterator();
			}
		};

		final List<String> expected = Arrays.asList( "A1", "B1", "A2", "B2", "A3", "B3" );
		assertIteratorEquals( expected, iterator );
	}

	@Test
	public void testSkipMiddleNull()
	{
		final Iterator<String> iterator = new FlatIterator<String, Integer>( Arrays.asList( 1, 2, 3 ).iterator() )
		{
			@Override
			protected Iterator<? extends String> getChildIterator( final Integer parent )
			{
				return Arrays.asList( "A" + parent, null, "B" + parent ).iterator();
			}
		};

		final List<String> expected = Arrays.asList( "A1", "B1", "A2", "B2", "A3", "B3" );
		assertIteratorEquals( expected, iterator );
	}

	private static <T> void assertIteratorEquals( final List<T> _expected, final Iterator<T> _actual )
	{
		for (final T t : _expected) {
			assertTrue( _actual.hasNext() );
			assertTrue( _actual.hasNext() );

			assertEquals( t, _actual.next() );
		}
		assertFalse( _actual.hasNext() );
		assertFalse( _actual.hasNext() );
	}
}
