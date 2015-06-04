#!/bin/sh
enerjdir=../../r2.enerj
r2jar=$R2_ANALYSIS/r2-analysis.jar
classpath=jmeint.jar:$r2jar
#classpath=new.jar:$r2jar
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
