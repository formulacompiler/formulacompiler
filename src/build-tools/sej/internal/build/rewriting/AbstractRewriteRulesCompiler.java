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
package sej.internal.build.rewriting;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import sej.compiler.Function;
import sej.describable.DescriptionBuilder;
import sej.internal.build.Util;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForAbstractFold;
import sej.internal.expressions.ExpressionNodeForConstantValue;
import sej.internal.expressions.ExpressionNodeForFold;
import sej.internal.expressions.ExpressionNodeForFoldArray;
import sej.internal.expressions.ExpressionNodeForFunction;
import sej.internal.expressions.ExpressionNodeForLet;
import sej.internal.expressions.ExpressionNodeForLetVar;
import sej.internal.expressions.ExpressionNodeForOperator;
import sej.internal.expressions.ExpressionNodeForReduce;
import sej.internal.expressions.parser.ExpressionParser;
import sej.runtime.New;

public abstract class AbstractRewriteRulesCompiler
{
	private static final String TEMPLATE_FILE = "src/build-tools/sej/internal/build/rewriting/GeneratedFunctionRewriter.template";
	private static final String TGT_FOLDER = "temp/gen-src/classes/sej/internal/model/rewriting";
	private static final String TGT_NAME = "GeneratedExpressionRewriter.java";

	private final DescriptionBuilder cases = new DescriptionBuilder();
	private final DescriptionBuilder methods = new DescriptionBuilder();
	private final List<Rule> rules = New.newList();

	protected AbstractRewriteRulesCompiler()
	{
		super();
		this.cases.indent( 3 );
		this.methods.indent( 1 );
	}


	private Rule currentRule = null;

	protected final void begin( Function _fun, String... _params )
	{
		this.currentRule = new Rule( _fun, _params );
	}

	protected final void param( String _param )
	{
		this.currentRule.param( _param );
	}

	protected final void body( String _fragment )
	{
		this.currentRule.body( _fragment );
	}

	protected final void end() throws Exception
	{
		this.rules.add( this.currentRule );
		this.currentRule = null;
	}

	protected final void def( Function _fun, String... _paramsTailedByBody ) throws Exception
	{
		begin( _fun );
		for (int i = 0; i < _paramsTailedByBody.length - 1; i++)
			param( _paramsTailedByBody[ i ] );
		body( _paramsTailedByBody[ _paramsTailedByBody.length - 1 ] );
		end();
	}


	protected final void run() throws Exception
	{
		defineFunctions();
		compileFunctions();
		writeOut();
	}


	protected abstract void defineFunctions() throws Exception;


	private void compileFunctions() throws Exception
	{
		Collections.sort( this.rules, new Comparator<Rule>()
		{
			public int compare( Rule _a, Rule _b )
			{
				int r = _a.fun.compareTo( _b.fun );
				if (r == 0) r = _a.params.size() < _b.params.size() ? -1 : +1;
				return r;
			}
		} );

		Function currCase = null;
		for (final Rule rule : this.rules) {
			if (rule.fun == currCase) {
				rule.compileSubCase();
			}
			else {
				if (null != currCase) closeSubSwitch();
				final boolean needSubSwitch = !rule.is_n_ary();
				if (needSubSwitch) {
					currCase = rule.fun;
					rule.compileCase();
					beginSubSwitch();
					rule.compileSubCase();
				}
				else {
					currCase = null;
					rule.compileCase();
				}
			}
			rule.compileCall();
			rule.compileMethod();
		}
		if (null != currCase) closeSubSwitch();

	}

	private void beginSubSwitch()
	{
		final DescriptionBuilder b = cases();
		b.indent();
		b.appendLine( "switch (_fun.cardinality()) {" );
		b.indent();
	}

	private void closeSubSwitch()
	{
		final DescriptionBuilder b = cases();
		b.outdent();
		b.appendLine( "}" );
		b.appendLine( "break;" );
		b.outdent();
	}

	private final void writeOut() throws IOException
	{
		final String template = Util.readStringFrom( new File( TEMPLATE_FILE ) );
		final String[] split1 = template.split( "__SWITCH__" );
		final String[] split2 = split1[ 1 ].split( "__METHODS__" );
		final String prefix = split1[ 0 ];
		final String infix = split2[ 0 ];
		final String suffix = split2[ 1 ];
		final String generated = prefix + this.cases.toString() + infix + this.methods.toString() + suffix;

		final File tgtFolder = new File( TGT_FOLDER );
		tgtFolder.mkdirs();
		Util.writeStringTo( generated, new File( tgtFolder, TGT_NAME ) );
	}


	final DescriptionBuilder cases()
	{
		return this.cases;
	}

	final DescriptionBuilder methods()
	{
		return this.methods;
	}


	private final class Rule
	{
		private final Function fun;
		private final List<String> params;
		private final StringBuilder body = new StringBuilder();

		public Rule(Function _fun, String... _params)
		{
			super();
			this.fun = _fun;
			this.params = New.newList( _params.length );
			for (final String p : _params)
				param( p );
		}


		void param( String _param )
		{
			this.params.add( _param );
		}

		void body( String _fragment )
		{
			this.body.append( _fragment );
		}


		final void compileCase()
		{
			final String funName = this.fun.getName();
			final DescriptionBuilder b = cases();
			b.append( "case " ).append( funName ).appendLine( ":" );
		}

		final void compileSubCase()
		{
			final int paramCount = this.params.size();
			final DescriptionBuilder b = cases();
			b.append( "case " ).append( paramCount ).appendLine( ":" );
		}

		final void compileCall()
		{
			final String mtdName = mtdName();
			final DescriptionBuilder b = cases();
			b.indent();
			{
				b.append( "return " ).append( mtdName ).appendLine( "( _fun );" );
			}
			b.outdent();
		}

		final void compileMethod() throws Exception
		{
			final String mtdName = mtdName();
			final DescriptionBuilder b = methods();
			final ExpressionNode expr = parse( this.body.toString() );
			b.append( "private final ExpressionNode " ).append( mtdName ).appendLine(
					"( ExpressionNodeForFunction _fun ) {" );
			b.indent();
			{
				b.appendLine( "final Iterator<ExpressionNode> args = _fun.arguments().iterator();" );
				final StringBuilder prefix = new StringBuilder();
				final StringBuilder suffix = new StringBuilder();
				int iParam = 0;
				for (String param : this.params) {
					if (param.endsWith( "*" )) {
						param = param.substring( 0, param.length() - 1 );
						b.append( "final ExpressionNode " ).append( param ).appendLine( " = substitution( args );" );
						this.params.set( iParam, param );
					}
					else {
						String paramExpr = "args.next()";
						if (param.endsWith( "#" )) {
							param = param.substring( 0, param.length() - 1 );
							paramExpr = "new ExpressionNodeForMakeArray( args.next() )";
							this.params.set( iParam, param );
						}
						if (occursMoreThanOnce( expr, param )) {
							b.append( "final ExpressionNode " ).append( param ).append( "_ = " ).append( paramExpr )
									.appendLine( ";" );
							b.append( "final ExpressionNode " ).append( param ).append( " = var( \"" ).append( param )
									.appendLine( "\" );" );
							prefix.append( "let( \"" ).append( param ).append( "\", " ).append( param ).append( "_, " );
							suffix.append( " )" );
						}
						else {
							b.append( "final ExpressionNode " ).append( param ).append( " = substitution( " ).append(
									paramExpr ).appendLine( " );" );
						}
					}
					iParam++;
				}

				b.append( "return " );
				b.append( prefix );
				compileExpr( expr, b );
				b.append( suffix );
				b.appendLine( ";" );
			}
			b.outdent();
			b.appendLine( "}" );
			b.newLine();
		}

		private final String mtdName()
		{
			if (is_n_ary()) {
				return "rewrite" + this.fun.getName();
			}
			return "rewrite" + this.fun.getName() + "_" + this.params.size();
		}

		private final boolean is_n_ary()
		{
			return this.params.get( this.params.size() - 1 ).endsWith( "*" );
		}

		private final boolean occursMoreThanOnce( final ExpressionNode _expr, String _param )
		{
			return countOccurrences_atLeast2( _expr, _param ) > 1;
		}

		private final int countOccurrences_atLeast2( ExpressionNode _expr, String _param )
		{
			if (_expr instanceof ExpressionNodeForLetVar) {
				final ExpressionNodeForLetVar varNode = (ExpressionNodeForLetVar) _expr;
				return varNode.varName().equals( _param ) ? 1 : 0;
			}
			else if (_expr instanceof ExpressionNodeForLet) {
				final ExpressionNodeForLet letNode = (ExpressionNodeForLet) _expr;
				int occ = countOccurrences_atLeast2( letNode.value(), _param );
				if (!letNode.varName().equals( _param )) {
					// not shadowed
					occ += countOccurrences_atLeast2( letNode.in(), _param );
				}
				return occ;
			}
			else if (_expr instanceof ExpressionNodeForFoldArray) {
				final ExpressionNodeForFoldArray foldNode = (ExpressionNodeForFoldArray) _expr;
				int occ = countOccurrences_atLeast2( foldNode.initialAccumulatorValue(), _param );
				if (!foldNode.accumulatorName().equals( _param )
						&& !foldNode.elementName().equals( _param ) && !foldNode.indexName().equals( _param )) {
					// If it occurs here, multiple since this is repeated -> always aliased.
					occ += countOccurrences_atLeast2( foldNode.accumulatingStep(), _param ) * 2;
				}
				return occ;
			}
			else if (_expr instanceof ExpressionNodeForAbstractFold) {
				final ExpressionNodeForAbstractFold foldNode = (ExpressionNodeForAbstractFold) _expr;
				int occ = countOccurrences_atLeast2( foldNode.initialAccumulatorValue(), _param );
				if (!foldNode.accumulatorName().equals( _param ) && !foldNode.elementName().equals( _param )) {
					// If it occurs here, multiple since this is repeated -> always aliased.
					occ += countOccurrences_atLeast2( foldNode.accumulatingStep(), _param ) * 2;
				}
				return occ;
			}
			else {
				int occ = 0;
				for (final ExpressionNode arg : _expr.arguments()) {
					occ += countOccurrences_atLeast2( arg, _param );
					if (occ > 1) return occ;
				}
				return occ;
			}
		}


		private final ExpressionNode parse( String _body ) throws Exception
		{
			return new ExpressionParser( _body ).parse();
		}


		private final void compileExpr( ExpressionNode _node, DescriptionBuilder _b ) throws Exception
		{
			if (_node instanceof ExpressionNodeForConstantValue) compileConst( (ExpressionNodeForConstantValue) _node, _b );
			else if (_node instanceof ExpressionNodeForOperator) compileOp( (ExpressionNodeForOperator) _node, _b );
			else if (_node instanceof ExpressionNodeForFunction) compileFun( (ExpressionNodeForFunction) _node, _b );
			else if (_node instanceof ExpressionNodeForLetVar) compileLetVar( (ExpressionNodeForLetVar) _node, _b );
			else if (_node instanceof ExpressionNodeForLet) compileLet( (ExpressionNodeForLet) _node, _b );
			else if (_node instanceof ExpressionNodeForFold) compileFold( (ExpressionNodeForFold) _node, _b );
			else if (_node instanceof ExpressionNodeForReduce) compileReduce( (ExpressionNodeForReduce) _node, _b );
			else if (_node instanceof ExpressionNodeForFoldArray) compileFoldArray( (ExpressionNodeForFoldArray) _node, _b );
			else throw new Exception( "Unsupported expression: " + _node.toString() );
		}


		private void compileArgs( Iterable<ExpressionNode> _args, DescriptionBuilder _b ) throws Exception
		{
			compileArgs( _args.iterator(), _b );
		}

		private void compileArgs( Iterator<ExpressionNode> _args, DescriptionBuilder _b ) throws Exception
		{
			boolean first = true;
			while (_args.hasNext()) {
				final ExpressionNode arg = _args.next();
				if (first) first = false;
				else _b.append( ", " );
				compileExpr( arg, _b );
			}
		}

		private void compileArgList( ExpressionNode _node, DescriptionBuilder _b ) throws Exception
		{
			_b.append( ", " );
			compileArgs( _node.arguments(), _b );
			_b.append( " )" );
		}


		private void compileConst( ExpressionNodeForConstantValue _value, DescriptionBuilder _b )
		{
			_b.append( "cst( " );

			final Object value = _value.value();
			if (value instanceof String) {
				_b.append( '"' ).append( ((String) value).replaceAll( "\"", "\\\"" ) ).append( '"' );
			}
			else {
				_b.append( value );
			}

			_b.append( " )" );
		}

		private void compileOp( ExpressionNodeForOperator _node, DescriptionBuilder _b ) throws Exception
		{
			_b.append( "op( Operator." ).append( _node.getOperator().toString() );
			compileArgList( _node, _b );
		}


		private void compileFun( ExpressionNodeForFunction _node, DescriptionBuilder _b ) throws Exception
		{
			_b.append( "fun( Function." ).append( _node.getFunction().toString() );
			compileArgList( _node, _b );
		}


		private void compileLetVar( ExpressionNodeForLetVar _node, DescriptionBuilder _b )
		{
			final String varName = _node.varName();
			if (this.params.contains( varName )) {
				_b.append( varName );
			}
			else {
				_b.append( "var( \"" ).append( varName ).append( "\" )" );
			}
		}


		private void compileLet( ExpressionNodeForLet _node, DescriptionBuilder _b ) throws Exception
		{
			_b.append( "let( \"" ).append( _node.varName() ).append( "\", " );
			compileExpr( _node.value(), _b );
			_b.append( ", " );
			compileExpr( _node.in(), _b );
			_b.append( " )" );
		}


		private void compileFold( ExpressionNodeForFold _fold, DescriptionBuilder _b ) throws Exception
		{
			_b.append( "fold( \"" ).append( _fold.accumulatorName() ).append( "\", " );
			compileExpr( _fold.initialAccumulatorValue(), _b );
			_b.append( ", \"" ).append( _fold.elementName() ).append( "\", " );
			compileExpr( _fold.accumulatingStep(), _b );
			_b.append( ", " ).append( _fold.mayReduce() ? "true" : "false" ).append( ", " );
			compileArgs( _fold.elements(), _b );
			_b.append( " )" );
		}


		private void compileReduce( ExpressionNodeForReduce _fold, DescriptionBuilder _b ) throws Exception
		{
			_b.append( "reduce( \"" ).append( _fold.accumulatorName() ).append( "\", \"" ).append( _fold.elementName() )
					.append( "\", " );
			compileExpr( _fold.accumulatingStep(), _b );
			_b.append( ", " );
			compileExpr( _fold.emptyValue(), _b );
			_b.append( ", " );
			compileArgs( _fold.elements(), _b );
			_b.append( " )" );
		}


		private void compileFoldArray( ExpressionNodeForFoldArray _fold, DescriptionBuilder _b ) throws Exception
		{
			_b.append( "folda( \"" ).append( _fold.accumulatorName() ).append( "\", " );
			compileExpr( _fold.initialAccumulatorValue(), _b );
			_b.append( ", \"" ).append( _fold.elementName() ).append( "\", \"" ).append( _fold.indexName() ).append(
					"\", " );
			compileExpr( _fold.accumulatingStep(), _b );
			_b.append( ", " );
			compileArgs( _fold.elements(), _b );
			_b.append( " )" );
		}

	}

}
