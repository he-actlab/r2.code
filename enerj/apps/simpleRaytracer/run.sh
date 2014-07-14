#!/bin/sh
enerjdir=../../enerj
classpath=new.jar
mainclass=Plane

enerjargs=-noisy
rayargs=
for arg
do
    case "$arg" in
    -nonoise) enerjargs= ;;
    *) ratargs="$rayargs $arg" ;;
    esac
done

$enerjdir/bin/enerj $enerjargs -Xmx1024m -cp $classpath $mainclass 2 1 30 10
#$enerjdir/bin/enerj $enerjargs -Xmx1024m Plane 2 1 30 10 #$rayargs #recommended parameters are 2 1 30 10
#java Plane 2 1 30 10 #$rayargs #recommended parameters are 2 1 30 10
# $enerjdir/bin/enerjstats
