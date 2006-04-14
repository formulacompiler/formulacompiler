package sej.engine.expressions;


public interface EvaluationStrategy
{
	Object evaluate( ExpressionNode _node, EvaluationContext _context ) throws EvaluationFailed;
}
