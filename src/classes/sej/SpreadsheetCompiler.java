/*
 * Copyright © 2006 by Abacus Research AG, Switzerland.
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
package sej;

import java.lang.reflect.Method;

import sej.runtime.EngineError;


public interface SpreadsheetCompiler
{

	public static class Config
	{
		public SpreadsheetBinding binding;
		public NumericType numericType = NumericType.DEFAULT;
		public Class factoryClass = null;
		public Method factoryMethod = null;

		public void validate()
		{
			if (this.binding == null) throw new IllegalArgumentException( "binding is null" );
			if (this.numericType == null) throw new IllegalArgumentException( "numericType is null" );

			SEJ.validateFactory( this.factoryClass, this.factoryMethod, this.binding.getInputClass(), this.binding
					.getOutputClass() );
		}
	}

	public SaveableEngine compile() throws CompilerError, EngineError;

}
