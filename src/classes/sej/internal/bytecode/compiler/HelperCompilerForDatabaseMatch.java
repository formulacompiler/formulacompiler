package sej.internal.bytecode.compiler;

import org.objectweb.asm.Label;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.CompilerException;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.LetDictionary.LetEntry;

final class HelperCompilerForDatabaseMatch extends MethodCompiler
{
	private final ExpressionNode node;

	public HelperCompilerForDatabaseMatch(SectionCompiler _section, ExpressionNode _node, Iterable<LetEntry> _closure)
	{
		super( _section, 0, _section.newGetterName(), "(" + descriptorOf( _section, _closure ) + ")Z" );
		this.node = _node;
		addClosureToLetDict( _closure );
	}

	@Override
	protected void compileBody() throws CompilerException
	{
		final GeneratorAdapter mv = mv();
		final Label ifFalse = mv.newLabel();
		numericCompiler().compileTest( this.node, ifFalse );
		mv.push( true );
		mv.returnValue();
		mv.visitLabel( ifFalse );
		mv.push( false );
		mv.returnValue();
	}

}
