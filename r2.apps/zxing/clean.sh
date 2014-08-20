#!/bin/sh

if [ "$1" = "-nosim" ]
then
export EnerJNoSim=true
fi

rm zxing-core-*
rm zxing-javase-*
rm markJavaZxing.log
rm enerj*

cd core
ant clean
cd ../javase
ant clean
