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
package org.formulacompiler.tests.reference;

import java.util.TimeZone;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.formulacompiler.runtime.Computation;


public class DateFunctionsWithPresetTimeZoneTest extends AbstractReferenceTest
{
	private static final TimeZone TIME_ZONE_1 = TimeZone.getTimeZone( "GMT+12" );
	private static final TimeZone TIME_ZONE_2 = TimeZone.getTimeZone( "GMT-12" );

	public DateFunctionsWithPresetTimeZoneTest()
	{
		super( "DateFunctions" );
		final TimeZone defaultTimeZone = TimeZone.getDefault();
		final TimeZone timeZone = getDayOfYear( defaultTimeZone ) != getDayOfYear( TIME_ZONE_1 ) ?
				TIME_ZONE_1 : TIME_ZONE_2;
		assertTrue( "The day must be different from local.", getDayOfYear( defaultTimeZone ) != getDayOfYear( timeZone ) );
		setConfig( new Computation.Config( timeZone ) );
	}

	private int getDayOfYear( final TimeZone _timeZone )
	{
		final Calendar calendar = new GregorianCalendar( _timeZone );
		return calendar.get( Calendar.DAY_OF_YEAR );
	}
}
