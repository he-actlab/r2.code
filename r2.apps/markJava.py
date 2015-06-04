#!/usr/bin/python

import os, sys, os.path

if len(sys.argv) != 2:
	print "Usage: ./markJava.py [quad list]"
	sys.exit(0)

quadList = sys.argv[1]

# check if files exist
if os.path.isfile(quadList) == False:
	print str(quadList) +  " doesn't exist"
	sys.exit(0)

quadListFile = open(quadList,"r")
lines = quadListFile.readlines()

logfile = open('markJava.log','w')

cnt = 1
for line in lines:
	line = line.strip('\n')
	tokens = line.split(' ')
	line = line.strip(tokens[0])

	# get quad
	quad = line.split(")")[1].strip(' [').strip(']')
	if "-1: " in quad:
		logfile.write(quad + '\n')
		logfile.write('\n')
		cnt += 1
		continue

	field=""
	if "NEW" in line:
		tokens = line.split(' ')
		field = tokens[len(tokens)-1]

	# get filename for the location
	loc = tokens[1].strip('()')
	tokens = loc.split(':')
	loc = tokens[0]
	lineNum = tokens[1]
	temp = ''
	for ch in loc:
		if ch == '/':
			temp += '\/'
		else:
			temp += ch
	loc = temp
	loc = 'src-marked\/' + loc

	if ("Loosen.java" in loc) or ("Tighten.java" in loc) or ("Tag.java" in loc) or ("ApproxMath.java" in loc) or ("ApproxFloat.java" in loc):
		continue

	# get the content of a certain line of java source code
	p = os.popen ('sed -n \'' + lineNum + 'p\' ' + loc)
	content = p.read().strip('\n')

	# get rid of white spaces in front of actual code
	temp = ''
	started = False
	for ch in content:
		if (ch == ' ' or ch == '\t') and started == False:
			continue
		else:
			started = True
			if ch == '/' or ch == '*' or ch == '[' or ch == ']' or ch == '\"' or ch == '&':
				temp += '\\' + ch
			elif ch == '\'':
				temp += '\'\''
			else:
				temp += ch
	content = temp

	# replace the original line of source code with a comment of quad 
	if "NEW" in quad or "NEW_ARRAY" in quad:
		quad = quad.split(',')[0]
		if field == 'ARRAY':
			newcontent = content + '\t\/\/ st: ' + quad
		else:
			newcontent = content + '\t\/\/ st: ' + quad + ', ' + field
	else:
		newcontent = content + '\t\/\/ op: ' + quad

	# replace the original line of source code with a comment of quad 
	os.system('sed \'' + lineNum + 's/' + content + '/' + newcontent + '/\' ' + loc + ' > ' + loc + '.tmp')

	logfile.write(str(cnt) + '\n')
	p = os.popen ('sed -n \'' + lineNum + 'p\' ' + loc)
	org = p.read().strip('\n')

	p = os.popen ('sed -n \'' + lineNum + 'p\' ' + loc + '.tmp')
	new = p.read().strip('\n')

	if new == '':
		logfile.write(" *** R2: adding annotation failed: " + org + '\n')
		logfile.write(" *** " + quad + "\n")
		logfile.write(" *** " + loc + "\n")
		logfile.write(' *** sed \'' + lineNum + 's/' + content + '/' + newcontent + '/\' ' + loc + ' > ' + loc + '.tmp')
		logfile.write(' *** org: ' + org + '\n')
		logfile.write(' *** org: ' + new + '\n')
		cnt += 1
		continue

	logfile.write('\t' + new + '\n')
	os.system('mv ' + loc + '.tmp ' + loc) 
	cnt += 1


