package sej.internal.expressions;

import java.util.HashMap;
import java.util.Map;

public final class LetDictionary<V>
{
	private final Map<String, V> map = new HashMap<String, V>();
	
	public LetDictionary()
	{
		super();
	}

	
	public final void clear()
	{
		this.map.clear();
	}

	
	public final V let( String _name, V _value )
	{
		final V old = this.map.get( _name );
		this.map.put( _name, _value );
		return old;
	}

	public final void set( String _name, V _value )
	{
		this.map.put( _name, _value );
	}

	public final void unlet( String _name, V _value )
	{
		this.map.put( _name, _value );
	}

	
	public final V lookup( String _name )
	{
		return this.map.get( _name );
	}


}
