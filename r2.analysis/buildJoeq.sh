cd ../chord/libsrc/joeq ; ant clean ; ant jar
cp joeq.jar ../../main/lib/joeq.jar
cd ../../main ; ant clean ; ant 
cd ../../r2.analysis ; ant clean ; ant
