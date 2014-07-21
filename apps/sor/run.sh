#!/bin/sh
enerjdir=../../enerj
expaxjar=$EXPAX_ANALYSIS/expax-analysis.jar
classpath=sor.jar:$expaxjar
mainclass=jnt.scimark2.commandline

enerjargs=-noisy
scimarkargs=
for arg
do
    case "$arg" in
    -nonoise) enerjargs= ;;
    *) scimarkargs="$scimarkargs $arg" ;;
    esac
done

$enerjdir/bin/enerj -Xmx2048m $enerjargs -cp $classpath $mainclass $scimarkargs
