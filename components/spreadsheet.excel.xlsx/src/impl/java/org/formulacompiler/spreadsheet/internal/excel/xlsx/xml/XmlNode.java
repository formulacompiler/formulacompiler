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

package org.formulacompiler.spreadsheet.internal.excel.xlsx.xml;

import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;

import org.formulacompiler.runtime.New;

/**
 * @author Igor Didyuk
 */
public final class XmlNode
{

	public static final class Attribute
	{
		private final QName name;
		private final String value;

		private Attribute( QName _name, String _value )
		{
			this.name = _name;
			this.value = _value;
		}

		private Attribute( javax.xml.stream.events.Attribute _attribute )
		{
			this.name = _attribute.getName();
			this.value = _attribute.getValue();
		}

		public QName getName()
		{
			return this.name;
		}

		public String getValue()
		{
			return this.value;
		}
	}

	private final QName name;
	private final List<Attribute> attributes = New.list();
	private final List<XmlNode> children = New.list();

	public XmlNode( QName _name )
	{
		this.name = _name;
	}

	@SuppressWarnings( "unchecked" )
	public XmlNode( StartElement _startElement )
	{
		this.name = _startElement.getName();
		Iterator<javax.xml.stream.events.Attribute> attributes = _startElement.getAttributes();
		while (attributes.hasNext())
			this.attributes.add( new Attribute( attributes.next() ) );
	}

	public void addAttribute( QName _name, String _value )
	{
		final Attribute attribute = new Attribute( _name, _value );

		final int attributeCount = this.attributes.size();
		for (int i = 0; i != attributeCount; i++)
			if (this.attributes.get( i ).name.equals( _name )) {
				this.attributes.set( i, attribute );
				return;
			}

		this.attributes.add( attribute );
	}

	public void addChild( XmlNode _child )
	{
		this.children.add( _child );
	}

	public XmlNode clone()
	{
		XmlNode node = new XmlNode( this.name );

		final int attributeCount = this.attributes.size();
		for (int i = 0; i != attributeCount; i++)
			node.attributes.add( this.attributes.get( i ) );

		final int childCount = this.children.size();
		for (int i = 0; i != childCount; i++)
			node.children.add( this.children.get( i ).clone() );

		return node;
	}

	public QName getName()
	{
		return this.name;
	}

	public List<Attribute> getAttributes()
	{
		return this.attributes;
	}

	public List<XmlNode> getChildren()
	{
		return this.children;
	}
}
