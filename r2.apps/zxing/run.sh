#!/bin/sh
enerjdir=../../r2.enerj
r2jar=$R2_ANALYSIS/r2-analysis.jar
classpath=javase/javase.jar:core/core.jar:$r2jar
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

# java -cp javase/javase.jar:core/core.jar:$R2_ANALYSIS/r2-analysis.jar com.google.zxing.client.j2se.CommandLineRunner input1.png
