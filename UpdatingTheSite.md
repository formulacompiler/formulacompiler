# Updating the Web Site #

The site is updated by pushing to the http://formulacompiler.org/hg/afc-site/ repo. The following script can be used to update a local clone from a release build:

```
#!/bin/bash

cd ~/dev/afc
if [ "$1" = "" ]; then
	echo "Please supply the source working copy to use."
elif [ ! -f "$1/doc/index.htm" ]; then
	echo "$1/doc/index.htm not found."
else
	rm up-site/* -rf
	hg -R up-site revert -a --no-backup
	hg -R up-site pull -u
	rm up-site/* -rf
	ls $1/doc | xargs -I NAME cp -r {$1/doc/,up-site/}NAME
	if [ -d "$1/dist" ] ; then
		mkdir up-site/dist
		ls $1/dist | xargs -I NAME cp -r {$1/dist/,up-site/dist/}NAME
	else
		hg --cwd up-site revert dist/
	fi
	cd up-site
	../add-google-analytics.py
	hg addrem
	hg stat -rn dist | xargs hg revert # undo removals of old binary releases
	hg stat
	cd ..
fi
```

and here's add-google-analytics.py which adds Google Analytics script:

```
#!/usr/bin/env python

import os

snippet = '''
<script type="text/javascript">
var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
</script>
<script type="text/javascript">
var pageTracker = _gat._getTracker("UA-1112076-2");
pageTracker._initData();
pageTracker._trackPageview();
</script>
'''

signature = 'var pageTracker = _gat._getTracker("UA-1112076-2");\n'


def process( filename ):
	inf = file( filename, 'r' )
	try:
		lines = inf.readlines()
	finally:
		inf.close()
	if signature not in lines:
		try:
			headend = lines.index( '</head>\r\n' )
		except ValueError: 
			try:
				headend = lines.index( '</HEAD>\n' )
			except ValueError:
				print repr( lines )
				print 'WARNING: %s has no </head> tag' % filename
				raise
		lines.insert( headend, snippet )
		outf = file( filename, 'w' )
		try:
			outf.writelines( lines )
			outf.truncate()
		finally:
			outf.close()
		print "%s updated" % filename

def visit( arg, dirname, files ):
	for f in files:
		if f.endswith( '.htm' ) or f.endswith( '.html' ):
			process( os.path.join( dirname, f ))

os.path.walk( '.', visit, None )
```