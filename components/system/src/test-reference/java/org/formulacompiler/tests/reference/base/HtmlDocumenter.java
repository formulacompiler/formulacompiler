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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.internal.DescriptionBuilder;
import org.formulacompiler.compiler.internal.IOUtil;
import org.formulacompiler.decompiler.ByteCodeEngineSource;
import org.formulacompiler.decompiler.FormulaDecompiler;
import org.formulacompiler.spreadsheet.internal.CellInstance;
import org.formulacompiler.spreadsheet.internal.CellWithLazilyParsedExpression;


public class HtmlDocumenter implements Documenter
{
	private FileDoc fileDoc;

	public void beginFile( String _fileName )
	{
		this.fileDoc = new FileDoc( _fileName );
	}

	public void endFile() throws Exception
	{
		if (null != this.fileDoc) this.fileDoc.close();
		this.fileDoc = null;
	}

	public void beginNamedSection( Context _cx )
	{
		this.fileDoc.beginNamedSection( _cx );
	}

	public void endNamedSection()
	{
		this.fileDoc.endNamedSection();
	}

	public void newEngineRow( Context _cx ) throws Exception
	{
		this.fileDoc.newEngineRow( _cx );
	}

	public void sameEngineRow( Context _cx )
	{
		this.fileDoc.sameEngineRow( _cx );
	}


	private static final class FileDoc implements Closeable
	{
		private static final File HTML_PATH = new File( "temp/test-reference/doc" );

		private final DescriptionBuilder html = new DescriptionBuilder();
		private final DescriptionBuilder terms = new DescriptionBuilder();
		private final String fileName;
		private SectionDoc sectionDoc;

		static {
			HTML_PATH.mkdirs();
		}

		public FileDoc( String _fileName )
		{
			this.fileName = _fileName;
		}

		public void close() throws IOException
		{
			assert null == this.sectionDoc;
			IOUtil.writeStringToIfNotUpToDate( this.html.toString(), new File( HTML_PATH, this.fileName() + ".htm" ) );
			IOUtil
					.writeStringToIfNotUpToDate( this.terms.toString(), new File( HTML_PATH, this.fileName() + "_terms.rb" ) );
		}

		public void beginNamedSection( Context _cx )
		{
			assert null == this.sectionDoc;
			this.sectionDoc = new SectionDoc( _cx );
		}

		public void endNamedSection()
		{
			this.sectionDoc.close();
			this.sectionDoc = null;
		}

		public void newEngineRow( Context _cx ) throws Exception
		{
			this.sectionDoc.newEngineRow( _cx );
		}

		public void sameEngineRow( Context _cx )
		{
			this.sectionDoc.sameEngineRow( _cx );
		}

		protected String fileName()
		{
			return this.fileName;
		}

		protected DescriptionBuilder html()
		{
			return this.html;
		}

		protected final String htmlize( String _text )
		{
			return _text.replace( "&", "&amp;" ).replace( "<", "&lt;" ).replace( ">", "&gt;" );
		}

		protected void addTerm( String _term )
		{
			this.terms.append( "terms << '" ).append( _term ).appendLine( "'" );
		}


		private final class SectionDoc implements Closeable
		{
			private final String title;
			private final DescriptionBuilder rowHtml = new DescriptionBuilder();
			private int lastRowIndex = -1;
			private String lastNormalizedRowExpr = "";
			private String[] highlightTerms = null;
			private int leftMostCol = -1;
			private int maxColCount = -1;

			public SectionDoc( Context _cx )
			{
				final RowSetup row = _cx.getRowSetup();
				this.title = row.getName();
			}

			public void close()
			{
				final DescriptionBuilder h = html();
				final int leftMostColName = 'A' + this.leftMostCol;
				h.append( "<h5 class=\"ref\">" ).append( htmlize( this.title ) ).appendLine( "</h5>" );
				h.append( "<table class=\"xl ref\"><thead><tr><td/>" );
				for (int col = 0; col < this.maxColCount; col++) {
					h.append( "<td>" ).append( (char) (leftMostColName + col) ).append( "</td>" );
				}
				h.appendLine( "</tr></thead><tbody>" );
				h.append( this.rowHtml );
				h.appendLine( "</tbody></table><p/>" );
			}

			public void newEngineRow( Context _cx ) throws Exception
			{
				if (!isNewRow( _cx )) return;
				visitRow( _cx ).newOrSimilarRow();
			}

			public void sameEngineRow( Context _cx )
			{
				if (!isNewRow( _cx )) return;
				visitRow( _cx ).similarRow();
			}

			private boolean isNewRow( Context _cx )
			{
				final int rowIndex = _cx.getRowIndex();
				if (rowIndex == this.lastRowIndex) return false;
				this.lastRowIndex = rowIndex;
				return true;
			}

			private RowDoc visitRow( Context _cx )
			{
				final RowSetup row = _cx.getRowSetup();
				extractLimitsFrom( _cx, row );
				extractHighlightsFrom( _cx.getRowCell( row.highlightCol() ) );
				return new RowDoc( _cx, row, this.rowHtml );
			}

			private void extractLimitsFrom( Context _cx, RowSetup _row )
			{
				if (this.leftMostCol < 0) this.leftMostCol = _row.expectedCol();
				this.maxColCount = Math.max( this.maxColCount, _row.documentedColCount() );
			}

			private void extractHighlightsFrom( CellInstance _cell )
			{
				if (null != _cell && null != _cell.getValue()) {
					final String highlights = _cell.getValue().toString();
					if (highlights.equals( "xx" )) {
						this.highlightTerms = null;
					}
					else {
						this.highlightTerms = htmlize( highlights ).split( " " );
						addTerm( highlights.split( " " )[ 0 ] );
					}
				}
			}


			private final class RowDoc
			{
				private final Context cx;
				private final RowSetup row;
				@SuppressWarnings( "hiding" )
				private final DescriptionBuilder html;
				private final int rowNum;
				private CellInstance exprCell;
				private String exprSource;
				private String exprPrefix;

				public RowDoc( Context _cx, RowSetup _row, DescriptionBuilder _html )
				{
					this.cx = _cx;
					this.row = _row;
					this.html = _html;
					this.rowNum = _cx.getRowIndex() + 1;
				}

				private void newOrSimilarRow() throws Exception
				{
					this.exprCell = this.cx.getOutputCell().getCell();
					if (null == this.exprCell) {
						this.exprSource = "";
						this.exprPrefix = "";
					}
					else if (this.exprCell instanceof CellWithLazilyParsedExpression) {
						this.exprSource = ((CellWithLazilyParsedExpression) this.exprCell).getExpressionParser().getSource();
						this.exprPrefix = "=";
					}
					else {
						this.exprSource = this.exprCell.getValue().toString();
						this.exprPrefix = "";
					}
					if (isNewExpr()) {
						newRow();
					}
					else {
						similarRow();
					}
				}

				private void newRow() throws Exception
				{
					final String expr = this.exprPrefix + highlightTermIn( htmlize( this.exprSource ) );
					final String prec = htmlPrecision( this.exprCell );
					final String linked = decompileEngineAndLinkFrom( expr );
					showRow( linked, prec );
				}

				private void similarRow()
				{
					showRow( "...", "" );
				}

				private void showRow( String _exprText, String _exprPrec )
				{
					final DescriptionBuilder h = this.html;
					final Object expected = formatExpectedValue( this.cx.getExpectedCell().getValue() );
					final String expectedCls = htmlCellClass( expected );
					h.append( "<tr><td class=\"xl-row\">" ).append( this.rowNum ).append( "</td>" );
					h.append( "<td" ).append( expectedCls ).append( ">" ).append( expected ).append( "</td>" );
					h.append( "<td class=\"xl-exp\">" ).append( _exprText ).append( _exprPrec ).append( "</td>" );

					final int rightMostColumn = this.row.expectedCol() + this.row.documentedColCount();
					for (int col = this.row.actualCol() + 1; col <= rightMostColumn; col++) {
						final CellInstance inputCell = this.cx.getRowCell( col );
						if (inputCell != null) {
							final Object input = inputCell.getValue();
							if (null != input) {
								final String inputCls = htmlCellClass( input );
								h.append( "<td" ).append( inputCls ).append( ">" ).append( htmlValue( input ) ).append(
										htmlPrecision( inputCell ) ).append( "</td>" );
							}
							else {
								final String inputExpr = ((CellWithLazilyParsedExpression) inputCell).getExpressionParser()
										.getSource();
								h.append( "<td>" ).append( htmlValue( inputExpr ) ).append( htmlPrecision( inputCell ) )
										.append( "</td>" );
							}
						}
						else {
							h.append( "<td/>" );
						}
					}

					final CellInstance excelSaysCell = this.cx.getRowCell( this.row.excelSaysCol() );
					if (null != excelSaysCell) {
						final Object excelSays = excelSaysCell.getValue();
						h.append( "<td class=\"ref-bad\">Excel says: " ).append( htmlValue( excelSays ) ).append( "</td>" );
					}

					h.appendLine( "</tr>" );
				}

				private Object formatExpectedValue( Object _val )
				{
					if (_val instanceof String) {
						String str = (String) _val;
						if (str.startsWith( "!NUM:" )) {
							return expectedErrorList( str.substring( 5 ) );
						}
						else if (str.startsWith( "!STR:" )) {
							return expectedErrorList( str.substring( 5 ) );
						}
						else if (str.startsWith( "!DATE:" )) {
							return expectedErrorList( str.substring( 6 ) );
						}
						else if (str.startsWith( "!BOOL:" )) {
							return expectedErrorList( str.substring( 6 ) );
						}
					}
					return _val;
				}

				private String expectedErrorList( String _errs )
				{
					final String[] acronyms = { "AE", "FE", "NA", "+Inf", "-Inf", "NaN" };
					final String[] titles = { "ArithmeticException", "FormulaException", "NotAvailableException",
							"Positive Infinity (double)", "Negative Infinity (double)", "Not A Number (double)" };
					String result = _errs;
					for (int i = 0; i < acronyms.length; i++) {
						result = result.replace( acronyms[ i ], "<acronym title=\""
								+ titles[ i ] + "\">" + acronyms[ i ] + "</acronym>" );
					}
					return "!" + result;
				}

				private final Object htmlValue( Object _value )
				{
					if (_value == null) return "";
					if (_value.toString().equals( "" )) return "' (empty string)";
					return _value;
				}

				private final String htmlCellClass( Object _value )
				{
					if (_value instanceof Date) return " class=\"xl-date\"";
					if (_value instanceof Number) return " class=\"xl-num\"";
					return "";
				}

				private final String htmlPrecision( CellInstance _inputCell )
				{
					if (_inputCell != null) {
						final int maxFrac = _inputCell.getMaxFractionalDigits();
						if (maxFrac < NumericType.UNLIMITED_FRACTIONAL_DIGITS) {
							final StringBuilder result = new StringBuilder();
							result.append( " <span class=\"ref-prec\">(#0." );
							for (int i = 0; i < maxFrac; i++) {
								result.append( "0" );
							}
							result.append( ")</span>" );
							return result.toString();
						}
					}
					return "";
				}

				private final String highlightTermIn( String _source )
				{
					final String[] terms = SectionDoc.this.highlightTerms;
					if (terms == null) return _source;
					String result = _source;
					for (final String term : terms) {
						result = result.replace( term, "<em>" + term + "</em>" );
					}
					return result;
				}

				private final String decompileEngineAndLinkFrom( String _exprText ) throws Exception
				{
					final String engineFileName = fileName() + "_" + this.rowNum;
					final DescriptionBuilder rex = new DescriptionBuilder();

					rex.append( "h1. Decompiled Code For <code>" ).append( this.exprSource ).appendLine( "</code>" );
					rex.newLine();
					rex.appendLine( "The expression" );
					rex.newLine();
					rex.append( "<pre><code>" ).append( stripEmphasis( _exprText ) ).appendLine( "</code></pre>" );
					rex.newLine();
					rex.appendLine( "is compiled to the following class(es):" );
					rex.newLine();

					final ByteCodeEngineSource decompiled = FormulaDecompiler.decompile( this.cx.getEngine() );
					final Map<String, String> classes = decompiled.getSortedClasses();
					boolean needToSkipFactory = true;
					for (final String source : classes.values()) {
						if (needToSkipFactory) {
							needToSkipFactory = false;
						}
						else {
							rex.append( "<notextile><pre jcite=\"jc\">" ).append( source ).appendLine( "</pre></notextile>" );
							rex.newLine();
						}
					}

					IOUtil.writeStringToIfNotUpToDate( rex.toString(), new File( HTML_PATH, engineFileName + ".rextile" ) );
					return "<a href=\"" + engineFileName + ".htm\">" + _exprText + "</a>";
				}

				private String stripEmphasis( String _exprText )
				{
					return _exprText.replace( "<em>", "" ).replace( "</em>", "" );
				}

				private boolean isNewExpr()
				{
					final String normalized = this.exprSource.replace( Integer.toString( this.rowNum ), "?" );
					if (normalized.equals( SectionDoc.this.lastNormalizedRowExpr )) return false;
					SectionDoc.this.lastNormalizedRowExpr = normalized;
					return true;
				}

			}

		}

	}

}
