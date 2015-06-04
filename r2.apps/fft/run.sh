#!/bin/sh
enerjdir=../../r2.enerj
r2jar=$R2_ANALYSIS/r2-analysis.jar
classpath=fft.jar:$r2jar
#classpath=new.jar:$r2jar
mainclass=jnt.scimark2.FFT

enerjargs=-noisy
fftargs=
for arg
do
    case "$arg" in
    -nonoise) enerjargs= ;;
    *) fftargs="$fftargs $arg" ;;
    esac
done
$enerjdir/bin/enerj -Xmx2048m $enerjargs -cp $classpath $mainclass $fftargs
