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
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Provides static generic collection contructors which make use of Java 5 type inference as
 * suggested by Josh Bloch in his Google slides on "Effective Java Reloaded".
 * 
 * @author peo
 */
public final class New
{

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

}
