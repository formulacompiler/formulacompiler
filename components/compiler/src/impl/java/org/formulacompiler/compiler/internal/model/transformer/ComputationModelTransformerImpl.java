/*
 * Copyright (c) 2006 by Abacus Research AG, Switzerland.
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
package org.formulacompiler.compiler.internal.model.transformer;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.internal.model.ComputationModel;
import org.formulacompiler.compiler.internal.model.ComputationModelTransformer;
import org.formulacompiler.compiler.internal.model.analysis.ModelIsTypedChecker;
import org.formulacompiler.compiler.internal.model.analysis.TypeAnnotator;
import org.formulacompiler.compiler.internal.model.interpreter.InterpretedNumericType;
import org.formulacompiler.compiler.internal.model.optimizer.ConstantSubExpressionEliminator;
import org.formulacompiler.compiler.internal.model.optimizer.IntermediateResultsInliner;
import org.formulacompiler.compiler.internal.model.rewriting.ModelRewriter;
import org.formulacompiler.compiler.internal.model.rewriting.SubstitutionInliner;
import org.formulacompiler.runtime.EngineException;


public final class ComputationModelTransformerImpl implements ComputationModelTransformer
{
	private final ComputationModel model;
	private final NumericType numericType;

	public ComputationModelTransformerImpl( Config _config )
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

		annotateTypes();
		assert modelIsFullyTyped(): "Typer should fully type the model";

		eliminateConstantSubExpressions();
		assert modelIsFullyTyped(): "CSE should leave the model fully typed";

		inlineIntermediateResults();
		assert modelIsFullyTyped(): "Cell inlining should leave the model fully typed";

		inlineSubstitutions();
		assert modelIsFullyTyped(): "Substitution inlining should leave the model fully typed";

		return getModel();
	}


	private boolean modelIsFullyTyped() throws CompilerException
	{
		this.model.traverse( new ModelIsTypedChecker() );
		return true;
	}


	private void rewriteExpressions() throws CompilerException
	{
		this.model.traverse( new ModelRewriter( InterpretedNumericType.typeFor( getNumericType(), this.model
				.getEnvironment() ) ) );
	}


	private void eliminateConstantSubExpressions() throws CompilerException
	{
		this.model.traverse( new ConstantSubExpressionEliminator( getNumericType(), this.model.getEnvironment() ) );
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
