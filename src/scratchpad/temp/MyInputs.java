package temp;

import java.util.Collection;
import java.util.Iterator;

public interface MyInputs
{
	double getOne();
	double getTwo();
	
	MyDetails[] getArray();
	Iterable<MyDetails> getIterable();
	Iterator<MyDetails> getIterator();
	
}
