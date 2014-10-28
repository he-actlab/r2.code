#!/bin/sh
enerjdir=$R2_ENERJ
r2jar=$R2_ANALYSIS/r2-analysis.jar
classpath=mc.jar:$r2jar
#classpath=new.jar:$r2jar
mainclass=jnt.scimark2.MonteCarlo

enerjargs=-noisy
scimarkargs=
for arg
do
    case "$arg" in
    -nonoise) enerjargs= ;;
    *) scimarkargs="$scimarkargs $arg" ;;
    esac
done

echo $enerjdir/bin/enerj -Xmx2048m $enerjargs -cp $classpath $mainclass $scimarkargs
$enerjdir/bin/enerj -Xmx2048m $enerjargs -cp $classpath $mainclass $scimarkargs
