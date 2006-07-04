package sej.internal.bytecode.compiler;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.CallFrame;
import sej.CompilerException;

public class ByteCodeSubSectionGetterCompiler extends ByteCodeSectionMethodCompiler
{
	private final ByteCodeSubSectionCompiler sub;


	ByteCodeSubSectionGetterCompiler(ByteCodeSectionCompiler _section, ByteCodeSubSectionCompiler _sub)
	{
		super( _section, Opcodes.ACC_PRIVATE, _sub.getterName(), _sub.getterDescriptor() );
		this.sub = _sub;
	}


	@Override
	protected void compileBody() throws CompilerException
	{
		final ByteCodeSubSectionCompiler sub = this.sub;
		final GeneratorAdapter mv = mv();

		// if (this.field == null) {
		final Label alreadySet = mv.newLabel();
		mv.loadThis();
		mv.getField( section().classType(), sub.getterName(), sub.arrayType() );
		mv.ifNonNull( alreadySet );

		// ~ final DetailInput[] ds = this.inputs.getarray();
		final CallFrame inputCall = sub.model().getCallChainToCall();
		final Type inputArrayType = Type.getType( inputCall.getMethod().getReturnType() );
		final int l_ds = mv.newLocal( inputArrayType );
		compileInputGetterCall( inputCall );
		mv.storeLocal( l_ds );

		// ~ if (ds != null) {
		final Label isNull = mv.newLabel();
		mv.loadLocal( l_ds );
		mv.ifNull( isNull );
		
		// ~ ~ final int dl = ds.length;
		final int l_dl = mv.newLocal( Type.INT_TYPE );
		mv.loadLocal( l_ds );
		mv.arrayLength();
		mv.storeLocal( l_dl );
		
		//	~ ~ DetailImpl[] di = new DetailPrototype[ dl ];
		final int l_di = mv.newLocal( sub.arrayType() );
		mv.loadLocal( l_dl );
		mv.newArray( sub.classType() );
		mv.storeLocal( l_di );
		
		//	~ ~ for (int i = 0; i < dl; i++) {
		final int l_i = mv.newLocal( Type.INT_TYPE );
		final Label next = mv.newLabel();
		mv.push( 0 );
		mv.storeLocal( l_i );
		mv.goTo( next );
		final Label again = mv.mark();
		
		//	~ ~ ~ di[ i ] = new DetailPrototype( ds[ i ] );
		mv.loadLocal( l_di );
		mv.loadLocal( l_i );
		mv.newInstance( sub.classType() );
		mv.dup();
		mv.loadLocal( l_ds );
		mv.loadLocal( l_i );
		mv.arrayLoad( sub.inputType() );
		mv.visitMethodInsn( Opcodes.INVOKESPECIAL, sub.classInternalName(), "<init>", "(" + sub.inputType().getDescriptor() + ")V" );
		mv.arrayStore( sub.classType() );
		
		//	~ ~ }
		mv.iinc( l_i, 1 );
		mv.mark( next );
		mv.loadLocal( l_i );
		mv.loadLocal( l_dl );
		mv.ifCmp( Type.INT_TYPE, mv.LT, again );
		
		//	~ ~ this.<field> = di;
		mv.loadThis();
		mv.loadLocal( l_di );
		mv.putField( section().classType(), sub.getterName(), sub.arrayType() );
		
		// ~ } else {
		mv.goTo( alreadySet );
		mv.mark( isNull );
		mv.loadThis();
		mv.push( 0 );
		mv.newArray( sub.classType() );
		mv.putField( section().classType(), sub.getterName(), sub.arrayType() );

		// ~ }
		// }
		mv.mark( alreadySet );

		// return this.field;
		mv.loadThis();
		mv.getField( section().classType(), sub.getterName(), sub.arrayType() );
		mv.visitInsn( Opcodes.ARETURN );
	}

}
