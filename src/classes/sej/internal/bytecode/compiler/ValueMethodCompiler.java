package sej.internal.bytecode.compiler;

import sej.internal.expressions.DataType;

abstract class ValueMethodCompiler extends TypedMethodCompiler
{

	public ValueMethodCompiler(SectionCompiler _section, int _access, String _methodName, String _argDescriptor,
			DataType _type)
	{
		super( _section, _access, _methodName, "(" + _argDescriptor + ")" + returnType( _section, _type ), _type );
	}

	
	protected static final String returnType( SectionCompiler _section, DataType _type )
	{
		return _section.engineCompiler().typeCompiler( _type ).typeDescriptor();
	}

	
	@Override
	protected void endCompilation()
	{
		mv().visitInsn( typeCompiler().returnOpcode() );
		super.endCompilation();
	}


}
