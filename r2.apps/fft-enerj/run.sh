#!/bin/sh
enerjdir=../../r2.enerj
classpath=fft.jar
mainclass=jnt.scimark2.commandline

enerjargs=-noisy
fftargs=
for arg
do
    case "$arg" in
    -nonoise) enerjargs= ;;
    *) fftargs="$fftargs $arg" ;;
    esac
done

echo $enerjdir/bin/enerj -Xmx2048m $enerjargs -cp $classpath $mainclass $fftargs
$enerjdir/bin/enerj -Xmx2048m $enerjargs -cp $classpath $mainclass $fftargs
