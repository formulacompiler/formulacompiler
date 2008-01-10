import uno

from com.sun.star.awt.FontUnderline import SINGLE as FONT_UNDERLINE_SINGLE

from com.sun.star.sheet.ConditionOperator import FORMULA as CONDITION_OPERATOR_FORMULA

from com.sun.star.table.CellContentType import EMPTY as CELL_CONTENT_TYPE_EMPTY

from com.sun.star.table.CellVertJustify import CENTER as CELL_VERT_JUSTIFY_CENTER


document = XSCRIPTCONTEXT.getDocument()
sheet = document.getSheets().getByIndex(0)

def forceAllFormatsAndColumns():

    def applyConditionalFormatting():

        def createStyles():
            styleFamilies = document.getStyleFamilies()
            cellStyles = styleFamilies.getByName("CellStyles")

            def createCellStyle(name):
                if cellStyles.hasByName(name):
                    cellStyles.removeByName(name)
                
                cellStyle = document.createInstance("com.sun.star.style.CellStyle")
                cellStyles.insertByName(name, cellStyle)
                return cellStyle
        
            createCellStyle("Error").CellBackColor = 0xFF8080
            createCellStyle("Warning").CellBackColor = 0xFFFF77
            createCellStyle("Parameter").CellBackColor = 0xCCFFCC
            createCellStyle("Attention").CellBackColor = 0xCCFFFF
            
        def addNewConditionalFormat(conditionalFormat, formula, styleName):
            
            def createPropertyValue( name=None, value=None, handle=None, state=None ):
                propertyValue = uno.createUnoStruct( "com.sun.star.beans.PropertyValue" )

                if name != None:
                    propertyValue.Name = name
                if value != None:
                    propertyValue.Value = value
                if handle != None:
                    propertyValue.Handle = handle
                if state != None:
                    propertyValue.State = state

                return propertyValue
            
            operatorPropValue = createPropertyValue("Operator", CONDITION_OPERATOR_FORMULA)
            formulaPropValue = createPropertyValue("Formula1", formula)
            styleNamePropValue = createPropertyValue("StyleName", styleName)

            conditionalFormat.addNew((operatorPropValue, formulaPropValue, styleNamePropValue))

        createStyles()

        #def changeConditionalFormat(cells, change):
        #    conditionalFormat = cells.ConditionalFormat
        #    change(conditionalFormat)
        #    cells.ConditionalFormat = conditionalFormat
            
        conditionalFormat = sheet.ConditionalFormat
        conditionalFormat.clear()
        sheet.ConditionalFormat = conditionalFormat

        cellRange = sheet.getCellRangeByName("A1")
        conditionalFormat = cellRange.ConditionalFormat
        addNewConditionalFormat(conditionalFormat, "NOT(AND($Q$2:$Q$10000))", "Error")
        cellRange.ConditionalFormat = conditionalFormat
        
        cellRange = sheet.getCellRangeByName("A2:A10000")
        conditionalFormat = cellRange.ConditionalFormat
        addNewConditionalFormat(conditionalFormat, "NOT(OR(ISBLANK($Q1);$Q1))", "Error")
        addNewConditionalFormat(conditionalFormat, "NOT(AND(ISBLANK($M1);ISBLANK($O1)))", "Attention")
        cellRange.ConditionalFormat = conditionalFormat

        cellRange = sheet.getCellRangeByName("C2:I10000")
        conditionalFormat = cellRange.ConditionalFormat
        addNewConditionalFormat(conditionalFormat, "$J1>COLUMN(A1)-3", "Parameter") 
        cellRange.ConditionalFormat = conditionalFormat

        cellRange = sheet.getCellRangeByName("M2:M10000")
        conditionalFormat = cellRange.ConditionalFormat
        addNewConditionalFormat(conditionalFormat, "AND(NOT(ISBLANK(A1));IF(ISERROR($A1);ERRORTYPE($A1)=ERRORTYPE(A1);$A1=A1))", "Warning")
        cellRange.ConditionalFormat = conditionalFormat

    def addCheckingCells():
        cellRange = sheet.getCellRangeByName("P2:Q10000")
        cellRange.clearContents(1023)

        for i in range(1, 100):
            if str(sheet.getCellByPosition(1, i).getType()) != str(CELL_CONTENT_TYPE_EMPTY):
                row = str(i + 1)
                exp = "A" + row
                act = "B" + row
                m = "M" + row
                sheet.getCellByPosition(15, i).Formula = "=OR(ISBLANK(" + act + ");IF(ISERROR(" + act + ");"\
                                                         + "ERRORTYPE(" + act + ")=IF(ISBLANK(" + m + ");ERRORTYPE(" + exp + ");ERRORTYPE(" + m +"));"\
                                                         + "IF(ISBLANK(" + m + ");AND(NOT(ISBLANK(" + exp + "));"\
                                                         + exp + "=" + act + ");" + act + "=" + m + ")))"
                o = "O" + row
                p = "P" + row
                sheet.getCellByPosition(16, i).Formula = "=IF(ISBLANK(" + o + ");IF(ISERROR(" + p + ");FALSE();" + p + ");" + o + ")"
                
    def fillCellHeaders():
        row1 = sheet.getRows().getByIndex(0)
        row1.clearContents(1023)
        row1.CharUnderline = FONT_UNDERLINE_SINGLE
        row1.VertJustify = CELL_VERT_JUSTIFY_CENTER
        row1.Height = 800

        #With Selection
        #    .Clear
        #    .RowHeight = 25.5
        #    .HorizontalAlignment = xlGeneral
        #    .VerticalAlignment = xlCenter
        #    .WrapText = False
        #    .Orientation = 0
        #    .AddIndent = False
        #    .IndentLevel = 0
        #    .ShrinkToFit = False
        #    .ReadingOrder = xlContext
        #    .MergeCells = False
        #    .Font.Underline = xlUnderlineStyleSingle
        #End With
        
        sheet.getCellRangeByName("A1").setString("Expected")
        sheet.getCellRangeByName("B1").setString("Actual")
        sheet.getCellRangeByName("C1").setString("Inputs")
        sheet.getCellRangeByName("J1").setString("# of Inputs")
        sheet.getCellRangeByName("K1").setString("Name")
        sheet.getCellRangeByName("L1").setString("Highlight")
        sheet.getCellRangeByName("M1").setString("OOo says")
        sheet.getCellRangeByName("N1").setString("Skip for")
        sheet.getCellRangeByName("O1").setString("Custom check")


    fillCellHeaders()
    addCheckingCells()
    applyConditionalFormatting()
