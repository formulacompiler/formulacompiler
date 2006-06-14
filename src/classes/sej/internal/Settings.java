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
package sej.internal;

/**
 * Holds global settings for SEJ.
 * 
 * @author peo
 */
public class Settings
{
	private static boolean debugLogEnabled = false;
	private static String debugIndentation = "";
	private static boolean debugParserEnabled = false;
	private static boolean debugCompilationEnabled = false;


	/**
	 * Returns whether the logging of debug information to the console is enabled.
	 */
	public static boolean isDebugLogEnabled()
	{
		return debugLogEnabled;
	}


	/**
	 * Controls whether the logging of debug information to the console is enabled.
	 */
	public static void setDebugLogEnabled( boolean _debugMode )
	{
		debugLogEnabled = _debugMode;
	}


	public static String getDebugIndentation()
	{
		return debugIndentation;
	}


	public static void printDebugIndentation()
	{
		System.out.print( debugIndentation );
	}


	public static void debugIndent()
	{
		debugIndentation += "  ";
	}


	public static void debugOutdent()
	{
		debugIndentation = debugIndentation.substring( 2 );
	}


	public static boolean isDebugParserEnabled()
	{
		return debugParserEnabled;
	}


	public static void setDebugParserEnabled( boolean _debugParserEnabled )
	{
		debugParserEnabled = _debugParserEnabled;
	}


	public static boolean isDebugCompilationEnabled()
	{
		return debugCompilationEnabled ;
	}


	public static void setDebugCompilationEnabled( boolean _debugCompilationEnabled )
	{
		debugCompilationEnabled = _debugCompilationEnabled;
	}

}
