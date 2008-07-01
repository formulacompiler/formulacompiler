/*
 * Copyright (c) 2006, 2008 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * This file is part of the Abacus Formula Compiler (AFC).
 *
 * For commercial licensing, please contact sales(at)formulacompiler.com.
 *
 * AFC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AFC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AFC.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.formulacompiler.tests.reference.base;

import org.formulacompiler.runtime.Computation;

import junit.extensions.ActiveTestSuite;
import junit.framework.TestSuite;

abstract class AbstractSuiteSetup
{

	protected static Context newXlsSheetContext( String _fileBaseName )
	{
		final Context cx = new Context( _fileBaseName, ".xls" );
		if (odsSpreadsheetExists( _fileBaseName ) && !_fileBaseName.equals( "ErrorCells" )) {
			final Context variant = new Context( _fileBaseName, ".ods" );
			variant.setRowVerificationTestCaseFactory( ODSRowVerificationTestCase.Factory.INSTANCE );
			cx.addVariant( variant );
		}

		final RowSetup.Builder setup;
		if (_fileBaseName.contains( "Database" )) {
			setup = new RowSetupDbAgg.Builder();
		}
		else {
			setup = new RowSetupDefault.Builder();
		}
		cx.setRowSetupBuilder( setup );

		return cx;
	}

	protected static Context newOdsSheetContext( String _fileBaseName )
	{
		final Context cx = new Context( _fileBaseName, ".ods" );

		final RowSetup.Builder setup;
		if (_fileBaseName.contains( "Database" )) {
			setup = new RowSetupDbAgg.Builder();
		}
		else {
			setup = new RowSetupDefault.Builder();
		}
		cx.setRowSetupBuilder( setup );

		return cx;
	}

	public static boolean odsSpreadsheetExists( String _fileBaseName )
	{
		return !_fileBaseName.endsWith( "_CustomDecimalSymbols" );
	}

	protected static TestSuite newLoader( Context _cx )
	{
		final TestSuite loader = new SheetLoadingTestSuite( _cx ).init();
		final int checkingCol = _cx.getRowSetup().checkingCol();
		if (checkingCol >= 0) {
			loader.addTest( new SheetCheckingColumnsVerificationTestCase( _cx, checkingCol ) );
		}
		return loader;
	}


	protected static abstract class AbstractSetup
	{
		private final AbstractSetup innerSetup;

		protected AbstractSetup( AbstractSetup _innerSetup )
		{
			this.innerSetup = _innerSetup;
		}

		protected AbstractSetup()
		{
			this( null );
		}

		protected abstract void setup( TestSuite _parent, Context _parentCx ) throws Exception;

		protected void addTest( TestSuite _parent, TestSuite _child, Context _childCx ) throws Exception
		{
			_parent.addTest( _child );
			if (null != this.innerSetup) {
				this.innerSetup.setup( _child, _childCx );
			}
		}

	}

	protected static final class ThreadedSetup extends AbstractSetup
	{

		public ThreadedSetup( AbstractSetup _innerSetup )
		{
			super( _innerSetup );
		}

		@Override
		protected void setup( TestSuite _parent, Context _parentCx ) throws Exception
		{
			TestSuite child = new ActiveTestSuite( "Parallelized" );
			addTest( _parent, child, _parentCx );
		}

	}

	protected static final class ComputationConfigSetup extends AbstractSetup
	{
		private final Computation.Config config;

		public ComputationConfigSetup( Computation.Config _config, AbstractSetup _innerSetup )
		{
			super( _innerSetup );
			this.config = _config;
		}

		@Override
		protected void setup( TestSuite _parent, Context _parentCx ) throws Exception
		{
			Context cx = new Context( _parentCx );
			cx.setComputationConfig( this.config );
			addTest( _parent, new PassthroughContextTestSuite( cx )
			{
				@Override
				protected String getOwnName()
				{
					return cx().getComputationConfig().toString();
				}
			}.init(), cx );
		}

	}

	protected static final class AllNumberTypesSetup extends AbstractSetup
	{
		private final BindingType emitDocsFor;

		public AllNumberTypesSetup( AbstractSetup _innerSetup )
		{
			this( _innerSetup, null );
		}

		public AllNumberTypesSetup( AbstractSetup _innerSetup, BindingType _emitDocsFor )
		{
			super( _innerSetup );
			this.emitDocsFor = _emitDocsFor;
		}

		@Override
		protected void setup( TestSuite _parent, Context _parentCx ) throws Exception
		{
			final boolean canDocument = !_parentCx.getExplicitCaching();
			for (BindingType type : BindingType.numbers()) {
				Context cx = new Context( _parentCx );
				cx.setNumberBindingType( type );

				final TestSuite child;
				if (canDocument && type == this.emitDocsFor) {
					child = new SheetDocumentingTestSuite( cx, new HtmlDocumenter() )
					{
						@Override
						protected String getOwnName()
						{
							return cx().getNumberBindingType().name() + " [documenting]";
						}
					}.init();
				}
				else {
					child = new PassthroughContextTestSuite( cx )
					{
						@Override
						protected String getOwnName()
						{
							return cx().getNumberBindingType().name();
						}
					}.init();
				}
				addTest( _parent, child, cx );
			}
		}

	}

	protected static final class AllCachingVariantsSetup extends AbstractSetup
	{

		public AllCachingVariantsSetup( AbstractSetup _innerSetup )
		{
			super( _innerSetup );
		}

		@Override
		protected void setup( TestSuite _parent, Context _parentCx ) throws Exception
		{
			for (boolean caching : new boolean[] { false, true }) {
				Context cx = new Context( _parentCx );
				cx.setExplicitCaching( caching );
				addTest( _parent, new PassthroughContextTestSuite( cx )
				{
					@Override
					protected String getOwnName()
					{
						return cx().getExplicitCaching() ? "With caching" : "No caching";
					}
				}.init(), cx );
			}
		}

	}

}
