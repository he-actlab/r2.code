#!/bin/sh
enerjdir=../../enerj
classpath=new.jar
mainclass=JMEIntTest
argument=

enerjargs=-noisy
if [ "$1" = "-nonoise" ]
then
enerjargs=
argument=$2
else
argument=$1
fi

# Run zxing.
$enerjdir/bin/enerj $enerjargs -cp $classpath $mainclass $argument

# Output stats.
# $enerjdir/bin/enerjstats
