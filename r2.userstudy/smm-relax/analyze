#!/usr/bin/python

import os,sys,time

os.system("rm -rf src-marked")
print
print " <<<<< [1] compilation start <<<<< "
print
os.system("ant")
print
print " >>>>> [1] compilation end >>>>> "
p = os.popen("pwd | awk -F'/' '{print $7}'")
result = p.read()
os.system("cd ../../expax.analysis; ./runpl-us.sh " + result.strip('\n') + " > /dev/null")
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
p = os.popen("ls markJava.py")
result = p.read()
if result.strip('\n') != "markJava.py":
	os.system("ln -s ../markJava.py markJava.py")
os.system("cp -r src src-marked")
os.system("./markJava.py after &")
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
