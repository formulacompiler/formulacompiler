package org.formulacompiler.compiler.internal.expressions;

public interface TypedResult
{
	public DataType getDataType();
	public boolean isConstant();
	public boolean hasConstantValue();
	public Object getConstantValue();
}
