/*
 * Copyright (c) 2006 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are prohibited, unless you have been explicitly granted
 * more rights by Abacus Research AG.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.formulacompiler.tests.reference.base;

import org.formulacompiler.runtime.Computation;

import junit.extensions.ActiveTestSuite;
import junit.framework.TestSuite;

abstract class AbstractSuiteSetup
{

	protected static Context newSheetContext( String _fileBaseName )
	{
		final Context cx = new Context( _fileBaseName, ".xls" );
		if (_fileBaseName.contains( "Database" )) {
			cx.setRowSetupBuilder( new RowSetupDbAgg.Builder() );
		}
		else {
			cx.setRowSetupBuilder( new RowSetupDefault.Builder() );
		}

		return cx;
	}

	protected static TestSuite newLoader( Context _cx )
	{
		return new SheetLoadingTestSuite( _cx ).init();
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
			for (BindingType type : BindingType.numbers()) {
				Context cx = new Context( _parentCx );
				cx.setNumberBindingType( type );

				final TestSuite child;
				if (type == this.emitDocsFor) {
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
						return cx().getExplicitCaching()? "With caching" : "No caching";
					}
				}.init(), cx );
			}
		}

	}

}
