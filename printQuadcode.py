#!/usr/bin/python

import os, sys

homedir = os.environ['EXPAX']

if len(sys.argv) != 2:
	print 'Usage: ./runExpax.py [bench]'
	sys.exit(0)

bench = sys.argv[1]
appListFilePath = 'apps/app.lst'
classesFilePath = 'apps/classes'

if os.path.isfile(appListFilePath) == False:
	print str(appListFilePath) + " doesn't exist"
	sys.exit(0)
appListFile = open(appListFilePath,"r")
appList = appListFile.readlines()

if os.path.isfile(classesFilePath) == False:
	print str(classesFilePath) + " doesn't exist"
	sys.exit(0)
classesFile = open(classesFilePath,"r")
classesLines = classesFile.readlines() 

for app in appList:
	if bench == app.strip('\n'):
		classes=''
		for line in classesLines:
			tokens = line.split()
			if tokens[0] == bench:
				classes = tokens[1]
		if classes == '':
			print 'Error! ' + classesFilePath + ' does not include an entry for ' + bench
			sys.exit(0)
		outfileName = bench + "txt"
		cmd = "ant -Dchord.work.dir=" + homedir + "/apps/" \
				+ bench + " -Dchord.ssa.kind=nophi " + \
				"-Dchord.print.classes=" + \
				classes + \
				" -Dchord.verbose=1 -Dchord.out.file=" + \
				outfileName + " run"
		print cmd
		os.system(cmd)
		sys.exit(0)

print appListFilePath  + ' does not include ' + bench

