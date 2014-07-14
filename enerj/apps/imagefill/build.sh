#!/bin/sh
#enerjdir=../../enerj

#args=-Alint=simulation,mbstatic
#if [ "$1" = "-nosim" ]
#then
#args=-Alint=mbstatic
#fi

#$enerjdir/bin/enerjc $args src/*.java

if [ "$1" = "-nosim" ]
then
export EnerJNoSim=true
fi

rm -rf build
ant
