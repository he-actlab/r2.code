#!/usr/bin/python

import os, sys

p = os.popen("ps aux | grep \"chord\" | awk '{print $2}'")
filelist = p.readlines()

for f in filelist:
	pname = f.strip('\n')
	os.system('kill -9 ' + pname)

