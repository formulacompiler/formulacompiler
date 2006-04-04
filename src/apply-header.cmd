for  /r %%f in (*.java) do call :applyto %%f
goto :eof

:applyto
pushd %~p1
echo %~n1
copy \java\projects\sej\trunk\src\copyright-header.txt /b + %~n1.java /b %~n1.tmp /b
del %~n1.java
ren %~n1.tmp %~n1.java
popd
goto :eof