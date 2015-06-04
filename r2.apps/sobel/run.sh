#!/bin/sh
enerjdir=../../r2.enerj
r2jar=$R2_ANALYSIS/r2-analysis.jar
classpath=sobel.jar:$r2jar
#classpath=new.jar:$r2jar
mainclass=Sobel.RgbImage

enerjargs=-noisy
sobelargs=
for arg
do
    case "$arg" in
    -nonoise) enerjargs= ;;
    *) sobelargs="$sobelargs$arg.rgb" ;;
    esac
done

$enerjdir/bin/enerj -Xmx2048m $enerjargs -cp $classpath $mainclass images/$sobelargs
#$enerjdir/bin/enerj -Xmx2048m $enerjargs jnt.sobel.commandline -tiny $sobelargs
# $enerjdir/bin/enerjstats
