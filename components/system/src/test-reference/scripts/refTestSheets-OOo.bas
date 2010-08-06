REM  *****  BASIC  *****

Sub Main
	ForceAllFormatsAndColumns()
End Sub


Function CreateCellStyle(oDocument, oCellStyles, sName) As Object
	If oCellStyles.hasByName(sName) Then
		oCellStyles.removeByName(sName)
	End If

	oCellStyle = oDocument.createInstance("com.sun.star.style.CellStyle")
	oCellStyles.insertByName(sName, oCellStyle)
	CreateCellStyle = oCellStyle
End Function

Sub CreateStyles(oDocument)
	oStyleFamilies = oDocument.getStyleFamilies()
	oCellStyles = oStyleFamilies.getByName("CellStyles")

	CreateCellStyle(oDocument, oCellStyles, "Error").CellBackColor = &HFF8080
	CreateCellStyle(oDocument, oCellStyles, "Warning").CellBackColor = &HFFFF77
	CreateCellStyle(oDocument, oCellStyles, "Parameter").CellBackColor = &HCCFFCC
	CreateCellStyle(oDocument, oCellStyles, "Attention").CellBackColor = &HCCFFFF
End Sub

Function CreatePropertyValue(sName, value)
	Dim oPropertyValue As New com.sun.star.beans.PropertyValue
	With oPropertyValue
		.Name = sName
		.Value = value
	End With
	CreatePropertyValue = oPropertyValue
End Function

Sub AddNewConditionalFormat(conditionalFormat, formula, styleName)
	Dim oConditionalFormatProperty(3) As New com.sun.star.beans.PropertyValue
	oConditionalFormatProperty(0) = CreatePropertyValue("Operator", com.sun.star.sheet.ConditionOperator.FORMULA)
	oConditionalFormatProperty(1) = CreatePropertyValue("Formula1", formula)
	oConditionalFormatProperty(2) = CreatePropertyValue("StyleName", styleName)
	conditionalFormat.addNew(oConditionalFormatProperty)
End Sub

Sub ApplyConditionalFormatting(oDocument, oSheet)
	CreateStyles(oDocument)
		
	conditionalFormat = oSheet.ConditionalFormat
	conditionalFormat.clear()
	oSheet.ConditionalFormat = conditionalFormat
	
	cellRange = oSheet.getCellRangeByName("A1")
	conditionalFormat = cellRange.ConditionalFormat
	AddNewConditionalFormat(conditionalFormat, "NOT(AND($Q$2:$Q$10000))", "Error")
	cellRange.ConditionalFormat = conditionalFormat
	
	cellRange = oSheet.getCellRangeByName("A2:A10000")
	conditionalFormat = cellRange.ConditionalFormat
	AddNewConditionalFormat(conditionalFormat, "NOT(OR(ISBLANK($Q1);$Q1))", "Error")
	AddNewConditionalFormat(conditionalFormat, "NOT(AND(ISBLANK($M1);ISBLANK($O1)))", "Attention")
	cellRange.ConditionalFormat = conditionalFormat
	
	cellRange = oSheet.getCellRangeByName("C2:I10000")
	conditionalFormat = cellRange.ConditionalFormat
	AddNewConditionalFormat(conditionalFormat, "$J1>COLUMN(A1)-3", "Parameter")
	cellRange.ConditionalFormat = conditionalFormat
	
	cellRange = oSheet.getCellRangeByName("M2:M10000")
	conditionalFormat = cellRange.ConditionalFormat
	AddNewConditionalFormat(conditionalFormat, "AND(NOT(ISBLANK(A1));IF(ISERROR($A1);ERRORTYPE($A1)=ERRORTYPE(A1);$A1=A1))", "Warning")
	cellRange.ConditionalFormat = conditionalFormat
End Sub

Sub AddCheckingCells(oSheet)
	cellRange = oSheet.getCellRangeByName("P2:Q10000")
	cellRange.clearContents(1023)
	
	Dim i As Integer
	For i = 1 to 10000
		If str(oSheet.getCellByPosition(1, i).getType()) <> str(com.sun.star.table.CellContentType.EMPTY) Then
			Dim row As Integer
			Dim exp, act, m, o, p As String
			row = i + 1
			exp = "A" & row
			act = "B" & row
			m = "M" & row
			oSheet.getCellByPosition(15, i).Formula = "=IF(ISBLANK(" + m + ");" + exp + ";" + m + ")"
			o = "O" & row
			p = "P" & row
			oSheet.getCellByPosition(16, i).Formula = "=IF(ISBLANK(" + o + ");IF(ISERROR(" + act + ");""Err:""&ERRORTYPE(" + act + ")=" + m + ";OR(" + p + "=" + act + ";AND(ISNUMBER(" + p + ");ISNUMBER(" + act + ");OR(AND(" + p + "=0;ABS(" + act + ")<1E-307);""""&" + act + "=""""&" + p + "))));" + o + ")"
		End If
	Next i
End Sub

Sub FillCellHeaders(oSheet)
	row1 = oSheet.getRows().getByIndex(0)
	row1.clearContents(1023)
	row1.CharUnderline = com.sun.star.awt.FontUnderline.SINGLE
	row1.VertJustify = com.sun.star.table.CellVertJustify.CENTER
	row1.Height = 800
	
	oSheet.getCellRangeByName("A1").Formula = "=IF(Q1;""Expected"";""FAILED!"")"
	oSheet.getCellRangeByName("B1").setString("Actual")
	oSheet.getCellRangeByName("C1").setString("Inputs")
	oSheet.getCellRangeByName("J1").setString("# of Inputs")
	oSheet.getCellRangeByName("K1").setString("Name")
	oSheet.getCellRangeByName("L1").setString("Highlight")
	oSheet.getCellRangeByName("M1").setString("OOo says")
	oSheet.getCellRangeByName("N1").setString("Skip for")
	oSheet.getCellRangeByName("O1").setString("Custom check")
	oSheet.getCellRangeByName("Q1").Formula = "=AND(Q2:Q10000)"
End Sub

Sub ForceAllFormatsAndColumns
	oDocument = ThisComponent
	oSheet = oDocument.getSheets().getByIndex(0)
	
    FillCellHeaders(oSheet)
    AddCheckingCells(oSheet)
    ApplyConditionalFormatting(oDocument, oSheet)
End Sub

