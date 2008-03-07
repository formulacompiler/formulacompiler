Attribute VB_Name = "refTestSheets"
Sub ApplyConditionalFormatting()
    Dim ls As String
    ls = Application.International(xlListSeparator)
    
    Cells.Select
    Selection.FormatConditions.Delete

    Range("A1").Select
    Selection.FormatConditions.Add Type:=xlExpression, Formula1:= _
        "=NOT(Q1)"
    Selection.FormatConditions(1).Interior.ColorIndex = 22
    
    Range("A2:A10000").Select
    Selection.FormatConditions.Add Type:=xlExpression, Formula1:= _
        "=NOT(OR(ISBLANK(Q2)" + ls + "Q2))"
    Selection.FormatConditions(1).Interior.ColorIndex = 22
    Selection.FormatConditions.Add Type:=xlExpression, Formula1:= _
        "=NOT(AND(ISBLANK(M2)" + ls + "ISBLANK(O2)))"
    Selection.FormatConditions(2).Interior.ColorIndex = 20
    
    Range("C2:I10000").Select
    Selection.FormatConditions.Add Type:=xlExpression, Formula1:= _
        "=$J2>COLUMN(C2)-3"
    Selection.FormatConditions(1).Interior.ColorIndex = 35
    
    Range("M2:M10000").Select
    Selection.FormatConditions.Add Type:=xlExpression, Formula1:= _
        "=AND(NOT(ISBLANK(M2))" + ls + "IF(ISERROR(A2)" + ls + "ERROR.TYPE(A2)=ERROR.TYPE(M2)" + ls + "A2=M2))"
    Selection.FormatConditions(1).Interior.ColorIndex = 27
    
    Range("A1").Select
End Sub

Sub AddCheckingCells()
    Range("P2:Q10000").Select
    Selection.ClearContents
        
    Dim Counter As Integer
    For Counter = 2 To 10000
        If Not IsEmpty(Cells(Counter, 2)) Then
            Cells(Counter, 16).FormulaR1C1 = _
                "=OR(ISBLANK(RC[-14]),IF(ISERROR(RC[-14]),ERROR.TYPE(RC[-14])=IF(ISBLANK(RC[-3]),ERROR.TYPE(RC[-15]),ERROR.TYPE(RC[-3])),IF(ISBLANK(RC[-3]),AND(NOT(ISBLANK(RC[-15])),RC[-15]=RC[-14]),RC[-14]=RC[-3])))"
            Cells(Counter, 17).FormulaR1C1 = _
                "=IF(ISBLANK(RC[-2]),IF(ISERROR(RC[-1]),FALSE,RC[-1]),RC[-2])"
        End If
    Next Counter
    
    Range("A1").Select
End Sub

Sub FillCellHeaders()
    Rows(1).Select
    
    With Selection
        .Clear
        .RowHeight = 25.5
        .HorizontalAlignment = xlGeneral
        .VerticalAlignment = xlCenter
        .WrapText = False
        .Orientation = 0
        .AddIndent = False
        .IndentLevel = 0
        .ShrinkToFit = False
        .ReadingOrder = xlContext
        .MergeCells = False
        .Font.Underline = xlUnderlineStyleSingle
    End With
    
    Cells(1, 1).FormulaR1C1 = "=IF( RC[16], ""Expected"", ""FAILED!"" )"
    Cells(1, 2).Value = "Actual"
    Cells(1, 3).Value = "Inputs"
    Cells(1, 10).Value = "# of Inputs"
    Cells(1, 11).Value = "Name"
    Cells(1, 12).Value = "Highlight"
    Cells(1, 13).Value = "Excel says"
    Cells(1, 14).Value = "Skip for"
    Cells(1, 15).Value = "Custom check"
    Cells(1, 17).FormulaR1C1 = "=AND( R[1]C:R[9999]C )"
End Sub

Sub ForceAllFormatsAndColumns()
    Call FillCellHeaders
    Call AddCheckingCells
    Call ApplyConditionalFormatting
End Sub
