package sej.internal.bytecode.compiler;

import java.io.IOException;

import sej.describable.DescriptionBuilder;
import sej.internal.expressions.ExpressionDescriptionConfig;
import sej.internal.expressions.ExpressionNode;

public class ExpressionNodeForInnerFoldedObjRef extends ExpressionNode
{
	private final FoldContext context;

	public ExpressionNodeForInnerFoldedObjRef(FoldContext _context, ExpressionNode _elt)
	{
		super( _elt );
		this.context = _context;
		this.setDataType( _elt.getDataType() );
	}
	
	
	public final FoldContext context()
	{
		return this.context;
	}


	@Override
	public ExpressionNode cloneWithoutArguments()
	{
		return null;
	}

	@Override
	protected void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg ) throws IOException
	{
		_to.append( "sub->" );
		argument( 0 ).describeTo( _to, _cfg );
	}

}
