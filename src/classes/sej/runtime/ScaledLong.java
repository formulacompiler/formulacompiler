package sej.runtime;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates to SEJ that a {@code long} value is scaled.
 * 
 * <p>
 * See the <a href="../../tutorial/numeric_type.htm#long">tutorial</a> for details.
 * 
 * @author peo
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ScaledLong {

	/**
	 * Indicates the scale used in the number.
	 */
	int value();


	/**
	 * The number 1 for the scaled {@code long} type at the different supported scales. Use it to
	 * scale unscaled values by multiplying them with the appropriate {@code ONE}.
	 */
	public static final long[] ONE = new long[] { 1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000,
			1000000000, 10000000000L, 100000000000L, 1000000000000L, 10000000000000L, 100000000000000L, 1000000000000000L,
			10000000000000000L, 100000000000000000L, 1000000000000000000L };

}
