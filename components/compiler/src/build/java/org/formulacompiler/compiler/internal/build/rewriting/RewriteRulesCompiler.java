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
import java.util.Map;
import java.util.Set;

import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.internal.build.Util;
import org.formulacompiler.compiler.internal.build.rewriting.AbstractDef.Param;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldApply;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldDefinition;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldList;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldVectors;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFunction;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForLet;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForLetVar;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForMaxValue;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForMinValue;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForOperator;
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
	private final List<RuleDef> rules = New.list();
	private final Map<String, FoldDef> folds = New.map();


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
		parse();
		compileFolds();
		compileRules();
		writeOut();
	}


	private void parse() throws Exception
	{
		parseRulesIn( "rewrite.rules" );
	}

	private void parseRulesIn( String _fileName ) throws Exception
	{

		final String src = Util.readStringFrom( ClassLoader.getSystemResourceAsStream( FILES_PATH + _fileName ) );
		new RewriteRuleExpressionParser( src, this.rules, this.folds ).parseFile();
	}


	private void compileFolds() throws Exception
	{
		for (final FoldDef fold : this.folds.values()) {
			final FoldCompiler foldc = new FoldCompiler( fold );
			foldc.compileMethod();
		}
	}


	private void compileRules() throws Exception
	{
		Collections.sort( this.rules );

		Function currCase = null;
		for (final RuleDef rule : this.rules) {
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
	private final class FoldCompiler extends AbstractDefCompiler
	{
		private final FoldDef fold;

		public FoldCompiler( FoldDef _fold )
		{
			super( _fold );
			this.fold = _fold;
		}

		@Override
		protected final String mtdName()
		{
			return "fold_" + fold.name;
		}

		@Override
		protected void compileMethodHeader( DescriptionBuilder _b )
		{
			_b.append( "final ExpressionNode " ).append( mtdName() ).appendLine( "() {" );
		}

		@Override
		protected void compileMethodIntro( DescriptionBuilder _b )
		{
			// Nothing to do here.
		}

	}


	@SuppressWarnings( "unqualified-field-access" )
	private final class RuleCompiler extends AbstractDefCompiler
	{
		private final RuleDef rule;

		public RuleCompiler( RuleDef _rule )
		{
			super( _rule );
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

		@Override
		protected final String mtdName()
		{
			if (is_n_ary()) {
				return "rewrite" + rule.fun.getName();
			}
			return "rewrite" + rule.fun.getName() + "_" + rule.params.size();
		}

		@Override
		protected void compileMethodHeader( DescriptionBuilder _b )
		{
			_b.append( "private final ExpressionNode " ).append( mtdName() ).appendLine(
					"( ExpressionNodeForFunction _fun ) {" );
		}

		@Override
		protected void compileMethodIntro( DescriptionBuilder _b )
		{
			_b.appendLine( "final Iterator<ExpressionNode> args = _fun.arguments().iterator();" );
		}

	}


	@SuppressWarnings( "unqualified-field-access" )
	private abstract class AbstractDefCompiler
	{
		private final AbstractDef def;

		public AbstractDefCompiler( AbstractDef _def )
		{
			super();
			this.def = _def;
		}

		private final LetDictionary letDict = new LetDictionary();
		private final Set<String> letVars = New.set();
		private char nextLetVarSuffix = 'a';

		protected final UniqueLetVarName compileUniqueLetVarName( String _name )
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
			if (_let != null) this.letDict.let( _let.letName, null, _let.varName );
		}

		private final void unlet( UniqueLetVarName _let )
		{
			if (_let != null) this.letDict.unlet( _let.letName );
		}

		private final void unlet( int _n )
		{
			this.letDict.unlet( _n );
		}

		private final void resetLetVars()
		{
			this.letVars.clear();
			this.letDict.clear();
		}


		final void compileMethod() throws Exception
		{
			final DescriptionBuilder b = methods();
			final ExpressionNode expr = def.body;

			resetLetVars();

			compileMethodHeader( b );
			b.indent();
			{
				compileMethodIntro( b );
				final StringBuilder prefix = new StringBuilder();
				final StringBuilder suffix = new StringBuilder();
				int iParam = 0;
				for (Param param : def.params) {
					switch (param.type) {

						case LIST:
							b.append( "final ExpressionNode " ).append( param.name ).appendLine( " = substitution( args );" );
							break;

						case ARRAY:
						case SYMBOLIC:
							b.append( "final ExpressionNode " ).append( param.name ).appendLine(
									" = substitution( args.next() );" );
							break;

						default:
							final String paramExpr = "args.next()";
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

		protected abstract String mtdName();
		protected abstract void compileMethodIntro( DescriptionBuilder _b );
		protected abstract void compileMethodHeader( DescriptionBuilder _b );

		protected final boolean is_n_ary()
		{
			return def.params.get( def.params.size() - 1 ).type == Param.Type.LIST;
		}

		private final boolean occursMoreThanOnce( final ExpressionNode _expr, String _param )
		{
			return countOccurrences_atLeast2( _expr, _param ) > 1;
		}

		private final int countOccurrences_atLeast2( ExpressionNode _expr, String _param )
		{
			if (null == _expr) {
				return 0;
			}
			else if (_expr instanceof ExpressionNodeForLetVar) {
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
			else if (_expr instanceof ExpressionNodeForFoldDefinition) {
				final ExpressionNodeForFoldDefinition fold = (ExpressionNodeForFoldDefinition) _expr;
				int occ = countOccurrences_atLeast2( fold.whenEmpty(), _param );
				for (int i = 0; i < fold.accuCount(); i++)
					occ += countOccurrences_atLeast2( fold.accuInit( i ), _param );
				if (!equalsOneOf( _param, fold.accuNames() )) {
					if (!_param.equals( fold.indexName() ) && !equalsOneOf( _param, fold.eltNames() )) {
						for (int i = 0; i < fold.accuCount(); i++)
							occ += countOccurrences_atLeast2( fold.accuStep( i ), _param );
					}
					if (!_param.equals( fold.countName() )) {
						occ += countOccurrences_atLeast2( fold.merge(), _param );
					}
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


		private boolean equalsOneOf( String _param, String[] _names )
		{
			for (String name : _names)
				if (_param.equals( name )) return true;
			return false;
		}


		private final DescriptionBuilder declBuilder()
		{
			return methods();
		}


		private final void compileExpr( ExpressionNode _node, DescriptionBuilder _b ) throws Exception
		{
			if (null == _node) _b.append( "null" );

			else if (_node instanceof ExpressionNodeForConstantValue) compileConst(
					(ExpressionNodeForConstantValue) _node, _b );
			else if (_node instanceof ExpressionNodeForMinValue) compileExtremum( false, _b );
			else if (_node instanceof ExpressionNodeForMaxValue) compileExtremum( true, _b );
			else if (_node instanceof ExpressionNodeForOperator) compileOp( (ExpressionNodeForOperator) _node, _b );
			else if (_node instanceof ExpressionNodeForFunction) compileFun( (ExpressionNodeForFunction) _node, _b );
			else if (_node instanceof ExpressionNodeForLetVar) compileLetVar( (ExpressionNodeForLetVar) _node, _b );
			else if (_node instanceof ExpressionNodeForLet) compileLet( (ExpressionNodeForLet) _node, _b );

			else if (_node instanceof ExpressionNodeForFoldDefinition) compileFoldDef(
					(ExpressionNodeForFoldDefinition) _node, _b );
			else if (_node instanceof ExpressionNodeForFoldList) compileFoldApply( (ExpressionNodeForFoldList) _node, _b );
			else if (_node instanceof ExpressionNodeForFoldVectors) compileFoldApply(
					(ExpressionNodeForFoldVectors) _node, _b );

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

		private void compileExtremum( boolean _isMax, DescriptionBuilder _b ) throws Exception
		{
			_b.append( "new ExpressionNodeFor" ).append( _isMax? "Max" : "Min" ).append( "Value()" );
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


		private void compileFoldDef( ExpressionNodeForFoldDefinition _def, DescriptionBuilder _b ) throws Exception
		{
			final UniqueLetVarName[] accuNames = new UniqueLetVarName[ _def.accuCount() ];
			final UniqueLetVarName[] eltNames = new UniqueLetVarName[ _def.eltCount() ];
			final UniqueLetVarName indexName, countName;

			_b.append( "new ExpressionNodeForFoldDefinition( new String[] {" );
			for (int i = 0; i < _def.accuCount(); i++) {
				if (i > 0) _b.append( ", " );
				accuNames[ i ] = compileConstName( _def.accuName( i ), _b );
			}
			_b.append( "}, new ExpressionNode[] {" );
			for (int i = 0; i < _def.accuCount(); i++) {
				if (i > 0) _b.append( ", " );
				compileExpr( _def.accuInit( i ), _b );
			}
			_b.append( "}, " );
			indexName = compileConstName( _def.indexName(), _b );
			_b.append( ", new String[] {" );
			for (int i = 0; i < _def.eltCount(); i++) {
				if (i > 0) _b.append( ", " );
				eltNames[ i ] = compileConstName( _def.eltName( i ), _b );
			}
			_b.append( "}, new ExpressionNode[] {" );

			for (int i = 0; i < _def.accuCount(); i++)
				let( accuNames[ i ] );
			for (int i = 0; i < _def.eltCount(); i++)
				let( eltNames[ i ] );
			let( indexName );

			for (int i = 0; i < _def.accuCount(); i++) {
				if (i > 0) _b.append( ", " );
				compileExpr( _def.accuStep( i ), _b );
			}
			unlet( indexName );
			unlet( _def.eltCount() );

			_b.append( "}, " );
			countName = compileConstName( _def.countName(), _b );
			_b.append( ", " );

			let( countName );

			compileExpr( _def.merge(), _b );

			unlet( countName );
			unlet( _def.accuCount() );

			_b.append( ", " );
			compileExpr( _def.whenEmpty(), _b );
			_b.append( ", " ).append( _def.mayRearrange() ).append( ", " ).append( _def.mayReduce() ).append( " )" );
		}

		private UniqueLetVarName compileConstName( String _varName, DescriptionBuilder _b )
		{
			if (null == _varName) {
				_b.append( "null" );
				return null;
			}
			else {
				UniqueLetVarName result = compileUniqueLetVarName( _varName );
				_b.append( result.constName );
				return result;
			}
		}

		private void compileFoldApply( ExpressionNodeForFoldApply _apply, DescriptionBuilder _b ) throws Exception
		{
			final String className = _apply.getClass().getName();
			final ExpressionNode foldDefNode = _apply.argument( 0 );
			if (foldDefNode instanceof ExpressionNodeForLetVar) {
				final ExpressionNodeForLetVar foldNameNode = (ExpressionNodeForLetVar) foldDefNode;
				final String foldName = foldNameNode.varName();
				_b.append( "new " ).append( className ).append( "( fold_" ).append( foldName ).append( "()" );
			}
			else {
				_b.append( "new " ).append( className ).append( "( " );
				compileExpr( foldDefNode, _b );
			}
			compileArgs( _apply.elements(), _b );
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
