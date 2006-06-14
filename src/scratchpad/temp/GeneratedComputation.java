package temp;

import sej.runtime.Computation;

public final class GeneratedComputation implements Computation, MyComputation
{
	private final MyInputs inputs;

	public GeneratedComputation(MyInputs _inputs)
	{
		this.inputs = _inputs;
	}

	public double getResult()
	{
		return this.inputs.getOne() + this.inputs.getTwo();
	}

}
