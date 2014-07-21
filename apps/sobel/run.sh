#!/bin/sh
enerjdir=../../enerj
expaxjar=$EXPAX_ANALYSIS/expax-analysis.jar
#classpath=sobel.jar:$expaxjar
classpath=new.jar:$expaxjar
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
