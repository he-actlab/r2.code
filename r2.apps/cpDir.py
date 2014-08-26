#!/usr/bin/python

import os, sys

parallelism = 30

if len(sys.argv) == 1:
	appListFileName = 'app.lst'

	# check if files exist
	if os.path.isfile(appListFileName) == False:
		print str(appListFileName) +  " doesn't exist"
		sys.exit(0)
	appListFile = open(appListFileName,"r")
	appList = appListFile.readlines()
	for app in appList:
		for x in range(0, parallelism):
			appName = app.strip('\n')
			#print "rm -rf " + str(x) + appName
			os.system("rm -rf " + str(x) + appName)
			#print "cp -r " + appName + " " + str(x) + appName
			os.system("cp -r " + appName + " " + str(x) + appName)
else:
	appName = sys.argv[1]
	if os.path.exists(appName) == False:
		print str(appName) +  " doesn't exist"	
		sys.exit(0)
	for x in range(0, parallelism):
		#print "rm -rf " + str(x) + appName
		os.system("rm -rf " + str(x) + appName)
		#print "cp -r " + appName + " " + str(x) + appName
		os.system("cp -r " + appName + " " + str(x) + appName)



