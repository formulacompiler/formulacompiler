package sej.internal.bytecode.compiler;

import org.objectweb.asm.Type;

import sej.CompilerException;
import sej.internal.expressions.ExpressionNode;
import sej.internal.model.SectionModel;

final class ByteCodeSubSectionCompiler extends ByteCodeSectionCompiler
{
	private final ByteCodeSectionCompiler parentSectionCompiler;
	private final String arrayDescriptor;
	private final Type arrayType;
	private final String getterName;
	private final String getterDescriptor;


	ByteCodeSubSectionCompiler(ByteCodeSectionCompiler _parent, SectionModel _model)
	{
		super( _parent.engineCompiler(), _model, _parent.engineCompiler().newSubClassName() );
		this.parentSectionCompiler = _parent;
		this.arrayDescriptor = "[" + classDescriptor();
		this.arrayType = Type.getType( arrayDescriptor() );
		this.getterName = "get" + className();
		this.getterDescriptor = "()" + arrayDescriptor();
		_parent.addSubSectionCompiler( _model, this );
	}


	@Override
	ByteCodeSectionCompiler parentSectionCompiler()
	{
		return this.parentSectionCompiler;
	}

	String arrayDescriptor()
	{
		return this.arrayDescriptor;
	}

	Type arrayType()
	{
		return this.arrayType;
	}

	String getterName()
	{
		return this.getterName;
	}

	String getterDescriptor()
	{
		return this.getterDescriptor;
	}


	public ByteCodeSectionNumericMethodCompiler compileExpr( ExpressionNode _node ) throws CompilerException
	{
		ByteCodeHelperCompilerForSubExpr result = new ByteCodeHelperCompilerForSubExpr( this, _node );
		result.compile();
		return result;
	}

}
