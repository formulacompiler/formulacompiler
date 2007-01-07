package sej.gen;
import sej.runtime.Computation;
import sej.runtime.ComputationFactory;
import sej.tutorials.Decompilation;

public final class $Factory implements ComputationFactory
{
    public final Computation newComputation(Object object) {
        return new $Root((Decompilation.Inputs) object);
    }
}