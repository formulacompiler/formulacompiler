#! /bin/bash

# Location setup
src=`echo $0 | sed s/-apply.sh//`

# Sanity checks
if [ ! -f $src.hgq ]; then echo "Could not find:" $src.hgq; exit; fi
if [ ! -f $src.hgignore ]; then echo "Could not find:" $src.hgignore; exit; fi
if [ ! -f jxlrwtest.xls ]; then echo "Don't seem to be in a JExcelAPI source folder; missing jxlrwtest.xls"; exit; fi
if [ -a .hg ]; then echo "Current folder already is an hg repository (found .hg)."; exit; fi

# Init as hg repo
hg init ; hg qinit -c

# Copy .hgignore, then add unpacked files as rev 0
cp $src.hgignore .hgignore
hg add ; hg ci -m "Unpatched version of JExcelAPI."

# Unbundle the patch queue
hg -R .hg/patches unbundle --update $src.hgq

# Apply the patches, build and test
hg qpush -a
( cd build ; ant test )

