#!/usr/bin/python

import os,sys,time
from colorama import init
from colorama import Fore, Back, Style

init()

os.system("rm -rf src-marked")
print
print Fore.MAGENTA + " <<<<< [1] compilation start <<<<< " + Fore.RESET
os.system("./clean.sh > /dev/null")
os.system("echo false > analysis.flag")
result = os.popen("./build.sh 2> /dev/null").readlines()
for line in result:
	if "error" in line:
		for line2 in result:
			print '   ' + line2.strip('\n')	 
		print Fore.MAGENTA + " >>>>> [1] compilation end >>>>> " + Fore.RESET
		sys.exit(0);
print "   BUILD SUCCESSFUL"
print Fore.MAGENTA + " >>>>> [1] compilation end >>>>> " + Fore.RESET
p = os.popen("pwd | awk -F'/' '{print $7}'")
result = p.read()
os.system("cd $R2_ANALYSIS; ./runpl.sh " + result.strip('\n') + " > /dev/null 2> /dev/null")
print
print Fore.GREEN + " <<<<< [2] r2 analysis start <<<<< " + Fore.RESET
time.sleep(1)
while True:
	print "   analyzing ... "
	p = os.popen("ps -ef | grep chord.run.analyses | grep -v grep | wc -l")
	result = p.read()
	if int(result.strip('\n')) == 0:
		break
	time.sleep(3)
print Fore.GREEN + " >>>>> [2] r2 analysis end >>>>> " + Fore.RESET
print
print Fore.CYAN + " <<<<< [3] back annotating start <<<<< " + Fore.RESET
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
print Fore.CYAN + " >>>>> [3] back annotating end >>>>> " + Fore.RESET
print

#print
#print Fore.MAGENTA + " <<<<< [4] re-compilation start <<<<< " + Fore.RESET
#print
#os.system('ant clean')
#os.system('ant')
#print
#print Fore.MAGENTA + " >>>>> [4] re-compilation end >>>>> " + Fore.RESET
#print
