package sej.internal.model.util;

import sej.CompilerException;
import sej.NumericType;
import sej.internal.model.ComputationModel;
import sej.internal.model.ComputationModelTransformer;
import sej.internal.model.analysis.TypeAnnotator;
import sej.internal.model.optimizer.ConstantSubExpressionEliminator;
import sej.internal.model.optimizer.IntermediateResultsInliner;
import sej.internal.model.rewriting.ModelRewriter;
import sej.internal.model.rewriting.SubstitutionInliner;
import sej.runtime.EngineException;

public final class ComputationModelTransformerImpl implements ComputationModelTransformer
{
	private final ComputationModel model;
	private final NumericType numericType;

	public ComputationModelTransformerImpl(Config _config)
	{
		super();
		_config.validate();
		this.model = _config.model;
		this.numericType = _config.numericType;
	}

	public static final class Factory implements ComputationModelTransformer.Factory
	{
		public ComputationModelTransformer newInstance( Config _config )
		{
			return new ComputationModelTransformerImpl( _config );
		}
	}


	public ComputationModel getModel()
	{
		return this.model;
	}

	public NumericType getNumericType()
	{
		return this.numericType;
	}


	public ComputationModel destructiveTransform() throws CompilerException, EngineException
	{
		rewriteExpressions();
		eliminateConstantSubExpressions();
		inlineIntermediateResults();
		inlineSubstitutions();
		annotateTypes();
		return getModel();
	}


	private void rewriteExpressions() throws CompilerException
	{
		this.model.traverse( new ModelRewriter( InterpretedNumericType.typeFor( getNumericType() ) ) );
	}


	private void eliminateConstantSubExpressions() throws CompilerException
	{
		this.model.traverse( new ConstantSubExpressionEliminator( getNumericType() ) );
	}


	private void inlineIntermediateResults() throws CompilerException
	{
		this.model.traverse( new IntermediateResultsInliner() );
	}

	private void inlineSubstitutions() throws CompilerException
	{
		this.model.traverse( new SubstitutionInliner() );
	}

	private void annotateTypes() throws CompilerException
	{
		this.model.traverse( new TypeAnnotator() );
	}


}
