#!/bin/sh
enerjdir=../../enerj
classpath=simple.jar
mainclass=Simple

enerjargs=-noisy
sobelargs=
for arg
do
    case "$arg" in
    -nonoise) enerjargs= ;;
    *) sobelargs="$sobelargs$arg.rgb" ;;
    esac
done

$enerjdir/bin/enerj -Xmx2048m $enerjargs -cp $classpath $mainclass
