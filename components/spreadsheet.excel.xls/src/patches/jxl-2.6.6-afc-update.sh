#! /bin/bash

# Location setup
tgt=`echo $0 | sed "s/-update.sh//"`
lib=`echo $0 | sed "s/src\/patches\/jxl-.*//"`lib

# Sanity checks
if [ ! -f jxlrwtest.xls ]; then echo "Don't seem to be in a JExcelAPI source folder; missing jxlrwtest.xls"; exit; fi
if [ ! -d .hg ]; then echo "Current folder is no hg repository; missing .hg"; exit; fi

if ( cd build ; ant test srczip ); then

	# Copy build result over to afc.
	cp jxl.jar $lib/impl/jxl.jar
	cp jxl-src.zip $lib/IDE/jxl-src.zip

	# Make export of just the base patches, not the tests containing binary files.
	hg qpop -a
	hg qselect no-tests
	hg qpush -a

	# Drop these from the export:
	# Date 1200579767 -3600
	# Node ID 8907c6f911764057bddef7d269a618f22111f1c1
	# Parent  53fa976136fce187cbc110df8da98dff277083ad
	hg export 1:tip \
		| sed -e "/^# Node ID /d" \
			-e "/^# Parent /d" \
			-e "/^# Date /d" \
		>$tgt.hgexport
	
	# Make bundle of whole patch queue, including tests with binary files.
	hg qpop -a
	hg qselect -n
	hg qpush -a
	hg -R .hg/patches bundle --base null $tgt.hgq

else
	echo "BUILD FAILED!"
fi
