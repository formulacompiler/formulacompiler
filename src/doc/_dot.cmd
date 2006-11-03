for %%F in (*.dot) do dot -Tpng -Gsize=6,6 -o%%~nF.png %%F
rem for %%F in (*.dot) do dot -Tsvg -o%%~nF.svg %%F