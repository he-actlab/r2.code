#!/bin/sh
enerjdir=../../enerj
#classpath=scimark2.jar
classpath=new.jar
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

# fft, sor, smm, mc, lu

$enerjdir/bin/enerj -Xmx2048m $enerjargs -cp $classpath $mainclass -tiny $scimarkargs
#$enerjdir/bin/enerj -Xmx2048m $enerjargs jnt.scimark2.commandline -tiny $scimarkargs
# $enerjdir/bin/enerjstats
