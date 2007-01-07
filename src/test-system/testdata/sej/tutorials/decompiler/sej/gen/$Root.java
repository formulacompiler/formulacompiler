package sej.gen;
import sej.internal.runtime.RuntimeDouble_v1;
import sej.runtime.Computation;
import sej.tutorials.Decompilation;

final class $Root implements Computation, Decompilation.Outputs
{
    private final Decompilation.Inputs $inputs;
    
    $Root(Decompilation.Inputs inputs) {
        $inputs = inputs;
    }
    
    final double get$0() {
        return RuntimeDouble_v1.max(get$1(), get$2());
    }
    
    public final double rebateOp() {
        return get$0();
    }
    
    final double get$1() {
        return $inputs.customerRebate();
    }
    
    final double get$2() {
        return $inputs.articleRebate();
    }
}