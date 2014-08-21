#!/usr/bin/python

import os,sys,time

if len(sys.argv) != 2:
	print "Usage: ./analyze.py [benchname]"
	sys.exit()

bench = sys.argv[1]
benchpath = os.environ['R2_BENCH'] + '/' + bench
if os.path.exists(benchpath) == False:
	print "path[" + benchpath + "] doens't exist"
	sys.exit()

os.system('cd ' + benchpath + '; rm -rf src-marked')
print
print " <<<<< [1] compilation start <<<<< "
print
os.system('echo false > ' + benchpath + '/analysis.flag')
os.system('cd ' + benchpath + '; ant')
print
print " >>>>> [1] compilation end >>>>> "
os.system("cd ../r2.analysis ; ./runpl.sh " + bench + " > /dev/null")
print
print " <<<<< [2] relax analysis start <<<<< "
time.sleep(1)
while True:
	print "   analyzing ... "
	p = os.popen("ps -ef | grep chord.run.analyses | grep -v grep | wc -l")
	result = p.read()
	if int(result.strip('\n')) == 0:
		break
	time.sleep(3)
print " >>>>> [2] relax analysis end >>>>> "
print
print " <<<<< [3] back annotating start <<<<< "
os.system('cd ' + benchpath + '; cp -r src src-marked')
os.system('cd ' + benchpath + '; ./markJava.py after &')
time.sleep(1)
while True:
	print "   back annotating ... "
	p = os.popen("ps -ef | grep markJava.py | grep -v grep | wc -l")
	result = p.read()
	if int(result.strip('\n')) == 0:
		break
	time.sleep(3)
print " >>>>> [3] back annotating end >>>>> "
print
