#!/bin/sh
enerjdir=../../enerj
classpath=new.jar
mainclass=JMEIntTest

enerjargs=-noisy
if [ "$1" = "-nonoise" ]
then
enerjargs=
fi

# Run zxing.
$enerjdir/bin/enerj $enerjargs -cp $classpath $mainclass

# Output stats.
# $enerjdir/bin/enerjstats
