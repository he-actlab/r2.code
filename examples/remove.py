#!/usr/bin/python

import os, sys

p = os.popen("grep -rl \"import enerj.lang.*;\" . | grep -v remove.py")
lines = p.readlines()

for line in lines:
	print line.strip('\n')
	filename = line.strip('\n')
	os.system("sed -i -e \"s/import enerj.lang.*;//g\" " + filename)

p = os.popen("grep -rl \"Endorsements.endorse\" . | grep -v remove.py")
lines = p.readlines()

for line in lines:
	print line.strip('\n')
	filename = line.strip('\n')
	os.system("sed -i -e \"s/Endorsements.endorse//g\" " + filename)

p = os.popen("grep -rl \"ApproxMath\" . | grep -v remove.py")
lines = p.readlines()

for line in lines:
	print line.strip('\n')
	filename = line.strip('\n')
	os.system("sed -i -e \"s/ApproxMath/Math/g\" " + filename)

p = os.popen("grep -rl \"@Approximable\" . | grep -v remove.py")
lines = p.readlines()

for line in lines:
	print line.strip('\n')
	filename = line.strip('\n')
	os.system("sed -i -e \"s/@Approximable//g\" " + filename)

p = os.popen("grep -rl \"@Approx\" . | grep -v remove.py")
lines = p.readlines()

for line in lines:
	print line.strip('\n')
	filename = line.strip('\n')
	os.system("sed -i -e \"s/@Approx//g\" " + filename)

p = os.popen("grep -rl \"@Precise\" . | grep -v remove.py")
lines = p.readlines()

for line in lines:
	print line.strip('\n')
	filename = line.strip('\n')
	os.system("sed -i -e \"s/Precise//g\" " + filename)

p = os.popen("grep -rl \"@Context\" . | grep -v remove.py")
lines = p.readlines()

for line in lines:
	print line.strip('\n')
	filename = line.strip('\n')
	os.system("sed -i -e \"s/@Context//g\" " + filename)

os.system("find . -name \"*-e\" -type f -delete")
