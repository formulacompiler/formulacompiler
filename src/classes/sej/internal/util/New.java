package sej.internal.util;

import java.util.ArrayList;
import java.util.List;

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

}
