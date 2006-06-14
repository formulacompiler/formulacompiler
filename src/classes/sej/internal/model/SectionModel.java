/*
 * Copyright © 2006 by Abacus Research AG, Switzerland.
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
package sej.internal.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sej.api.CompilerError;
import sej.api.DescriptionBuilder;

public class SectionModel extends ElementModel
{
	private final String name;
	private final List<SectionModel> sections = new ArrayList<SectionModel>();
	private final List<CellModel> cells = new ArrayList<CellModel>();
	private final Class inputClass;
	private final Class outputClass;


	public SectionModel(SectionModel _section, String _name, Class _inputClass, Class _outputClass)
	{
		super( _section );
		this.name = _name;
		this.inputClass = _inputClass;
		this.outputClass = _outputClass;
		_section.getSections().add( this );
	}


	SectionModel(ComputationModel _engine, String _name, Class _inputClass, Class _outputClass)
	{
		super( _engine );
		this.name = _name;
		this.inputClass = _inputClass;
		this.outputClass = _outputClass;
	}


	public List<SectionModel> getSections()
	{
		return this.sections;
	}


	public List<CellModel> getCells()
	{
		return this.cells;
	}


	public Class getInputClass()
	{
		return this.inputClass;
	}

	public Class getOutputClass()
	{
		return this.outputClass;
	}


	public boolean traverse( ComputationModelVisitor _visitor ) throws CompilerError
	{
		if (!_visitor.visit( this )) return false;
		for (CellModel cell : getCells()) {
			if (!_visitor.visit( cell )) return false;
		}
		for (SectionModel section : getSections()) {
			if (!section.traverse( _visitor )) return false;
		}
		if (!_visitor.visited( this )) return false;
		return true;
	}


	@Override
	public void describeTo( DescriptionBuilder _to ) throws IOException
	{
		_to.append( "<section id=\"" );
		_to.append( toString() );
		_to.append( "\">" );
		_to.newLine();
		_to.indent();

		for (CellModel cell : getCells()) {
			cell.describeTo( _to );
		}

		for (SectionModel ns : getSections()) {
			ns.describeTo( _to );
		}

		_to.outdent();
		_to.appendLine( "</section>" );
	}
	
	
	@Override
	public String getName()
	{
		return this.name;
	}


}
