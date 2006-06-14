package sej.api;


/**
 * Defines the bindings of spreadsheet cells and sections to Java elements.
 * 
 * @author peo
 */
public interface SpreadsheetBinder
{


	/**
	 * Parameters to a new instance of a spreadsheet binder.
	 * 
	 * @author peo
	 */
	public static class Config
	{

		/**
		 * The spreadsheet whose elements you want to bind.
		 */
		public Spreadsheet spreadsheet;

		/**
		 * The class of the input type to whose methods you want to bind input elements.
		 */
		public Class inputClass;

		/**
		 * The class of the output type whose methods you want to bind to output elements.
		 */
		public Class outputClass;

		/**
		 * Validates the configuration for missing or improperly set values.
		 * 
		 * @throws IllegalArgumentException
		 */
		public void validate()
		{
			if (this.spreadsheet == null) throw new IllegalArgumentException( "spreadsheet is null" );
			if (this.inputClass == null) throw new IllegalArgumentException( "inputClass is null" );
			if (this.outputClass == null) throw new IllegalArgumentException( "outputClass is null" );

			Util.validateIsAccessible( this.inputClass, "inputClass" );
			Util.validateIsImplementable( this.outputClass, "outputClass" );
		}
	}


	/**
	 * Returns the spreadsheet model of which elements are bound.
	 * 
	 * @return The spreadsheet model.
	 */
	public Spreadsheet getSpreadsheet();


	/**
	 * Finalizes and returns the binding built by this class.
	 * 
	 * @return The finalized binding.
	 * @throws CompilerError 
	 */
	public SpreadsheetBinding getBinding() throws CompilerError;


	/**
	 * Returns the root section, which represents the entire worksheet.
	 * 
	 * @return The root section. Is never <code>null</code>.
	 */
	public Section getRoot();


	/**
	 * Represents both the container for the definitions of global spreadsheet cells and instances of
	 * horizontal or vertical <a href="{@docRoot}/../tutorial/binding.htm#BindRepeatingSections">sections</a>
	 * within a spreadsheet.
	 * 
	 * @author peo
	 */
	public static interface Section
	{

		/**
		 * The class of the input type to whose methods you are binding input elements.
		 */
		public Class getInputClass();

		/**
		 * The class of the output type whose methods you are binding to output elements.
		 */
		public Class getOutputClass();


		/**
		 * Marks a given spreadsheet cell as a variable input to the constructed engine and binds it
		 * to the given method call chain.
		 * 
		 * @param _cell is a spreadsheet cell whose value, rather than being assumed constant, will be
		 *           defined later, when a specific engine computation is run. The cell is thus like a
		 *           parameter to a Java method.
		 * @param _callChainToCall will be called during computations to obtain the value of the input
		 *           cell. The type of the cell is inferred from the return type of the last method in
		 *           the chain. The head of the chain must be callable on the input class of this name
		 *           space.
		 * @throws CompilerError
		 * 
		 * @see #defineOutputCell(Spreadsheet.Cell, CallFrame)
		 * @see Spreadsheet#getCell(int, int, int)
		 * @see Spreadsheet#getCell(String)
		 */
		public void defineInputCell( Spreadsheet.Cell _cell, CallFrame _callChainToCall ) throws CompilerError;


		/**
		 * Marks a given spreadsheet cell as a computable output of the constructed engine and binds
		 * it to the given call. The call will be implemented by the constructed engine to compute the
		 * formula in the designated cell.
		 * 
		 * @param _cell is a spreadsheet cell whose valuewill be computed later, when a specific
		 *           engine computation is run. The cell is thus like a return value of a Java method.
		 * @param _callToImplement is what you call on a computation to obtain the actual value of the
		 *           output cell in a specific computation. The type of the cell is inferred from the
		 *           return type of the call. The call may not be a chain. The call must be applicable
		 *           to the input class of this section. If the call is parametrized, the compiler
		 *           generates internal lookup code that maps the given argument values to the given
		 *           output cell. Other argument values can be bound to other cells. Unbound values
		 *           will call the inherited implementation (which must not be abstract).
		 * 
		 * @see #defineInputCell(Spreadsheet.Cell, CallFrame)
		 * @see Spreadsheet#getCell(int, int, int)
		 * @see Spreadsheet#getCell(String)
		 */
		public void defineOutputCell( Spreadsheet.Cell _cell, CallFrame _callToImplement ) throws CompilerError;


		/**
		 * Defines a range in the spreadsheet as a section of similar, repeating rows (or columns). At
		 * computation time, the section's effective height (or width) is determined by calling an
		 * iterator on your input interface.
		 * 
		 * <p>
		 * The compiler uses the first row (or column) of the range as a template for all other rows
		 * (or columns). It does not currently check that all rows (or columns) are in fact similar in
		 * structure in the model spreadsheet. The compiler does not support auto-extending constant
		 * series, for example the series 1, 2, 3 ... n.
		 * 
		 * @param _range is the range whose rows (or columns) should be repeated.
		 * @param _orientation indicates the orientation of the variable extent. Horizontal means
		 *           repeating rows, vertical means repeating rows.
		 * @param _inputCallChainReturningIterable gets called on a computation's input to return an
		 *           iterable for the elements that the repeating section should effectively have.
		 * @param _inputClass is the input type of the returned section.
		 * @param _outputCallToImplementIterable is implemented on the output class to iterate the
		 *           elements of the repeating section and compute output values for them.
		 * @param _outputClass is the output type of the returned section.
		 * @return A new section for the repeated rows (or columns). The input and output interfaces
		 *         are automatically inferred from the arguments above.
		 * 
		 * @see Spreadsheet#getRange(String)
		 */
		public Section defineRepeatingSection( Spreadsheet.Range _range, Orientation _orientation,
				CallFrame _inputCallChainReturningIterable, Class _inputClass, CallFrame _outputCallToImplementIterable,
				Class _outputClass ) throws CompilerError;


	}

}
