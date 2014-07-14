#!/usr/bin/python

import os, sys

appListFileName = 'app.lst'

# check if files exist
if os.path.isfile(appListFileName) == False:
 	print str(appListFileName) +  " doesn't exist"
	sys.exit(0)

appListFile = open(appListFileName,"r")
appList = appListFile.readlines()

for app in appList:
	for x in range(0, 30):
		print("rm -rf " + str(x) + app.strip('\n'))
		os.system("rm -rf " + str(x) + app.strip('\n'))
