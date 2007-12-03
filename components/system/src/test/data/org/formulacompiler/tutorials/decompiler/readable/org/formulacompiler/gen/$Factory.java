package org.formulacompiler.gen;
import org.formulacompiler.runtime.Computation;
import org.formulacompiler.runtime.ComputationFactory;
import org.formulacompiler.runtime.internal.Environment;
import org.formulacompiler.tutorials.Decompilation;

public final class $Factory
    implements ComputationFactory, Decompilation.MyFactory
{
    private final Environment $environment;
    
    public $Factory(Environment environment) {
        $environment = environment;
    }
    
    public final Computation newComputation(Object object) {
        return new $Root((Decompilation.MyInputs) object, $environment);
    }
    
    public final Decompilation.MyOutputs newOutputs
        (Decompilation.MyInputs myinputs) {
        return new $Root(myinputs, $environment);
    }
}