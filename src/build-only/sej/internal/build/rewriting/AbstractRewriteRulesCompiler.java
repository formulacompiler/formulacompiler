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
package sej.internal.build.rewriting;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sej.Function;
import sej.describable.DescriptionBuilder;
import sej.internal.build.Util;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForConstantValue;
import sej.internal.expressions.ExpressionNodeForFold;
import sej.internal.expressions.ExpressionNodeForFunction;
import sej.internal.expressions.ExpressionNodeForLet;
import sej.internal.expressions.ExpressionNodeForLetVar;
import sej.internal.expressions.ExpressionNodeForOperator;
import sej.internal.spreadsheet.loader.excel.RewriteLanguageParser;

public abstract class AbstractRewriteRulesCompiler
{
	private static final String TEMPLATE_FILE = "src/build-only/sej/internal/build/rewriting/GeneratedFunctionRewriter.template";
	private static final String TGT_FOLDER = "src/classes-gen/sej/internal/model/rewriting";
	private static final String TGT_NAME = "GeneratedExpressionRewriter.java";

	private final DescriptionBuilder cases = new DescriptionBuilder();
	private final DescriptionBuilder methods = new DescriptionBuilder();

	protected AbstractRewriteRulesCompiler()
	{
		super();
		this.cases.indent( 3 );
		this.methods.indent( 1 );
	}


	private Function fun;
	private List<String> params;
	private StringBuilder body;

	protected final void begin( Function _fun, String... _params )
	{
		this.fun = _fun;
		this.params = new ArrayList<String>();
		for (String p : _params)
			param( p );
		this.body = new StringBuilder();
	}

	protected final void param( String _param )
	{
		this.params.add( _param );
	}

	protected final void body( String _fragment )
	{
		this.body.append( _fragment );
		// this.body.append( "\n" );
	}

	protected final void end() throws Exception
	{
		compile();
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
		writeOut();
	}


	protected abstract void defineFunctions() throws Exception;


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


	private final void compile() throws Exception
	{
		compileCase();
		compileMethod();
	}

	private final void compileCase()
	{
		final String funName = this.fun.getName();
		final DescriptionBuilder b = this.cases;
		b.append( "case " ).append( funName ).appendLine( ":" );
		b.indent();
		{
			b.append( "return rewrite" ).append( funName ).appendLine( "( _fun );" );
		}
		b.outdent();
	}

	private final void compileMethod() throws Exception
	{
		final DescriptionBuilder b = this.methods;
		b.append( "private final ExpressionNode rewrite" ).append( this.fun.getName() ).appendLine(
				"( ExpressionNodeForFunction _fun ) {" );
		b.indent();
		{
			b.appendLine( "final Iterator<ExpressionNode> args = _fun.arguments().iterator();" );
			int iParam = 0;
			for (String param : this.params) {
				if (param.endsWith( "*" )) {
					param = param.substring( 0, param.length() - 1 );
					b.append( "final ExpressionNode " ).append( param ).appendLine( " = substitution( args );" );
					this.params.set( iParam, param );
				}
				else {
					b.append( "final ExpressionNode " ).append( param ).appendLine( " = substitution( args.next() );" );
				}
				iParam++;
			}

			b.append( "return " );
			compileExpr( parse( this.body.toString() ), b );
			b.appendLine( ";" );
		}
		b.outdent();
		b.appendLine( "}" );
		b.newLine();
	}


	private final ExpressionNode parse( String _body ) throws Exception
	{
		return RewriteLanguageParser.parse( _body );
	}


	private final void compileExpr( ExpressionNode _node, DescriptionBuilder _b ) throws Exception
	{
		if (_node instanceof ExpressionNodeForConstantValue) compileConst( (ExpressionNodeForConstantValue) _node, _b );
		else if (_node instanceof ExpressionNodeForOperator) compileOp( (ExpressionNodeForOperator) _node, _b );
		else if (_node instanceof ExpressionNodeForFunction) compileFun( (ExpressionNodeForFunction) _node, _b );
		else if (_node instanceof ExpressionNodeForLetVar) compileLetVar( (ExpressionNodeForLetVar) _node, _b );
		else if (_node instanceof ExpressionNodeForLet) compileLet( (ExpressionNodeForLet) _node, _b );
		else if (_node instanceof ExpressionNodeForFold) compileFold( (ExpressionNodeForFold) _node, _b );
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

		final Object value = _value.getValue();
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
		_b.append( "op( Operator." ).append( _node.getOperator().getName() );
		compileArgList( _node, _b );
	}


	private void compileFun( ExpressionNodeForFunction _node, DescriptionBuilder _b ) throws Exception
	{
		_b.append( "fun( Function." ).append( _node.getFunction().getName() );
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
		_b.append( "foldl( \"" ).append( _fold.accumulatorName() ).append( "\", " );
		compileExpr( _fold.initialAccumulatorValue(), _b );
		_b.append( ", \"" ).append( _fold.elementName() ).append( "\", " );
		compileExpr( _fold.accumulatingStep(), _b );
		_b.append( ", " );
		compileArgs( _fold.elements(), _b );
		_b.append( " )" );
	}


}
