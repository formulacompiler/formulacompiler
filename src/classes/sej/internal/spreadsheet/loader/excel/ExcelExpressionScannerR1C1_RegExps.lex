IDX		= ("[" "-"? {INT} "]") | {INT}
ROW		= "R" {IDX}?
COL		= "C" {IDX}?
CELL	= {ROW} {COL}
