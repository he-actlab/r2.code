#!/bin/sh
enerjdir=../../enerj
classpath=javase/javase.jar:core/core.jar
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
