#!/bin/bash

# Location setup
src=`echo $0 | sed s/-apply.sh//`

# Sanity checks
if [ ! -f $src.hgq ]; then echo "Could not find:" $src.hgq; exit; fi
if [ ! -f jxlrwtest.xls ]; then echo "Don't seem to be in a JExcelAPI source folder; missing jxlrwtest.xls"; exit; fi
if [ ! -d .hg ]; then echo "Current folder is not a Mercurial repository (.hg not found)"; exit; fi

# Init mq
hg qinit -c

# Unbundle the patch queue
hg -R .hg/patches unbundle --update $src.hgq

# Apply the patches, build and test
hg qpush -a
( cd build ; ant test )
