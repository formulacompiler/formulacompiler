package org.formulacompiler.gen;
import org.formulacompiler.runtime.Computation;
import org.formulacompiler.runtime.internal.Environment;
import org.formulacompiler.runtime.internal.RuntimeDouble_v2;
import org.formulacompiler.tutorials.Decompilation;

final class $Root implements Computation, Decompilation.MyOutputs
{
    private final Decompilation.MyInputs $inputs;
    final Environment $environment;
    
    $Root(Decompilation.MyInputs myinputs, Environment environment) {
        $environment = environment;
        $inputs = myinputs;
    }
    
    final double get$REBATE() {
        return RuntimeDouble_v2.max(get$CUSTOMERREBATE(), get$ARTICLEREBATE());
    }
    
    public final double rebate() {
        return get$REBATE();
    }
    
    final double get$CUSTOMERREBATE() {
        return $inputs.customerRebate();
    }
    
    final double get$ARTICLEREBATE() {
        return $inputs.articleRebate();
    }
}