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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.internal.build.Util;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForAbstractFold;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFold;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldArray;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFunction;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForLet;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForLetVar;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForOperator;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForReduce;
import org.formulacompiler.compiler.internal.expressions.LetDictionary;
import org.formulacompiler.describable.DescriptionBuilder;
import org.formulacompiler.runtime.New;


public final class RewriteRulesCompiler
{
	private static final String FILES_PATH = "org/formulacompiler/compiler/internal/build/rewriting/";
	private static final String TEMPLATE_FILE = FILES_PATH + "GeneratedFunctionRewriter.template";
	private static final String TGT_FOLDER = "temp/impl/java/org/formulacompiler/compiler/internal/model/rewriting";
	private static final String TGT_NAME = "GeneratedFunctionRewriter.java";

	private final DescriptionBuilder cases = new DescriptionBuilder();
	private final DescriptionBuilder methods = new DescriptionBuilder();
	private final List<RewriteRule> rules = New.list();


	public static void main( String[] args ) throws Exception
	{
		new RewriteRulesCompiler().run();
	}


	private RewriteRulesCompiler()
	{
		super();
		this.cases.indent( 3 );
		this.methods.indent( 1 );
	}


	private void run() throws Exception
	{
		defineFunctions();
		compileFunctions();
		writeOut();
	}


	private void defineFunctions() throws Exception
	{
		parseRulesIn( "rewrite.rules" );
	}

	private void parseRulesIn( String _fileName ) throws Exception
	{

		final String src = Util.readStringFrom( ClassLoader.getSystemResourceAsStream( FILES_PATH + _fileName ) );
		new RewriteRuleExpressionParser( src, this.rules ).parseFile();
	}


	private void compileFunctions() throws Exception
	{
		Collections.sort( this.rules );

		Function currCase = null;
		for (final RewriteRule rule : this.rules) {
			final RuleCompiler rulec = new RuleCompiler( rule );
			if (rule.fun == currCase) {
				rulec.compileSubCase();
			}
			else {
				if (null != currCase) closeSubSwitch();
				final boolean needSubSwitch = !rulec.is_n_ary();
				if (needSubSwitch) {
					currCase = rule.fun;
					rulec.compileCase();
					beginSubSwitch();
					rulec.compileSubCase();
				}
				else {
					currCase = null;
					rulec.compileCase();
				}
			}
			rulec.compileCall();
			rulec.compileMethod();
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
		final String template = Util.readStringFrom( ClassLoader.getSystemResourceAsStream( TEMPLATE_FILE ) );
		final String[] split1 = template.split( "__SWITCH__" );
		final String[] split2 = split1[ 1 ].split( "__METHODS__" );
		final String prefix = split1[ 0 ];
		final String infix = split2[ 0 ];
		final String suffix = split2[ 1 ];
		final String generated = prefix + this.cases.toString() + infix + this.methods.toString() + suffix;

		final File tgtFolder = new File( TGT_FOLDER );
		final File tgtFile = new File( tgtFolder, TGT_NAME );
		tgtFolder.mkdirs();
		Util.writeStringToIfNotUpToDateWithMessage( generated, tgtFile );
	}


	final DescriptionBuilder cases()
	{
		return this.cases;
	}

	final DescriptionBuilder methods()
	{
		return this.methods;
	}


	@SuppressWarnings( "unqualified-field-access" )
	private final class RuleCompiler
	{
		private final RewriteRule rule;

		public RuleCompiler( RewriteRule _rule )
		{
			super();
			this.rule = _rule;
		}

		final void compileCase()
		{
			final String funName = rule.fun.getName();
			final DescriptionBuilder b = cases();
			b.append( "case " ).append( funName ).appendLine( ":" );
		}

		final void compileSubCase()
		{
			final int paramCount = rule.params.size();
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


		private final LetDictionary letDict = new LetDictionary();
		private final Set<String> letVars = New.set();
		private char nextLetVarSuffix = 'a';

		private final UniqueLetVarName compileUniqueLetVarName( String _name )
		{
			final String varName;
			if (this.letVars.contains( _name )) {
				varName = _name + '$' + this.nextLetVarSuffix++;
			}
			else {
				this.letVars.add( _name );
				varName = _name;
			}
			final UniqueLetVarName result = new UniqueLetVarName( _name, varName );
			final DescriptionBuilder b = declBuilder();
			b.append( "final String " ).append( result.constName ).append( " = \"" ).append( result.varName ).appendLine(
					"__\" + newSanitizingSuffix();" );
			b.append( "final ExpressionNode " ).append( result.varName ).append( " = var( " ).append( result.constName )
					.appendLine( " );" );
			return result;
		}

		private final void let( UniqueLetVarName _let )
		{
			this.letDict.let( _let.letName, null, _let.varName );
		}

		private final void unlet( UniqueLetVarName _let )
		{
			this.letDict.unlet( _let.letName );
		}

		private final void resetLetVars()
		{
			this.letVars.clear();
			this.letDict.clear();
		}


		final void compileMethod() throws Exception
		{
			final String mtdName = mtdName();
			final DescriptionBuilder b = methods();
			final ExpressionNode expr = rule.body;

			resetLetVars();

			b.append( "private final ExpressionNode " ).append( mtdName ).appendLine(
					"( ExpressionNodeForFunction _fun ) {" );
			b.indent();
			{
				b.appendLine( "final Iterator<ExpressionNode> args = _fun.arguments().iterator();" );
				final StringBuilder prefix = new StringBuilder();
				final StringBuilder suffix = new StringBuilder();
				int iParam = 0;
				for (RewriteRule.Param param : rule.params) {
					switch (param.type) {

						case LIST:
							b.append( "final ExpressionNode " ).append( param.name ).appendLine( " = substitution( args );" );
							break;

						case SYMBOLIC:
							b.append( "final ExpressionNode " ).append( param.name ).appendLine(
									" = substitution( args.next() );" );
							break;

						default:
							final String paramExpr;
							if (param.type == RewriteRule.Param.Type.ARRAY) {
								paramExpr = "new ExpressionNodeForMakeArray( args.next() )";
							}
							else {
								paramExpr = "args.next()";
							}

							if (occursMoreThanOnce( expr, param.name )) {
								final UniqueLetVarName var = compileUniqueLetVarName( param.name );
								b.append( "final ExpressionNode " ).append( var.varName ).append( "$$ = " ).append( paramExpr )
										.appendLine( ";" );
								prefix.append( "let( " ).append( var.constName ).append( ", " ).append( var.varName ).append(
										"$$, " );
								suffix.append( " )" );
								let( var );
							}
							else {
								b.append( "final ExpressionNode " ).append( param.name ).append( " = substitution( " ).append(
										paramExpr ).appendLine( " );" );
							}

					}

					iParam++;
				}

				final DescriptionBuilder exprBuilder = new DescriptionBuilder();
				compileExpr( expr, exprBuilder );

				b.append( "return " ).append( prefix ).append( exprBuilder ).append( suffix ).appendLine( ";" );
			}
			b.outdent();
			b.appendLine( "}" );
			b.newLine();
		}


		private final String mtdName()
		{
			if (is_n_ary()) {
				return "rewrite" + rule.fun.getName();
			}
			return "rewrite" + rule.fun.getName() + "_" + rule.params.size();
		}

		private final boolean is_n_ary()
		{
			return rule.params.get( rule.params.size() - 1 ).type == RewriteRule.Param.Type.LIST;
		}

		private final boolean occursMoreThanOnce( final ExpressionNode _expr, String _param )
		{
			return countOccurrences_atLeast2( _expr, _param ) > 1;
		}

		private final int countOccurrences_atLeast2( ExpressionNode _expr, String _param )
		{
			if (_expr instanceof ExpressionNodeForLetVar) {
				final ExpressionNodeForLetVar varNode = (ExpressionNodeForLetVar) _expr;
				return varNode.varName().equals( _param )? 1 : 0;
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


		private final DescriptionBuilder declBuilder()
		{
			return methods();
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
			while (_args.hasNext()) {
				final ExpressionNode arg = _args.next();
				_b.append( ", " );
				compileExpr( arg, _b );
			}
		}

		private void compileArgList( ExpressionNode _node, DescriptionBuilder _b ) throws Exception
		{
			compileArgs( _node.arguments(), _b );
			_b.append( " )" );
		}


		private void compileConst( ExpressionNodeForConstantValue _value, DescriptionBuilder _b )
		{
			final Object value = _value.value();
			if (value instanceof Integer) {
				final int v = (Integer) value;
				if (v >= 0 && v < CONSTANT_NAMES.length) {
					_b.append( CONSTANT_NAMES[ v ] );
					return;
				}
			}

			// LATER These nodes could be moved out into static final members of the generator.

			_b.append( "cst( " );

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
			final String letName = _node.varName();
			final String varName = (String) this.letDict.lookup( letName );
			_b.append( (null == varName)? letName : varName );
		}


		private void compileLet( ExpressionNodeForLet _node, DescriptionBuilder _b ) throws Exception
		{
			final UniqueLetVarName var = compileUniqueLetVarName( _node.varName() );
			_b.append( "let( " ).append( var.constName ).append( ", " );
			compileExpr( _node.value(), _b );
			_b.append( ", " );
			let( var );
			compileExpr( _node.in(), _b );
			unlet( var );
			_b.append( " )" );
		}


		private void compileFold( ExpressionNodeForFold _fold, DescriptionBuilder _b ) throws Exception
		{
			final UniqueLetVarName acc = compileUniqueLetVarName( _fold.accumulatorName() );
			final UniqueLetVarName elt = compileUniqueLetVarName( _fold.elementName() );

			_b.append( "fold( " ).append( acc.constName ).append( ", " );
			compileExpr( _fold.initialAccumulatorValue(), _b );
			_b.append( ", " ).append( elt.constName ).append( ", " );
			let( acc );
			let( elt );
			compileExpr( _fold.accumulatingStep(), _b );
			unlet( elt );
			unlet( acc );
			_b.append( ", " ).append( _fold.mayReduce()? "true" : "false" );
			compileArgs( _fold.elements(), _b );
			_b.append( " )" );
		}


		private void compileReduce( ExpressionNodeForReduce _fold, DescriptionBuilder _b ) throws Exception
		{
			final UniqueLetVarName acc = compileUniqueLetVarName( _fold.accumulatorName() );
			final UniqueLetVarName elt = compileUniqueLetVarName( _fold.elementName() );

			_b.append( "reduce( " ).append( acc.constName ).append( ", " ).append( elt.constName ).append( ", " );
			let( acc );
			let( elt );
			compileExpr( _fold.accumulatingStep(), _b );
			unlet( elt );
			unlet( acc );
			_b.append( ", " );
			compileExpr( _fold.emptyValue(), _b );
			compileArgs( _fold.elements(), _b );
			_b.append( " )" );
		}


		private void compileFoldArray( ExpressionNodeForFoldArray _fold, DescriptionBuilder _b ) throws Exception
		{
			final UniqueLetVarName acc = compileUniqueLetVarName( _fold.accumulatorName() );
			final UniqueLetVarName elt = compileUniqueLetVarName( _fold.elementName() );
			final UniqueLetVarName idx = compileUniqueLetVarName( _fold.indexName() );

			_b.append( "folda( " ).append( acc.constName ).append( ", " );
			compileExpr( _fold.initialAccumulatorValue(), _b );
			_b.append( ", " ).append( elt.constName ).append( ", " ).append( idx.constName ).append( ", " );
			let( acc );
			let( elt );
			let( idx );
			compileExpr( _fold.accumulatingStep(), _b );
			unlet( idx );
			unlet( elt );
			unlet( acc );
			compileArgs( _fold.elements(), _b );
			_b.append( " )" );
		}

	}

	private static final class UniqueLetVarName
	{
		public final String letName;
		public final String varName;
		public final String constName;

		public UniqueLetVarName( String _letName, String _varName )
		{
			super();
			this.letName = _letName;
			this.varName = _varName;
			this.constName = _varName + "$";
		}
	}


	protected static final String[] CONSTANT_NAMES = { "ZERO", "ONE", "TWO", "THREE" };
}
