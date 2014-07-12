#!/usr/bin/python

import os, sys

p = os.popen("grep -rl \"jspark.com.google.zxing\" . | grep -v remove.py")
lines = p.readlines()

for line in lines:
	print line.strip('\n')
	filename = line.strip('\n')
	os.system("sed -i -e \"s/jspark.com.google.zxing/com.google.zxing/g\" " + filename)


