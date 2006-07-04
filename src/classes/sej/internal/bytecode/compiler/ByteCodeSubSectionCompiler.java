package sej.internal.bytecode.compiler;

import sej.internal.model.SectionModel;

final class ByteCodeSubSectionCompiler extends ByteCodeSectionCompiler
{
	private final ByteCodeSectionCompiler parentSectionCompiler;
	private final String arrayDescriptor;
	private final String getterName;
	private final String getterDescriptor;


	ByteCodeSubSectionCompiler(ByteCodeSectionCompiler _parent, SectionModel _model)
	{
		super( _parent.engineCompiler(), _model, _parent.engineCompiler().newSubClassName() );
		this.parentSectionCompiler = _parent;
		this.arrayDescriptor = "[" + classDescriptor();
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

	String getterName()
	{
		return this.getterName;
	}

	String getterDescriptor()
	{
		return this.getterDescriptor;
	}

}
