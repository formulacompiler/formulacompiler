package temp;

import sej.Computation;
import sej.ComputationFactory;

public class GeneratedFactory implements ComputationFactory, MyFactory
{

	public Computation newInstance( Object _inputs )
	{
		return new GeneratedComputation( (MyInputs) _inputs );
	}

	public MyComputation newInstance( MyInputs _inputs )
	{
		return new GeneratedComputation( _inputs );
	}

}
