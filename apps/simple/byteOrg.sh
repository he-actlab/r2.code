cd src
javac Simple.java
javap -c Simple > Simple.org
mv Simple.org ../tests/
rm Simple.class

