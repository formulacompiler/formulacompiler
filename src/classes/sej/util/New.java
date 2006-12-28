package sej.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public final class New
{

	public static <T> List<T> newArrayList()
	{
		return new ArrayList<T>();
	}

	public static <T> List<T> newArrayList( int _initialSize )
	{
		return new ArrayList<T>( _initialSize );
	}

	public static <T> List<T> newList()
	{
		return newArrayList();
	}

	public static <T> List<T> newList( int _initialSize )
	{
		return newArrayList( _initialSize );
	}

	public static <T> Collection<T> newCollection()
	{
		return newList();
	}

	public static <T> Collection<T> newCollection( int _initialSize )
	{
		return newList( _initialSize );
	}

	public static <K, V> Map<K, V> newHashMap()
	{
		return new HashMap<K, V>();
	}

	public static <K, V> Map<K, V> newMap()
	{
		return newHashMap();
	}

	public static <T> SortedSet<T> newSortedSet()
	{
		return new TreeSet<T>();
	}

}
