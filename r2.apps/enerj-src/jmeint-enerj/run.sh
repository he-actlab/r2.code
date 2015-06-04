#!/bin/sh
enerjdir=../../r2.enerj
classpath=jmeint.jar
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

$enerjdir/bin/enerj $enerjargs -cp $classpath $mainclass $argument
