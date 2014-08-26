#!/bin/sh

cd jsr308-langtools/make ; ant
cd ../../annotation-tools/asmx ; ant clean ; ant
cd ../ ; ant clean ; ant
cd ../checker-framework/checkers/ ; ant
cd ../../checker-runtime/ ; ant
cd ../r2.enerj/ ; ant
cd ../chord/main ; ant
cd ../../r2.analysis/ ; ant
cd ../r2.optimization/ ; ant
