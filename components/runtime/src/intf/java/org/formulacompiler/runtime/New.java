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

package org.formulacompiler.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Provides static generic collection constructors which make use of Java 5 type inference as
 * suggested by Josh Bloch in his Google slides on "Effective Java Reloaded".
 * 
 * @author peo
 */
public final class New
{

	public static <T> T[] array( T... e )
	{
		return e;
	}

	public static <T> List<T> arrayList()
	{
		return new ArrayList<T>();
	}

	public static <T> List<T> arrayList( int _initialSize )
	{
		return new ArrayList<T>( _initialSize );
	}

	public static <T> List<T> list()
	{
		return arrayList();
	}

	public static <T> List<T> list( int _initialSize )
	{
		return arrayList( _initialSize );
	}

	public static <T> Collection<T> collection()
	{
		return list();
	}

	public static <T> Collection<T> collection( int _initialSize )
	{
		return list( _initialSize );
	}

	public static <K, V> Map<K, V> hashMap()
	{
		return new HashMap<K, V>();
	}

	public static <K, V> Map<K, V> map()
	{
		return hashMap();
	}

	public static <V> Map<String, V> caseInsensitiveMap()
	{
		return new TreeMap<String, V>( String.CASE_INSENSITIVE_ORDER );
	}

	public static <T> SortedSet<T> sortedSet()
	{
		return new TreeSet<T>();
	}

	public static <K, V> SortedMap<K, V> sortedMap()
	{
		return new TreeMap<K, V>();
	}

	public static <T> Set<T> set()
	{
		return new HashSet<T>();
	}

	public static <T> Stack<T> stack()
	{
		return new Stack<T>();
	}

}
