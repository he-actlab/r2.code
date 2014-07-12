#!/bin/sh
enerjdir=../../enerj
#classpath=simpleRaytracer.jar
classpath=new.jar
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
#$enerjdir/bin/enerj $enerjargs -Xmx1024m -cp $classpath $mainclass 3 2 30 10
#$enerjdir/bin/enerj $enerjargs -Xmx1024m Plane 2 1 30 10 #$rayargs #recommended parameters are 2 1 30 10
#java Plane 2 1 30 10 #$rayargs #recommended parameters are 2 1 30 10
# $enerjdir/bin/enerjstats
