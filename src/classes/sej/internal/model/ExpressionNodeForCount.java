package sej.internal.model;

import java.io.IOException;
import java.util.Collection;

import sej.describable.DescriptionBuilder;
import sej.internal.expressions.ExpressionDescriptionConfig;
import sej.internal.expressions.ExpressionNode;

public final class ExpressionNodeForCount extends ExpressionNode
{
	private final int staticValueCount;
	private final SectionModel[] subSectionModels;
	private final int[] subSectionStaticValueCounts;

	public ExpressionNodeForCount(int _staticValueCount, SectionModel[] _subSectionModels,
			int[] _subSectionStaticValueCounts)
	{
		super();
		this.staticValueCount = _staticValueCount;
		this.subSectionModels = _subSectionModels;
		this.subSectionStaticValueCounts = _subSectionStaticValueCounts;
	}
	
	
	public final int staticValueCount()
	{
		return this.staticValueCount;
	}


	public final SectionModel[] subSectionModels()
	{
		return this.subSectionModels;
	}


	public final int[] subSectionStaticValueCounts()
	{
		return this.subSectionStaticValueCounts;
	}


	@Override
	protected int countValuesCore( Collection<ExpressionNode> _uncountables )
	{
		throw new AbstractMethodError();
	}


	@Override
	protected void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg ) throws IOException
	{
		_to.append( "(" ).append( this.staticValueCount );
		for (int i = 0; i < this.subSectionModels.length; i++) {
			_to.append( " + " ).append( this.subSectionModels[ i ].getName() ).append( ".size()" );
			_to.append( " * " ).append( this.subSectionStaticValueCounts[ i ] );
		}
		_to.append( ")" );
	}


	@Override
	protected ExpressionNode innerCloneWithoutArguments()
	{
		// Array sharing should be OK here.
		return new ExpressionNodeForCount( this.staticValueCount, this.subSectionModels, this.subSectionStaticValueCounts );
	}


}
