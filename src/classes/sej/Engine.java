package sej;

import sej.internal.EngineBuilderImpl;
import sej.internal.EngineLoader;

/**
 * API to a computation engine. You normally use a {@link EngineBuilderImpl} to build one from a given
 * spreadsheet file, or else instantiate one constructed earlier from persistent storage using an
 * {@link EngineLoader}. An engine can return a computation factory, from which you instantiate
 * individual computations, and it can be saved to and loaded from persistent storage without
 * requiring access to the original spreadsheet file.
 * 
 * @author peo
 * @see EngineBuilderImpl
 * @see #getComputationFactory()
 */
public interface Engine
{

	/**
	 * Returns the factory for computations implemented by this engine.
	 * 
	 * @return The generated factory. Besides SEJ's own factory interface, the returned factory also
	 *         implements your own factory interface, if you specified one (which is recommended). So
	 *         you can simply cast the returned factory to your own interface.
	 */
	public ComputationFactory getComputationFactory();

}
