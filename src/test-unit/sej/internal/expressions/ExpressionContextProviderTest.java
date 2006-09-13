package sej.internal.expressions;

import sej.Function;
import sej.Operator;
import sej.describable.DescriptionBuilder;
import junit.framework.TestCase;

public class ExpressionContextProviderTest extends TestCase
{
	private final ExpressionNode parsedInnerInner = new ExpressionNodeForConstantValue( 3.141 );
	private final ExpressionNode parsedInner = new ExpressionNodeForFunction( Function.ROUND, this.parsedInnerInner,
			new ExpressionNodeForConstantValue( 0 ) );
	private final ExpressionNode parsed = new ExpressionNodeForOperator( Operator.PLUS, this.parsedInner,
			new ExpressionNodeForConstantValue( 2 ) );
	private final ExpressionNode derived = new ExpressionNodeForOperator( Operator.PLUS,
			new ExpressionNodeForConstantValue( 3 ), new ExpressionNodeForConstantValue( 2 ) );
	private final ExpressionNode derivedInner = this.derived.arguments().get( 0 );

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		this.parsed.setContextProvider( new CellExpressionAsContextProvider( "A1", this.parsed ) );
		this.derived.setDerivedFrom( this.parsed );
		this.derivedInner.setDerivedFrom( this.parsedInner );
	}


	public void testNoFocus() throws Exception
	{
		String ctx = this.derived.getContext( null );
		assertEquals( "\nIn expression (ROUND( 3.141, 0 ) + 2); error location indicated by >>..<<.\nIn cell A1.", ctx );
	}

	public void testOuterFocus() throws Exception
	{
		String ctx = this.derived.getContext( this.derived );
		assertEquals( "\nIn expression  >> (ROUND( 3.141, 0 ) + 2) << ; error location indicated by >>..<<.\nIn cell A1.", ctx );
	}

	public void testInnerFocus() throws Exception
	{
		String ctx = this.derived.getContext( this.derivedInner );
		assertEquals( "\nIn expression ( >> ROUND( 3.141, 0 ) <<  + 2); error location indicated by >>..<<.\nIn cell A1.", ctx );
	}

	public void testInnerFocusOnBase() throws Exception
	{
		String ctx = this.parsed.getContext( this.parsedInner );
		assertEquals( "\nIn expression ( >> ROUND( 3.141, 0 ) <<  + 2); error location indicated by >>..<<.\nIn cell A1.", ctx );
	}

	public void testDeepInnerFocusOnBase() throws Exception
	{
		String ctx = this.parsed.getContext( this.parsedInnerInner );
		assertEquals( "\nIn expression (ROUND(  >> 3.141 << , 0 ) + 2); error location indicated by >>..<<.\nIn cell A1.", ctx );
	}


	private static final class CellExpressionAsContextProvider extends ExpressionSourceAsContextProvider
	{
		private final String cellName;

		public CellExpressionAsContextProvider(String _cellName, ExpressionNode _expr)
		{
			super( _expr );
			this.cellName = _cellName;
		}

		@Override
		public void buildContext( DescriptionBuilder _result, ExpressionNode _focusedNode )
		{
			super.buildContext( _result, _focusedNode );
			_result.append( "\nIn cell " ).append( this.cellName ).append( "." );
		}

	}

}
