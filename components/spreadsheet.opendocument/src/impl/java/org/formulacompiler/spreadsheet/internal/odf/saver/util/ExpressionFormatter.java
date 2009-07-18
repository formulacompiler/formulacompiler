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

package org.formulacompiler.spreadsheet.internal.odf.saver.util;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.Operator;
import org.formulacompiler.compiler.internal.Duration;
import org.formulacompiler.compiler.internal.LocalDate;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFunction;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForOperator;
import org.formulacompiler.runtime.New;
import org.formulacompiler.spreadsheet.internal.CellIndex;
import org.formulacompiler.spreadsheet.internal.CellRange;
import org.formulacompiler.spreadsheet.internal.ExpressionNodeForCell;
import org.formulacompiler.spreadsheet.internal.ExpressionNodeForRange;
import org.formulacompiler.spreadsheet.internal.ExpressionNodeForRangeIntersection;
import org.formulacompiler.spreadsheet.internal.ExpressionNodeForRangeUnion;

public class ExpressionFormatter
{
	private static final Set<Operator> COMMUTATIVE_OPERATORS = New.set();

	static {
		COMMUTATIVE_OPERATORS.add( Operator.CONCAT );
		COMMUTATIVE_OPERATORS.add( Operator.PLUS );
		COMMUTATIVE_OPERATORS.add( Operator.TIMES );
	}

	public static String format( ExpressionNode _expr, CellIndex _baseCell )
	{
		StringBuilder stringBuilder = new StringBuilder( "=" );
		append( stringBuilder, _expr, _baseCell, null );
		return stringBuilder.toString();
	}

	private static void append( StringBuilder _stringBuilder, ExpressionNode _expr, CellIndex _baseCell, Operator _context )
	{
		if (_expr instanceof ExpressionNodeForConstantValue) {
			final Object constantValue = _expr.getConstantValue();
			if (constantValue instanceof Boolean) {
				_stringBuilder.append( ((Boolean) constantValue) ? "TRUE()" : "FALSE()" );
			}
			else if (constantValue instanceof Date) {
				throw new IllegalArgumentException( "Saving dates is not supported." );
			}
			else if (constantValue instanceof LocalDate) {
				final LocalDate localDate = (LocalDate) constantValue;
				_stringBuilder.append( localDate.doubleValue() );
			}
			else if (constantValue instanceof Duration) {
				final Duration duration = (Duration) constantValue;
				_stringBuilder.append( duration.doubleValue() );
			}
			else if (constantValue instanceof Number) {
				_stringBuilder.append( constantValue.toString() );
			}
			else if (constantValue instanceof String) {
				_stringBuilder.append( '"' );
				final String str = (String) constantValue;
				_stringBuilder.append( str.replaceAll( "\"", "\"\"" ) );
				_stringBuilder.append( '"' );
			}

		}
		else if (_expr instanceof ExpressionNodeForOperator) {
			final Operator operator = ((ExpressionNodeForOperator) _expr).getOperator();
			final List<ExpressionNode> arguments = _expr.arguments();
			final int argCount = arguments.size();
			final boolean parenthesesNeeded = _context != null &&
					!(operator.equals( _context ) && COMMUTATIVE_OPERATORS.contains( operator ));
			switch (argCount) {
				case 0:
					_stringBuilder.append( operator.getSymbol() );
					break;
				case 1:
					if (parenthesesNeeded) _stringBuilder.append( '(' );
					final boolean isPrefix = operator.isPrefix();
					if (isPrefix) _stringBuilder.append( operator.getSymbol() );
					append( _stringBuilder, arguments.get( 0 ), _baseCell, operator );
					if (!isPrefix) _stringBuilder.append( operator.getSymbol() );
					if (parenthesesNeeded) _stringBuilder.append( ')' );
					break;
				default:
					if (parenthesesNeeded) _stringBuilder.append( '(' );
					appendArguments( _stringBuilder, arguments, operator.getSymbol(), _baseCell, operator );
					if (parenthesesNeeded) _stringBuilder.append( ')' );
					break;
			}
		}
		else if (_expr instanceof ExpressionNodeForFunction) {
			final Function function = ((ExpressionNodeForFunction) _expr).getFunction();
			_stringBuilder.append( function );
			_stringBuilder.append( '(' );
			final List<ExpressionNode> argList = _expr.arguments();
			appendArguments( _stringBuilder, argList, ";", _baseCell, null );
			_stringBuilder.append( ')' );
		}
		else if (_expr instanceof ExpressionNodeForCell) {
			final ExpressionNodeForCell expressionNodeForCell = (ExpressionNodeForCell) _expr;
			final String name = expressionNodeForCell.getName();
			if (name != null) {
				_stringBuilder.append( name );
			}
			else {
				final CellIndex cellIndex = expressionNodeForCell.getCellIndex();
				_stringBuilder.append( '[' );
				RefFormatter.append( _stringBuilder, cellIndex, _baseCell );
				_stringBuilder.append( ']' );
			}
		}
		else if (_expr instanceof ExpressionNodeForRange) {
			final ExpressionNodeForRange expressionNodeForRange = (ExpressionNodeForRange) _expr;
			final String name = expressionNodeForRange.getName();
			if (name != null) {
				_stringBuilder.append( name );
			}
			else {
				final CellRange range = expressionNodeForRange.getRange();
				_stringBuilder.append( '[' );
				RefFormatter.append( _stringBuilder, range, _baseCell );
				_stringBuilder.append( ']' );
			}
		}
		else if (_expr instanceof ExpressionNodeForRangeIntersection) {
			final List<ExpressionNode> arguments = _expr.arguments();
			appendArguments( _stringBuilder, arguments, "!", _baseCell, null );
		}
		else if (_expr instanceof ExpressionNodeForRangeUnion) {
			final List<ExpressionNode> arguments = _expr.arguments();
			appendArguments( _stringBuilder, arguments, ";", _baseCell, null );
		}
		else {
			throw new IllegalArgumentException( "Cannot format " + _expr );
		}
	}

	private static void appendArguments( final StringBuilder _stringBuilder, final List<ExpressionNode> _argList, final String _separator, final CellIndex _baseCell, Operator _context )
	{
		final Iterator<ExpressionNode> argIterator = _argList.iterator();
		if (argIterator.hasNext()) {
			append( _stringBuilder, argIterator.next(), _baseCell, _context );
		}
		while (argIterator.hasNext()) {
			_stringBuilder.append( _separator );
			append( _stringBuilder, argIterator.next(), _baseCell, _context );
		}
	}
}
