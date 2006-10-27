package sej.internal.model.optimizer.consteval;

import sej.internal.expressions.ExpressionNodeForConstantValue;
import sej.internal.expressions.ExpressionNodeForFold1st;
import sej.internal.model.util.InterpretedNumericType;

final class EvalFold1st extends EvalAbstractFold
{
	private static final Object NO_VALUE = new Object();
	
	private final String firstName;

	public EvalFold1st(ExpressionNodeForFold1st _node, InterpretedNumericType _type)
	{
		super( _node, _type );
		this.firstName = _node.firstName();
	}


	@Override
	protected int evalFixedArgs( Object[] _args, int _i0 )
	{
		int i0 = super.evalFixedArgs( _args, _i0 );
		_args[ i0++ ] = node().argument( 2 ); // first
		return i0;
	}
	
	
	@Override
	protected Object initial( Object[] _args )
	{
		return NO_VALUE;
	}
	
	
	private boolean haveFirst = false;

	@Override
	protected Object fold( Object _acc, Object _val )
	{
		if (this.haveFirst) {
			return super.fold( _acc, _val );
		}
		else {
			this.haveFirst = true;
			letDict().let( this.firstName, null, _val );
			try {
				return evaluateArgument( 2 ); // first
			}
			finally {
				letDict().unlet( this.firstName );
			}
		}
	}


	@Override
	protected void insertPartialFold( Object _acc )
	{
		if (_acc != NO_VALUE) {
			node().addArgument( new ExpressionNodeForConstantValue( _acc ) );
		}
	}


}
