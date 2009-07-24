/*
 * Copyright (c) 2006-2009 by Abacus Research AG, Switzerland.
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
