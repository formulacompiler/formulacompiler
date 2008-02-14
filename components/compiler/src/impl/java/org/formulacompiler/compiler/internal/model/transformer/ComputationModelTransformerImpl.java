/*
 * Copyright (c) 2006, 2008 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * This file is part of the Abacus Formula Compiler (AFC).
 *
 * For commercial licensing, please contact sales(at)formulacompiler.com.
 *
 * AFC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AFC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AFC.  If not, see <http://www.gnu.org/licenses/>.
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
