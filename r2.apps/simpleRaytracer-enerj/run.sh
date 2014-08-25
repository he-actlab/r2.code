#!/bin/sh
enerjdir=../../r2.enerj
classpath=simpleRaytracer.jar
mainclass=Plane

enerjargs=-noisy
if [ "$1" = "-nonoise" ]
then
enerjargs=
arguments=$2' '$3' '$4' '$5
else
arguments=$1' '$2' '$3' '$4
fi

$enerjdir/bin/enerj $enerjargs -Xmx1024m -cp $classpath $mainclass $arguments
