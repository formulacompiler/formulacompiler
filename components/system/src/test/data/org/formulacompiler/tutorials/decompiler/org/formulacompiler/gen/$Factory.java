package org.formulacompiler.gen;
import org.formulacompiler.runtime.Computation;
import org.formulacompiler.runtime.ComputationFactory;
import org.formulacompiler.tutorials.Decompilation;

public final class $Factory implements ComputationFactory
{
    public final Computation newComputation(Object object) {
        return new $Root((Decompilation.Inputs) object);
    }
}