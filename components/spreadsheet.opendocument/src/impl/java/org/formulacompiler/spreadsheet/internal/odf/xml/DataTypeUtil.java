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

package org.formulacompiler.spreadsheet.internal.odf.xml;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * @author Vladimir Korenev
 */
public class DataTypeUtil
{

	public static final TimeZone GMT_TIME_ZONE = TimeZone.getTimeZone( "GMT" );

	public static long durationFromXmlFormat( final String _xmlFormat )
	{
		try {
			final DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
			final Duration duration = datatypeFactory.newDuration( _xmlFormat );
			final Calendar calendar = new GregorianCalendar( GMT_TIME_ZONE );
			final long durationInMillis = duration.getTimeInMillis( calendar );
			return durationInMillis;
		} catch (DatatypeConfigurationException e) {
			throw new ConfigurationException( e );
		}
	}

	public static Date dateFromXmlFormat( final String _xmlFormat, final TimeZone _timeZone )
	{
		try {
			final DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
			final XMLGregorianCalendar gregorianCalendar = datatypeFactory.newXMLGregorianCalendar( _xmlFormat );
			final Calendar calendar = gregorianCalendar.toGregorianCalendar( _timeZone, null, null );
			return calendar.getTime();
		} catch (DatatypeConfigurationException e) {
			throw new ConfigurationException( e );
		}
	}

	/**
	 * Exception thrown when configuration errors occur.
	 */
	public static class ConfigurationException extends RuntimeException
	{
		private ConfigurationException( Throwable _throwable )
		{
			super( _throwable );
		}
	}


}
