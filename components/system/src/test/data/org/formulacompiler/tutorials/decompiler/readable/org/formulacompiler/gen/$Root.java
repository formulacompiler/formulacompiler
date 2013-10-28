package org.formulacompiler.gen;
import org.formulacompiler.runtime.Computation;
import org.formulacompiler.runtime.internal.Environment;
import org.formulacompiler.runtime.internal.RuntimeDouble_v2;
import org.formulacompiler.tutorials.Decompilation;

final class $Root implements Computation, Decompilation.MyOutputs
{
    private final Decompilation.MyInputs $inputs;
    final Environment $environment;
    
    final double get$Rebate() {
        return RuntimeDouble_v2.max(get$CustomerRebate(), get$ArticleRebate());
    }
    
    public final double rebate() {
        return get$Rebate();
    }
    
    final double get$CustomerRebate() {
        return $inputs.customerRebate();
    }
    
    final double get$ArticleRebate() {
        return $inputs.articleRebate();
    }
    
    $Root(Decompilation.MyInputs myinputs, Environment environment) {
        $environment = environment;
        $inputs = myinputs;
    }
}