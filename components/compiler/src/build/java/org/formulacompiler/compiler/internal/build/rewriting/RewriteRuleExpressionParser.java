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
package org.formulacompiler.compiler.internal.build.rewriting;

import java.util.Collection;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForLetVar;
import org.formulacompiler.compiler.internal.expressions.LetDictionary;
import org.formulacompiler.compiler.internal.expressions.parser.ExpressionParser;
import org.formulacompiler.compiler.internal.expressions.parser.ParseException;
import org.formulacompiler.compiler.internal.expressions.parser.Token;

final class RewriteRuleExpressionParser extends ExpressionParser
{
	private final Collection<RewriteRule> rules;

	public RewriteRuleExpressionParser( String _exprText, Collection<RewriteRule> _rules )
	{
		super( _exprText );
		this.rules = _rules;
	}

	
	public void parseFile() throws CompilerException
	{
		try {
			parseRules();
		}
		catch (InnerParserException e) {
			throw adorn( e.getCause() );
		}
		catch (ParseException e) {
			throw adorn( e );
		}
	}


	private RewriteRule currentRule = null;

	@Override
	protected void makeNewRewriteRule( Token _name )
	{
		final String name = _name.image.toUpperCase();
		this.currentRule = new RewriteRule( Function.valueOf( name ) );
	}

	@Override
	protected void checkInRewrite()
	{
		if (null == this.currentRule) super.checkInRewrite();
	}

	@Override
	protected void makeNewParam( Token _name, char _suffix )
	{
		checkInRewrite();
		final RewriteRule.Param.Type type;
		switch (_suffix) {
			case 0:
				type = RewriteRule.Param.Type.VALUE;
				break;
			case '#':
				type = RewriteRule.Param.Type.ARRAY;
				break;
			case '*':
				type = RewriteRule.Param.Type.LIST;
				break;
			case '+':
				type = RewriteRule.Param.Type.SYMBOLIC;
				break;
			default:
				throw new IllegalArgumentException( "Unexpected param suffix " + _suffix );
		}
		this.currentRule.addParam( _name.image, type );
	}

	@Override
	protected void makeBody()
	{
		checkInRewrite();
		this.currentRule.setBody( popNode() );
	}

	@Override
	protected void finalizeLastRewriteRule()
	{
		checkInRewrite();
		this.rules.add( this.currentRule );
		this.currentRule = null;
	}


	private final LetDictionary letDict = new LetDictionary();

	@Override
	protected void let( Token... _names )
	{
		checkInRewrite();
		for (int i = 0; i < _names.length; i++)
			this.letDict.let( _names[ i ].image, null, null );
	}

	@Override
	protected void unlet( Token... _names )
	{
		checkInRewrite();
		for (int i = _names.length - 1; i >= 0; i--)
			this.letDict.unlet( _names[ i ].image );
	}

	@Override
	protected void letParams()
	{
		for (RewriteRule.Param p : this.currentRule.params)
			this.letDict.let( p.name, null, null );
	}

	@Override
	protected void unletParams()
	{
		this.letDict.unlet( this.currentRule.params.size() );
	}

	@Override
	protected ExpressionNode makeLetVar( Token _name )
	{
		checkInRewrite();
		final String name = _name.image;
		if (null == this.letDict.find( name )) {
			throw new IllegalArgumentException( "Let var " + name + " is not defined here." );
		}
		return new ExpressionNodeForLetVar( name );
	}

	@Override
	protected ExpressionNode makeNamedCellRef( Token _name )
	{
		return makeLetVar( _name );
	}

	@Override
	protected ExpressionNode makeShapedRange( ExpressionNode _range )
	{
		if (_range instanceof ExpressionNodeForLetVar) {
			final ExpressionNodeForLetVar var = (ExpressionNodeForLetVar) _range;
			return var;
		}
		return super.makeShapedRange( _range );
	}
	
	
}
