#!/usr/bin/python

import os, sys, os.path

if len(sys.argv) != 3:
	print "Usage: ./mark.py [quad list] [joeq bytecode]"
	sys.exit(0)

quadList = sys.argv[1]
joeqByteCode = sys.argv[2]

# check if files exist
if os.path.isfile(quadList) == False:
	print str(quadList) +  " doesn't exist"
	sys.exit(0)
if os.path.isfile(joeqByteCode) == False:
	print str(joeqByteCode) + " doesn't exist"
	sys.exit(0)

quadListFile = open(quadList,"r")
lines = quadListFile.readlines()

for line in lines:
	line = line.strip('\n')
	tokens = line.split(' ')
	line = line.strip(tokens[0])
	quad = line.split(")")[1].strip(' [').strip(']')
	if "-1: " in quad:
		continue
	loc = tokens[1].strip('()')
	os.system('cat ' + joeqByteCode + ' | grep \"' + quad + '\"')
	temp = ''
	for ch in loc:
		if ch == '/':
			temp += '\/'
		else:
			temp += ch
	loc = temp
	os.system ('sed -i -e \'s/' + quad + '/' + quad + '					### ' + loc + '/g\' ' + joeqByteCode)

os.system('rm ' + str(joeqByteCode) + '-e')
