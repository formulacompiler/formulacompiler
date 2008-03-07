
def ver( afc_ver, tools_ver )
	return %Q[
	
<div class="download-box">
<p>

dt. "formulacompiler-#{afc_ver}-bin.zip":http://formulacompiler.org/dist/formulacompiler-#{afc_ver}-bin.zip : Binary release, including documentation and all required third-party libraries.

dt. "formulacompiler-#{afc_ver}-IDE-src.zip":http://formulacompiler.org/dist/formulacompiler-#{afc_ver}-IDE-src.zip : Source code and documentation for AFC in .jar files. Makes attaching source and doc in IDEs simple.

dt. "formulacompiler-#{afc_ver}-src.zip":http://formulacompiler.org/dist/formulacompiler-#{afc_ver}-src.zip : Complete source code for AFC, including tests and examples. Needs external tools to run (see below). Setup instructions are "here":../contribute/hacking/tools.htm.

dt. "formulacompiler-#{tools_ver}-tools.zip":http://formulacompiler.org/dist/formulacompiler-#{tools_ver}-tools.zip : Most of the external tools required to build an AFC distribution in a single download. It's OK if the version here is less than the others.

</p>
</div>

]
end


def oldver( afc_ver, tools_ver )
	return %Q[
	
h4. Release #{afc_ver}
	
	* "formulacompiler-#{afc_ver}-bin.zip":http://formulacompiler.org/dist/formulacompiler-#{afc_ver}-bin.zip
	* "formulacompiler-#{afc_ver}-IDE-src.zip":http://formulacompiler.org/dist/formulacompiler-#{afc_ver}-IDE-src.zip
	* "formulacompiler-#{afc_ver}-src.zip":http://formulacompiler.org/dist/formulacompiler-#{afc_ver}-src.zip
	* "formulacompiler-#{tools_ver}-tools.zip":http://formulacompiler.org/dist/formulacompiler-#{tools_ver}-tools.zip

]
end

