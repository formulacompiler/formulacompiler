/*
 * Copyright © 2006 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are prohibited, unless you have been explicitly granted 
 * more rights by Abacus Research AG.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS 
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND 
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR 
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package sej.internal.model.transformer;

import sej.compiler.CompilerException;
import sej.compiler.NumericType;
import sej.internal.model.ComputationModel;
import sej.internal.model.ComputationModelTransformer;
import sej.internal.model.analysis.TypeAnnotator;
import sej.internal.model.interpreter.InterpretedNumericType;
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
