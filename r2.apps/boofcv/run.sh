#!/bin/sh
enerjdir=../../r2.enerj
r2jar=$R2_ANALYSIS/r2-analysis.jar
classpath=boofcv.jar:$r2jar:libraries/core-0.26.jar:libraries/ddogleg-0.6.jar:libraries/georegression-0.7.jar:libraries/xmlpull-1.1.3.1.jar:libraries/xpp3_min-1.1.4c.jar:libraries/xstream-1.4.7.jar
#classpath=new.jar:$r2jar:libraries/core-0.26.jar:libraries/ddogleg-0.6.jar:libraries/georegression-0.7.jar:libraries/xmlpull-1.1.3.1.jar:libraries/xpp3_min-1.1.4c.jar:libraries/xstream-1.4.7.jar
mainclass=boofcv.examples.features.ExampleInterestPoint

enerjargs=-noisy
boofcvargs=
for arg
do
    case "$arg" in
    -nonoise) enerjargs= ;;
    *) boofcvargs="$boofcvargs $arg" ;;
    esac
done
$enerjdir/bin/enerj -Xmx2048m $enerjargs -cp $classpath $mainclass $boofcvargs
