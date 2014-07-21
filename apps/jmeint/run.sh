#!/bin/sh
enerjdir=../../enerj
expaxjar=$EXPAX_ANALYSIS/expax-analysis.jar
classpath=jmeint.jar:$expaxjar
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
