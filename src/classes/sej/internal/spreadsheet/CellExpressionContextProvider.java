package sej.internal.spreadsheet;

import sej.describable.DescriptionBuilder;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionSourceAsContextProvider;

final class CellExpressionContextProvider extends ExpressionSourceAsContextProvider
{
	private final CellInstance cell;

	public CellExpressionContextProvider(CellInstance _cell, ExpressionNode _expr)
	{
		super( _expr );
		this.cell = _cell;
	}


	@Override
	public void buildContext( DescriptionBuilder _result, ExpressionNode _focusedNode )
	{
		super.buildContext( _result, _focusedNode );
		_result.append( "\nCell containing expression is " ).append( this.cell.getCanonicalName() ).append( "." );
	}

}
