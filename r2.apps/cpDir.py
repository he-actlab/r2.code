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
		appName = app.strip('\n')
		print "rm -rf " + str(x) + appName
		os.system("rm -rf " + str(x) + appName)
		print "cp -r " + appName + " " + str(x) + appName
		os.system("cp -r " + appName + " " + str(x) + appName)
