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
package org.formulacompiler.spreadsheet.internal.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;

import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.runtime.New;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBinder;
import org.formulacompiler.spreadsheet.SpreadsheetByNameBinder;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.Spreadsheet.Cell;
import org.formulacompiler.spreadsheet.Spreadsheet.CellNameDefinition;
import org.formulacompiler.spreadsheet.Spreadsheet.NameDefinition;


public class SpreadsheetByNameBinderImpl implements SpreadsheetByNameBinder
{
	private final SpreadsheetBinder binder;
	private final CellBinderImpl inputBinder;
	private final CellBinderImpl outputBinder;

	public SpreadsheetByNameBinderImpl( Config _config )
	{
		super();
		_config.validate();
		this.binder = _config.binder;
		this.inputBinder = new InputCellBinder( this.binder.getRoot().getInputClass() );
		this.outputBinder = new OutputCellBinder( this.binder.getRoot().getOutputClass() );
	}

	public static final class Factory implements SpreadsheetByNameBinder.Factory
	{
		public SpreadsheetByNameBinder newInstance( Config _config )
		{
			return new SpreadsheetByNameBinderImpl( _config );
		}
	}


	public CellBinder inputs()
	{
		return this.inputBinder;
	}

	public CellBinder outputs()
	{
		return this.outputBinder;
	}


	public Collection<CellNameDefinition> cellNamesLeftUnbound()
	{
		final NameDefinition[] defs = getBinder().getSpreadsheet().getDefinedNames();
		final Collection<CellNameDefinition> result = New.newCollection( defs.length );
		final SpreadsheetBinder binder = getBinder();
		for (NameDefinition def : defs) {
			if (def instanceof CellNameDefinition) {
				final CellNameDefinition cellDef = (CellNameDefinition) def;
				final Cell cell = cellDef.getCell();
				if (!binder.isInputCell( cell ) && !binder.isOutputCell( cell )) {
					result.add( cellDef );
				}
			}
		}
		return result;
	}

	public void failIfCellNamesAreStillUnbound() throws SpreadsheetException
	{
		final Collection<CellNameDefinition> unbound = cellNamesLeftUnbound();
		if (unbound.size() > 0) {
			final String name = unbound.iterator().next().getName();
			throw new SpreadsheetException.NameNotFound( "There is no input or output method named "
					+ name + "() or get" + name + "() to bind the cell " + name + " to (character case is irrelevant)." );
		}
	}


	private SpreadsheetBinder getBinder()
	{
		return this.binder;
	}

	private Spreadsheet getSpreadsheet()
	{
		return getBinder().getSpreadsheet();
	}


	private abstract class CellBinderImpl implements CellBinder
	{
		private final Method[] contextMethods;

		protected CellBinderImpl( Class _contextClass )
		{
			super();
			this.contextMethods = _contextClass.getMethods();
		}

		public void bindAllMethodsToNamedCells() throws CompilerException
		{
			bindAllMethodsToPrefixedNamedCells( "" );
		}

		public void bindAllMethodsToPrefixedNamedCells( String _prefix ) throws CompilerException
		{
			for (Method m : this.contextMethods) {
				if (m.getDeclaringClass() != Object.class) {
					if (canBind( m )) {
						bindThisMethodToNamedCell( m, _prefix );
					}
				}
			}
		}

		protected boolean canBind( Method _method )
		{
			return _method.getParameterTypes().length == 0;
		}

		protected boolean canBind( Spreadsheet.Cell _cell )
		{
			return true;
		}

		private void bindThisMethodToNamedCell( Method _m, String _cellNamePrefix ) throws CompilerException
		{
			final String fullCellName = (_cellNamePrefix + _m.getName()).toUpperCase();
			Cell cell = null;
			try {
				cell = getSpreadsheet().getCell( fullCellName );
			}
			catch (SpreadsheetException.NameNotFound e) {
				final String baseCellName = (_cellNamePrefix + getPlainCellNameFor( _m )).toUpperCase();
				try {
					cell = getSpreadsheet().getCell( baseCellName );
				}
				catch (SpreadsheetException.NameNotFound e2) {
					return;
				}
			}
			if (cell != null && canBind( cell )) {
				bindCell( cell, new CallFrame( _m ) );
			}
		}

		private String getPlainCellNameFor( Method _m )
		{
			String cellName = _m.getName();
			if (cellName.length() > 3 && cellName.startsWith( "get" ) && Character.isUpperCase( cellName.charAt( 3 ) )) {
				cellName = cellName.substring( 3 );
			}
			return cellName.toUpperCase();
		}


		public void bindAllNamedCellsToMethods() throws CompilerException
		{
			bindAllPrefixedNamedCellsToMethods( null );
		}

		public void bindAllPrefixedNamedCellsToMethods( String _prefix ) throws CompilerException
		{
			final String prefix = (null != _prefix)? _prefix.toUpperCase() : null;
			final NameDefinition[] defs = getSpreadsheet().getDefinedNames();
			for (NameDefinition def : defs) {
				if (def instanceof Spreadsheet.CellNameDefinition) {
					final Spreadsheet.CellNameDefinition cellDef = (Spreadsheet.CellNameDefinition) def;
					if (canBind( cellDef.getCell() )) {
						if (null == prefix) {
							bindThisNamedCellToMethod( cellDef );
						}
						else {
							final String defName = def.getName();
							if (defName.startsWith( prefix )) {
								final String strippedName = defName.substring( prefix.length() );
								bindThisNamedCellToMethod( cellDef, strippedName );
							}
						}
					}
				}
			}
		}

		private boolean bindThisNamedCellToMethod( CellNameDefinition _def ) throws CompilerException
		{
			return bindThisNamedCellToMethod( _def, _def.getName() );
		}

		private boolean bindThisNamedCellToMethod( CellNameDefinition _def, String _methodBaseName )
				throws CompilerException
		{
			return bindThisNamedCellToMethod( _methodBaseName, _def )
					|| bindThisNamedCellToMethod( "get" + _methodBaseName, _def );
		}

		private boolean bindThisNamedCellToMethod( String _methodName, CellNameDefinition _def ) throws CompilerException
		{
			for (Method m : this.contextMethods) {
				if (m.getName().equalsIgnoreCase( _methodName ) && canBind( m )) {
					bindCell( _def.getCell(), new CallFrame( m ) );
					return true;
				}
			}
			return false;
		}


		protected abstract void bindCell( Cell _cell, CallFrame _frame ) throws CompilerException;


		public void failIfCellNamesAreStillUnbound( String _prefix ) throws CompilerException
		{
			final NameDefinition[] defs = getBinder().getSpreadsheet().getDefinedNames();
			for (NameDefinition def : defs) {
				if (def instanceof CellNameDefinition) {
					final CellNameDefinition cellDef = (CellNameDefinition) def;
					final String name = cellDef.getName();
					if (name.startsWith( _prefix )) {
						if (!isBound( cellDef.getCell() )) {
							throw new SpreadsheetException.NameNotFound( "There is no "
									+ bindingTypeName() + " method named " + name + "() or get" + name + "() to bind the cell "
									+ name + " to (character case is irrelevant)." );
						}
					}
				}
			}
		}

		protected abstract boolean isBound( Cell _cell );
		protected abstract String bindingTypeName();

	}


	private class InputCellBinder extends CellBinderImpl
	{

		protected InputCellBinder( Class _contextClass )
		{
			super( _contextClass );
		}

		@Override
		protected void bindCell( Cell _cell, CallFrame _chain ) throws CompilerException
		{
			getBinder().getRoot().defineInputCell( _cell, _chain );
		}

		@Override
		protected boolean canBind( Cell _cell )
		{
			return !getBinder().isInputCell( _cell );
		}
		
		@Override
		protected boolean isBound( Cell _cell )
		{
			return getBinder().isInputCell( _cell );
		}
		
		@Override
		protected String bindingTypeName()
		{
			return "input";
		}

	}


	private class OutputCellBinder extends CellBinderImpl
	{

		protected OutputCellBinder( Class _contextClass )
		{
			super( _contextClass );
		}

		@Override
		protected void bindCell( Cell _cell, CallFrame _chain ) throws CompilerException
		{
			getBinder().getRoot().defineOutputCell( _cell, _chain );
		}

		@Override
		protected boolean canBind( Method _method )
		{
			return super.canBind( _method )
					&& !Modifier.isFinal( _method.getModifiers() ) && !Modifier.isStatic( _method.getModifiers() )
					&& !_method.getName().equals( "reset" );
		}

		@Override
		protected boolean isBound( Cell _cell )
		{
			return getBinder().isOutputCell( _cell );
		}
		
		@Override
		protected String bindingTypeName()
		{
			return "output";
		}

	}

}
