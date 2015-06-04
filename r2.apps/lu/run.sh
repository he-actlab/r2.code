#!/bin/sh
enerjdir=../../r2.enerj
r2jar=$R2_ANALYSIS/r2-analysis.jar
classpath=lu.jar:$r2jar
#classpath=new.jar:$r2jar
mainclass=jnt.scimark2.LU

enerjargs=-noisy
scimarkargs=100
for arg
do
    case "$arg" in
    -nonoise) enerjargs= ;;
    *) scimarkargs="$scimarkargs $arg" ;;
    esac
done


$enerjdir/bin/enerj -Xmx2048m $enerjargs -cp $classpath $mainclass $scimarkargs
