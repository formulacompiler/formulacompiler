

import java.util.List;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.CompilerException;
import sej.Operator;
import sej.internal.bytecode.compiler.HelperCompiler;
import sej.internal.bytecode.compiler.SectionCompiler;
import sej.internal.bytecode.compiler.SubSectionCompiler;
import sej.internal.bytecode.compiler.TypedMethodCompiler;
import sej.internal.bytecode.compiler.ValueMethodCompiler;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForOperator;
import sej.internal.model.ExpressionNodeForSubSectionModel;


final class HelperCompilerForIteration extends HelperCompiler
{
	private final SubSectionCompiler sub;
	private final Operator reductor;


	HelperCompilerForIteration(SectionCompiler _section, Operator _reductor,
			ExpressionNodeForSubSectionModel _node)
	{
		super( _section, _node );
		this.sub = section().subSectionCompiler( _node.getSectionModel() );
		this.reductor = _reductor;
	}


	@Override
	protected void compileBody() throws CompilerException
	{
		final SubSectionCompiler sub = this.sub;
		final TypedMethodCompiler subExpr = compileSubExpr( node().arguments() );

		final GeneratorAdapter mv = mv();

		// final DetailPrototype[] ds = arrayDets();
		final int l_ds = mv.newLocal( sub.classType() );
		mv.loadThis();
		section().compileCallToGetterFor( mv, sub );
		mv.storeLocal( l_ds );

		// final int dl = ds.length;
		final int l_dl = mv.newLocal( Type.INT_TYPE );
		mv.loadLocal( l_ds );
		mv.arrayLength();
		mv.storeLocal( l_dl );

		// if (dl > 0) {
		final Label noData = mv.newLabel();
		mv.loadLocal( l_dl );
		mv.ifZCmp( mv.LE, noData );

		// ~ long result = ds[ 0 ].detailValue();
		final int l_result = mv.newLocal( subExpr.typeCompiler().type() );
		mv.loadLocal( l_ds );
		mv.push( 0 );
		mv.arrayLoad( sub.classType() );
		mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, sub.classInternalName(), subExpr.methodName(), subExpr
				.methodDescriptor() );
		mv.storeLocal( l_result );

		// ~ for (int i = 1; i < dl; i++) {
		final Label next = mv.newLabel();
		final int l_i = mv.newLocal( Type.INT_TYPE );
		mv.push( 1 );
		mv.storeLocal( l_i );
		mv.goTo( next );
		final Label again = mv.mark();

		// ~ ~ result += ds[ i ].detailValue();
		mv.loadLocal( l_result );
		mv.loadLocal( l_ds );
		mv.loadLocal( l_i );
		mv.arrayLoad( sub.classType() );
		mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, sub.classInternalName(), subExpr.methodName(), subExpr
				.methodDescriptor() );
		expressionCompiler().compileOperatorWithFirstArgOnStack( this.reductor, 2 );
		mv.storeLocal( l_result );

		// ~ }
		mv.iinc( l_i, 1 );
		mv.mark( next );
		mv.loadLocal( l_i );
		mv.loadLocal( l_dl );
		mv.ifCmp( Type.INT_TYPE, mv.LT, again );

		// ~ return result;
		mv.loadLocal( l_result );
		mv.visitInsn( typeCompiler().returnOpcode() );

		// } else {
		mv.mark( noData );
		expressionCompiler().compileZero();
	}


	private ValueMethodCompiler compileSubExpr( List<ExpressionNode> _args ) throws CompilerException
	{
		switch (_args.size()) {
			case 0:
				throw new CompilerException.UnsupportedExpression( "Aggregation across subsection must have at least one value to be aggregated." );
			case 1:
				return this.sub.compileMethodForExpression( _args.get( 0 ) );
			default:
				final ExpressionNodeForOperator combiner = new ExpressionNodeForOperator( this.reductor );
				combiner.setDataType( dataType() );
				combiner.arguments().addAll( _args );
				return this.sub.compileMethodForExpression( combiner );
		}
	}


}
