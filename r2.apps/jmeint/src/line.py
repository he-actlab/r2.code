#!/usr/bin/python

import os, sys

jFileList = os.popen('find . -name "*.java"').readlines()

linesum = 0;
for jFile in jFileList:
	result = os.popen('wc -l ' + jFile.strip('\n') + ' | awk \'{print $1}\'').readlines()[0]
	linesum = linesum + int(result)
print 'total lines: ' + str(linesum)
