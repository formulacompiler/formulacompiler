package temp;

import java.util.Iterator;

import sej.runtime.Computation;

public final class GeneratedComputation implements Computation, MyComputation
{
	private final MyInputs inputs;
	private GeneratedDetails[] details;
	private boolean detailsInitialized;

	public GeneratedComputation(MyInputs _inputs)
	{
		this.inputs = _inputs;
	}

	public double getResult()
	{
		return sumArray() + sumIterable() + sumIterator();
	}


	private double sumArray()
	{
		double result = 0.0;
		final MyDetails[] arr = this.inputs.getArray();
		final int len = arr.length;
		for (int i = 0; i < len; i++) {
			final MyDetails d = arr[i];
			result += d.getValue();
		}
		return result;
	}

	private double sumIterable()
	{
		double result = 0.0;
		for (MyDetails d : this.inputs.getIterable()) {
			result += d.getValue();
		}
		return result;
	}

	private double sumIterator()
	{
		double result = 0.0;
		final Iterator<MyDetails> it = this.inputs.getIterator();
		while (it.hasNext()) {
			final MyDetails d = it.next();
			result += d.getValue();
		}
		return result;
	}

}
