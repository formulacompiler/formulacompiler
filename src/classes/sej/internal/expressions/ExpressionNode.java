/*
 * Copyright � 2006 by Abacus Research AG, Switzerland.
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
package sej.internal.expressions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import sej.describable.AbstractDescribable;
import sej.describable.DescriptionBuilder;


public abstract class ExpressionNode extends AbstractDescribable
{
	private List<ExpressionNode> arguments = new ArrayList<ExpressionNode>();
	private DataType dataType;
	private ExpressionNode derivedFrom;
	private ExpressionContextProvider contextProvider;


	protected ExpressionNode()
	{
		super();
	}


	protected ExpressionNode(ExpressionNode... _args)
	{
		for (ExpressionNode arg : _args) {
			arguments().add( arg );
		}
	}


	@SuppressWarnings("unchecked")
	public ExpressionNode(Collection _args)
	{
		for (ExpressionNode arg : (Collection<ExpressionNode>) _args) {
			arguments().add( arg );
		}
	}


	public List<ExpressionNode> arguments()
	{
		return this.arguments;
	}


	public void addArgument( ExpressionNode _arg )
	{
		this.arguments.add( _arg );
	}

	public int cardinality()
	{
		int result = arguments().size();
		while ((result > 0) && (arguments().get( result - 1 ) == null)) {
			result--;
		}
		return result;
	}


	public DataType getDataType()
	{
		return this.dataType;
	}
	
	public void setDataType( DataType _dataType )
	{
		this.dataType = _dataType;
	}
	

	public ExpressionNode getDerivedFrom()
	{
		return this.derivedFrom;
	}

	public void setDerivedFrom( ExpressionNode _node )
	{
		this.derivedFrom = _node;
	}

	public ExpressionNode getOrigin()
	{
		return (this.derivedFrom == null)? this : this.derivedFrom.getOrigin();
	}


	public abstract ExpressionNode cloneWithoutArguments();


	@Override
	public Object clone()
	{
		final ExpressionNode result = cloneWithoutArguments();
		for (ExpressionNode arg : arguments()) {
			final ExpressionNode newArg = (ExpressionNode) arg.clone();
			result.arguments().add( newArg );
		}
		return result;
	}
	
	
	@Override
	public final void describeTo( DescriptionBuilder _to ) throws IOException
	{
		describeToWithConfig( _to, null );
	}


	public final void describeTo( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg ) throws IOException
	{
		if (null != _cfg && _cfg.isFocused( this )) {
			_to.append( _cfg.focusStartMarker() );
			describeToWithConfig( _to, _cfg );
			_to.append( _cfg.focusEndMarker() );
		}
		else {
			describeToWithConfig( _to, _cfg );
		}
	}


	protected abstract void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg ) throws IOException;


	protected void describeArgumentTo( DescriptionBuilder _d, ExpressionDescriptionConfig _cfg, int _iArgument ) throws IOException
	{
		final ExpressionNode arg = this.arguments().get( _iArgument );
		if (null != arg) {
			arg.describeTo( _d, _cfg );
		}
	}


	protected void describeArgumentListTo( DescriptionBuilder _d, ExpressionDescriptionConfig _cfg ) throws IOException
	{
		if (0 == arguments().size()) {
			_d.append( "()" );
		}
		else {
			_d.append( "( " );
			describeArgumentTo( _d, _cfg, 0 );
			for (int iArg = 1; iArg < arguments().size(); iArg++) {
				_d.append( ", " );
				describeArgumentTo( _d, _cfg, iArg );
			}
			_d.append( " )" );
		}
	}


	protected void describeArgumentOrArgumentListTo( DescriptionBuilder _d, ExpressionDescriptionConfig _cfg ) throws IOException
	{
		if (1 == arguments().size()) {
			describeArgumentTo( _d, _cfg, 0 );
		}
		else {
			describeArgumentListTo( _d, _cfg );
		}
	}


	public ExpressionContextProvider getContextProvider()
	{
		return this.contextProvider;
	}

	public void setContextProvider( ExpressionContextProvider _provider )
	{
		this.contextProvider = _provider;
	}

	public String getContext( ExpressionNode _focusedNode )
	{
		final DescriptionBuilder builder = new DescriptionBuilder();
		try {
			buildContext( builder, _focusedNode );
		}
		catch (IOException e) {
			builder.append( e.getMessage() );
		}
		return builder.toString();
	}

	public void buildContext( DescriptionBuilder _builder, ExpressionNode _focusedNode ) throws IOException
	{
		ExpressionContextProvider prov = getNearestContextProvider();
		if (null != prov) {
			prov.buildContext( _builder, _focusedNode );
		}
		else {
			describeTo( _builder );
		}
	}


	private ExpressionContextProvider getNearestContextProvider()
	{
		final ExpressionContextProvider prov = getContextProvider();
		return (null == prov)? getOrigin().getContextProvider() : prov;
	}


}
