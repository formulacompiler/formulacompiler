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
package sej.expressions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import sej.describable.AbstractDescribable;
import sej.describable.DescriptionBuilder;


public abstract class ExpressionNode extends AbstractDescribable
{
	private List<ExpressionNode> arguments = new ArrayList<ExpressionNode>();


	protected ExpressionNode()
	{
		super();
	}


	protected ExpressionNode(ExpressionNode... _args)
	{
		for (ExpressionNode arg : _args) {
			getArguments().add( arg );
		}
	}


	@SuppressWarnings("unchecked")
	public ExpressionNode(Collection _args)
	{
		for (ExpressionNode arg : (Collection<ExpressionNode>) _args) {
			getArguments().add( arg );
		}
	}


	public List<ExpressionNode> getArguments()
	{
		return this.arguments;
	}


	public void addArgument( ExpressionNode _arg )
	{
		this.arguments.add( _arg );
	}


	public abstract ExpressionNode cloneWithoutArguments();


	@Override
	public Object clone()
	{
		ExpressionNode result = cloneWithoutArguments();
		for (ExpressionNode arg : getArguments()) {
			ExpressionNode newArg = (ExpressionNode) arg.clone();
			result.getArguments().add( newArg );
		}
		return result;
	}


	protected void describeArgumentTo( DescriptionBuilder _d, int _iArgument ) throws IOException
	{
		ExpressionNode arg = this.getArguments().get( _iArgument );
		if (null != arg) {
			arg.describeTo( _d );
		}
	}


	protected void describeArgumentListTo( DescriptionBuilder _d ) throws IOException
	{
		if (0 == getArguments().size()) {
			_d.append( "()" );
		}
		else {
			_d.append( "( " );
			describeArgumentTo( _d, 0 );
			for (int iArg = 1; iArg < getArguments().size(); iArg++) {
				_d.append( ", " );
				describeArgumentTo( _d, iArg );
			}
			_d.append( " )" );
		}
	}


	protected void describeArgumentOrArgumentListTo( DescriptionBuilder _d ) throws IOException
	{
		if (1 == getArguments().size()) {
			describeArgumentTo( _d, 0 );
		}
		else {
			describeArgumentListTo( _d );
		}
	}

}
