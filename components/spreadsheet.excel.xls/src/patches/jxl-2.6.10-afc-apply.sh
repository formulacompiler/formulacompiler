#!/bin/bash

# Location setup
junit=`echo $0 | sed "s/components\/spreadsheet.excel.xls\/src\/patches\/jxl-.*/lib\/test\/junit.jar/"`

# Sanity checks
if [ ! -f $junit ]; then echo "Could not find:" $junit; exit; fi
if [ ! -f jxlrwtest.xls ]; then echo "Don't seem to be in a JExcelAPI source folder; missing jxlrwtest.xls"; exit; fi
if [ ! -d .hg ]; then echo "Current folder is not a Mercurial repository (.hg not found)"; exit; fi

# Apply the patches, build and test
hg qpush -a
( cd build ; ant -lib $junit test )
