for %%F in (*.dot) do dot -Tpng -o%%~nF.png %%F
rem for %%F in (*.dot) do dot -Tsvg -o%%~nF.svg %%F