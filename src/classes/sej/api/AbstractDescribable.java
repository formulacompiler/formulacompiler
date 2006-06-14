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
package sej.api;

import java.io.IOException;


/**
 * Default base implementation of {@link Describable}.
 * 
 * @author peo
 */
public abstract class AbstractDescribable implements Describable
{


	public final String describe()
	{
		DescriptionBuilder description = new DescriptionBuilder();
		try {
			describeTo( description );
		}
		catch (IOException e) {
			return e.getMessage();
		}
		return description.toString();
	}


	public final void describeTo( StringBuilder _to )
	{
		DescriptionBuilder description = new DescriptionBuilder();
		try {
			describeTo( description );
			_to.append( description.toString() );
		}
		catch (IOException e) {
			_to.append( e.getMessage() );
		}
	}


	public abstract void describeTo( DescriptionBuilder _to ) throws IOException;


	@Override
	public String toString()
	{
		return describe();
	}


}
