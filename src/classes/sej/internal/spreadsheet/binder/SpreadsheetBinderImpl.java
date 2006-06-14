package sej.internal.spreadsheet.binder;

import sej.CallFrame;
import sej.CompilerError;
import sej.Orientation;
import sej.Spreadsheet;
import sej.SpreadsheetBinder;
import sej.SpreadsheetBinding;
import sej.Spreadsheet.Cell;
import sej.Spreadsheet.Range;
import sej.internal.spreadsheet.SpreadsheetImpl;
import sej.internal.spreadsheet.binding.SectionBinding;
import sej.internal.spreadsheet.binding.WorkbookBinding;

public final class SpreadsheetBinderImpl implements SpreadsheetBinder
{
	private final Spreadsheet spreadsheet;
	private final WorkbookBinding binding;
	private final SectionBinderImpl root;

	public SpreadsheetBinderImpl(Config _config)
	{
		super();
		_config.validate();
		this.spreadsheet = _config.spreadsheet;
		this.binding = new WorkbookBinding( (SpreadsheetImpl) _config.spreadsheet, _config.inputClass, _config.outputClass );
		this.root = new SectionBinderImpl( this.binding.getRoot() );
	}


	public Spreadsheet getSpreadsheet()
	{
		return this.spreadsheet;
	}


	public SpreadsheetBinding getBinding() throws CompilerError
	{
		this.binding.validate();
		return this.binding;
	}


	// ------------------------------------------------ Operations


	public Section getRoot()
	{
		return this.root;
	}


	private class SectionBinderImpl implements SpreadsheetBinder.Section
	{
		private final SectionBinding sectionBinding;

		public SectionBinderImpl(SectionBinding _binding)
		{
			super();
			this.sectionBinding = _binding;
		}

		public void defineInputCell( Cell _cell, CallFrame _callChainToCall ) throws CompilerError
		{
			this.sectionBinding.defineInputCell( _cell, _callChainToCall );
		}

		public void defineOutputCell( Cell _cell, CallFrame _call ) throws CompilerError
		{
			this.sectionBinding.defineOutputCell( _cell, _call );
		}

		public SpreadsheetBinder.Section defineRepeatingSection( Range _range, Orientation _orientation,
				CallFrame _inputCallChainReturningIterable, Class _inputClass, CallFrame _outputCallToImplementIterable,
				Class _outputClass ) throws CompilerError
		{
			return new SectionBinderImpl( this.sectionBinding.defineRepeatingSection( _range, _orientation,
					_inputCallChainReturningIterable, _inputClass, _outputCallToImplementIterable, _outputClass ) );
		}

		public Class getInputClass()
		{
			return this.sectionBinding.getInputClass();
		}

		public Class getOutputClass()
		{
			return this.sectionBinding.getOutputClass();
		}

	}


}
