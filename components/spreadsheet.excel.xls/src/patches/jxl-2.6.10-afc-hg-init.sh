#!/bin/bash

# Location setup
src=`echo $0 | sed s/-hg-init.sh//`

# Sanity checks
if [ ! -f $src.hgignore ]; then echo "Could not find:" $src.hgignore; exit; fi
if [ ! -f $src.hgq ]; then echo "Could not find:" $src.hgq; exit; fi
if [ ! -f jxlrwtest.xls ]; then echo "Don't seem to be in a JExcelAPI source folder; missing jxlrwtest.xls"; exit; fi
if [ -a .hg ]; then echo "Current folder already is an hg repository (found .hg)."; exit; fi

# Convert Java source files to UNIX format
find . -name *.java -type f -print0 | xargs -0 dos2unix
find . -name *.properties -type f -print0 | xargs -0 dos2unix
dos2unix .classpath .project build/build.xml

# Init as hg repo
hg init

# Copy .hgignore, then add unpacked files as rev 0
cp $src.hgignore .hgignore
hg add
hg ci -m "Unpatched version of JExcelAPI."

# Init mq
hg qinit -c

# Unbundle the patch queue
hg -R .hg/patches unbundle --update $src.hgq
