#!/bin/sh
enerjdir=../../r2.enerj
classpath=mc.jar
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
