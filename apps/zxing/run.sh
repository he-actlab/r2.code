#!/bin/sh
enerjdir=../../enerj
expaxjar=$EXPAX_ANALYSIS/expax-analysis.jar
classpath=javase/javase.jar:core/core.jar:$expaxjar
mainclass=com.google.zxing.client.j2se.CommandLineRunner
filename=

enerjargs=-noisy
if [ "$1" = "-nonoise" ]
then
enerjargs=
filename=input$2.png
else
filename=input$1.png
fi

zxingargs=
if [ "$1" = "-prof" ]
then
enerjargs=
zxingargs=--sleep
fi

# Run zxing.
$enerjdir/bin/enerj $enerjargs -cp $classpath $mainclass $filename $zxingargs

# Output stats.
# $enerjdir/bin/enerjstats

# java -cp javase/javase.jar:core/core.jar:../../expax.analysis/expax-analysis.jar com.google.zxing.client.j2se.CommandLineRunner input1.png
