# Building and Testing a Release #

Releases are built on Linux using the following directory setup:

```
dist/
  tools/
    apache-ant-1.7.1/
    checkstyle-5.0/
    ... (all the needed tools that go into ...-tools.zip)
  make
  quick-tests.build.properties
```

Then `./make` is run in the `dist/` directory as follows:

```
#!/bin/bash

# Path to ant
ant=tools/apache-ant-1.7.1/bin/ant

# Synopsis:
#               diff_zip a b
# Description:
#               Diffs the contents of the two zip files a and b, ignoring .jars and time stamps.
#
function diff_zip {
        tmp="/tmp/formulacompiler_diff_zip"
        rm -rf $tmp
        mkdir $tmp

        mkdir $tmp/a
        unzip -q -d $tmp/a $1

        mkdir $tmp/b
        unzip -q -d $tmp/b $2
        
        diff \
                        -x "formulacompiler*.jar" \
                        -x "*.png" \
                        -rq $tmp/a $tmp/b
}


# Synopsis:
#               diff_jar a b
# Description:
#               Diffs the contents of the two jar files a and b ignoring time stamps.
#
function diff_jar {
        tmp="/tmp/formulacompiler_diff_jar"
        rm -rf $tmp
        mkdir $tmp

        mkdir $tmp/a
        unzip -d $tmp/a -q $1

        mkdir $tmp/b
        unzip -d $tmp/b -q $2
        
        diff \
                        -x "MANIFEST.MF" \
                        -rq $tmp/a $tmp/b
}


# Synopsis:
#               ./make repo rev version [--export] [--quick] [--build] [--tools] [--test-bin] [--test-src] [--cleanup]
# Description:
#               Runs a clean distro build straight from a svn export, then runs the build again from the archives just 
#               produced and compares the results. Passing "dev" means to use ../dev, that is, the local dev version.
#       Examples:
#               ./make http://formulacompiler.org/hg/afc 0.10.0 0.10.0
#               ./make ../dev tip 0.10.0 --export --build
#
if [ "$3" = "" ] ; then
        echo "Must specify repo, revision, and version number."

else

        export_from="$1"
        if [ "$2" = "tip" ]; then
                export_rev=
        else
                export_rev="-r $2"
        fi
        formulacompiler_ver="formulacompiler-$3"
        
        do_all=true
        do_export=
        do_quick=
        do_build=
        do_tools=
        do_test_bin=
        do_test_src=
        do_cleanup=
        for arg in "$@" ; do
                if [ "$arg" = "--export" ] ; then
                        do_all=
                        do_export=true
                elif [ "$arg" = "--quick" ] ; then
                        do_quick=true
                elif [ "$arg" = "--build" ] ; then
                        do_all=
                        do_build=true
                elif [ "$arg" = "--tools" ] ; then
                        do_all=
                        do_tools=true
                elif [ "$arg" = "--test-bin" ] ; then
                        do_all=
                        do_test_bin=true
                elif [ "$arg" = "--test-src" ] ; then
                        do_all=
                        do_test_src=true
                elif [ "$arg" = "--cleanup" ] ; then
                        do_all=
                        do_cleanup=true
                elif [ "$arg" = "-h" -o "$arg" = "--help" ] ; then
                        show_help=true
                fi
        done
        
        if [ is$do_all$do_export = istrue ] ; then
                echo "Cleaning out temp..."

                rm -rf release
                rm -rf $formulacompiler_ver
                echo "Cloning $export_from $export_rev to release..."
                hg clone $export_rev $export_from release
        fi
        
        if [ is$do_quick = istrue ] ; then
                echo "Making main build run more quickly..."
                cd release
                ln -s ../quick-tests.build.properties build.properties
                cd ..
        fi

        if [ is$do_all$do_build = istrue ] ; then
                if [ -f "release/build.xml" ] ; then
                        echo "Building release..."
                        cd release
                        ln -s ../tools tools
                        $ant -q dist
                        cd ..
                else
                        echo "release/build.xml is missing."
                fi
        fi
        
        if [ is$do_all$do_tools = istrue ] ; then
                if [ -f "release/build.xml" ] ; then
                        echo "Building release..."
                        cd release
                        $ant -q tools
                        cd ..
                else
                        echo "release/build.xml is missing."
                fi
        fi
        
        if [ is$do_all$do_test_bin = istrue ] ; then
                echo "Cleaning out $formulacompiler_ver..."

                rm -rf $formulacompiler_ver
                archive="release/dist/$formulacompiler_ver-bin.bin"
                if [ -f $archive ] ; then
                        echo "Unpacking binary dist $archive..."
                        unzip $archive
                        echo "Running sample using binary dist..."
                        cd $formulacompiler_ver
                        ln -s ../sample sample
                        
                                                echo "THIS IS STILL MISSING"
                        
                        cd ..
                else
                        echo $archive " is missing."
                fi
                echo "Verifying sample outputs (you should see no diffs here)..."
                echo "==================="
                
                                        echo "THIS IS STILL MISSING"
                                        # diff $formulacompiler_ver/test-java.htm $formulacompiler_ver/sample/expected/test-java.htm
                
                echo "==================="
        fi

        if [ is$do_all$do_test_src = istrue ] ; then
                echo "Cleaning out $formulacompiler_ver..."

                rm -rf $formulacompiler_ver
                archive="release/dist/$formulacompiler_ver-src.zip"
                if [ -f "$archive" ] ; then
                
                        echo "Unpacking source dist $archive..."
                        unzip $archive
                        echo "Building release from source dist..."
                        cd $formulacompiler_ver
                        ln -s ../tools tools
                        ln -s ../quick-tests.build.properties build.properties # Second build can always be quick
                        $ant -q dist
                        cd ..
                        
                        echo "Verifying results (you should see no diffs here)..."
                        echo "==================="
                        for suffix in bin src IDE-src ; do
                                # Doesn't diff the jars; they differ in internal time stamps ...
                                diff_zip release/dist/$formulacompiler_ver-$suffix.zip $formulacompiler_ver/dist/$formulacompiler_ver-$suffix.zip
                        done
                        # ... but diff the jar contents now.
                        for suffix in runtime compiler decompiler spreadsheet spreadsheet-excel-xls; do
                                diff_jar release/build/formulacompiler-$suffix.jar $formulacompiler_ver/build/formulacompiler-$suffix.jar
                        done
                        echo "==================="
                        
                else
                        echo $archive " is missing."
                fi
        fi
        
        if [ is$do_all$do_cleanup = istrue ] ; then
                echo "Cleaning out $formulacompiler_ver..."

                rm -rf $formulacompiler_ver
                echo "Cleaning out release..."

                cd release
                $ant -q clean
                cd ..
        fi
        
fi
```

## quick-tests.build.properties ##

```
# Make reference tests run faster.
test-ref-quick: true
```