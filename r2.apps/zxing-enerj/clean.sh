#!/bin/sh

if [ "$1" = "-nosim" ]
then
export EnerJNoSim=true
fi

cd core
ant clean
cd ../javase
ant clean
