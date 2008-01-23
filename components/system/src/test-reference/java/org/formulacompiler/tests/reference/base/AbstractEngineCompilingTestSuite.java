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

import java.lang.reflect.Method;
import java.util.Map;

import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.runtime.ComputationFactory;
import org.formulacompiler.runtime.New;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.spreadsheet.internal.CellIndex;

public abstract class AbstractEngineCompilingTestSuite extends AbstractContextTestSuite
{

	public AbstractEngineCompilingTestSuite( Context _cx )
	{
		super( _cx );
	}


	@Override
	protected void setUp() throws Throwable
	{
		final EngineBuilder eb = SpreadsheetCompiler.newEngineBuilder();
		eb.setSpreadsheet( cx().getSpreadsheet() );
		eb.setInputClass( Inputs.class );
		eb.setOutputClass( Outputs.class );

		eb.setFullCaching( cx().getExplicitCaching() );
		eb.setNumericType( cx().getNumericType() );
		eb.setCompileTimeConfig( cx().getComputationConfig() );

		eb.getRootBinder().defineOutputCell( cx().getOutputCell(),
				getterFor( Outputs.class, cx().getExpected().type( 0 ) ) );
		final CellIndex[] ins = cx().getInputCells();
		final boolean[] flags = cx().getInputBindingFlags();
		final Inputs in = cx().getInputs();
		for (int i = 0; i < ins.length; i++) {
			if (flags[ i ]) {
				eb.getRootBinder().defineInputCell( ins[ i ], getterFor( Inputs.class, in.type( i ), i ) );
			}
		}

		final SaveableEngine engine = eb.compile();
		final ComputationFactory factory;
		try {
			factory = engine.getComputationFactory( cx().getComputationConfig() );
		}
		catch (Throwable t) {
			cx().reportFailedEngineAndRethrow( this, engine, t );
			throw t; // to make factory accessible outside
		}

		cx().setEngine( engine );
		cx().setFactory( factory );
	}


	@Override
	protected void tearDown() throws Exception
	{
		cx().releaseEngine();
	}


	private static final Map<Class, Map<BindingType, Method>> getters = New.map();
	private static final String[] getterNames = new String[] { "dbl", "bdec", "bdec", "lng", "bool", "date", "str", null };

	public static CallFrame getterFor( Class _cls, BindingType _type, int _index )
	{
		final Map<BindingType, Method> getters = gettersFor( _cls, true );
		return SpreadsheetCompiler.newCallFrame( getters.get( _type ), _index );
	}

	public static CallFrame getterFor( Class _cls, BindingType _type )
	{
		final Map<BindingType, Method> getters = gettersFor( _cls, false );
		return SpreadsheetCompiler.newCallFrame( getters.get( _type ) );
	}

	private static Map<BindingType, Method> gettersFor( Class _cls, boolean _indexed )
	{
		Map<BindingType, Method> result = getters.get( _cls );
		if (null == result) {
			result = New.map();
			final Class[] getterParams = _indexed ? new Class[] { Integer.TYPE } : null;
			for (BindingType t : BindingType.values()) {
				final String name = getterNames[ t.ordinal() ];
				if (null != name) {
					try {
						result.put( t, _cls.getMethod( name, getterParams ) );
					}
					catch (Exception e) {
						throw new IllegalArgumentException( e );
					}
				}
			}
			getters.put( _cls, result );
		}
		return result;
	}


}
