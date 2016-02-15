#!/usr/bin/python

import os,sys,time
import Inputs
from colorama import init
from colorama import Fore, Back, Style

init()

if len(sys.argv) != 2:
	print
	print "Usage: ./runsimulation.py [aggressive|high|medium|low]"
	print
	print "Options: approximate system model; should pick one of the models"
	print
	sys.exit(0)

system = sys.argv[1]

if system != "aggressive" and system != "high" and system != "medium" and system != "low":
	print
	print "   Error: system model should be either aggressive, high, medium, or low"
	print
	sys.exit(0)	

if system == "aggressive":
	system = "high"
if system == "high":
	system = "fd"
if system == "medium":
	system = "med"

p = os.popen("pwd | awk -F'/' '{print $NF}'")
result = p.read()
bench = result.strip('\n')

print
print Fore.MAGENTA + " <<<<< [1] generating binary start <<<<< " + Fore.RESET
result = os.popen("ant 2> /dev/null").readlines()
for line in result:
	if "error" in line:
		for line2 in result:
			print '   ' + line2.strip('\n')	 
		print Fore.MAGENTA + " >>>>> [1] generating binary end >>>>> " + Fore.RESET
		sys.exit(0);
print "   BUILD SUCCESSFUL"
print Fore.MAGENTA + " >>>>> [1] generating binary end >>>>> " + Fore.RESET

print
print Fore.GREEN + " <<<<< [2] measuring quality degradation start <<<<< " + Fore.RESET

# Single input data for test purpose
if bench != 'simpleRaytracer':
	result = os.popen("cd ..; ./collect_" + system + ".py " + bench + " " + Inputs.BenchInputs[bench].testInputs[0]) 
else:
	inputArgs = Inputs.BenchInputs[bench].testInputs[0]
	argStr = ""
	for arg in inputArgs:
		argStr += str(arg) + " "
	result = os.popen("cd ..; ./collect_" + system + ".py " + bench + " " + argStr)

for line in result:
	if "level" in line:
		error = 100 * float((line.strip('\n')).split()[2])
		print "   Quality degradation: " + str(error) + "%"
print Fore.GREEN + " <<<<< [2] measuring quality degradation end <<<<< " + Fore.RESET

print
print Fore.CYAN + " <<<<< [3] measuring energy start <<<<< " + Fore.RESET
result = os.popen("cd ..; ./energy_chart_" + system + ".py " + bench)
baseline = 0.0
saved = 0.0
for line in result:
	if "-B" in line:
		baseline = 100 * float((line.strip('\n')).split()[1])
	if system == "high":
		if "-4" in line:
			saved = 100 * float((line.strip('\n')).split()[1])
	if system == "fd":
		if "-3" in line:
			saved = 100 * float((line.strip('\n')).split()[1])
	if system == "med":
		if "-2" in line:
			saved = 100 * float((line.strip('\n')).split()[1])
	if system == "low":
		if "-1" in line:
			saved = 100 * float((line.strip('\n')).split()[1])
print "   Energy saving: " + str(baseline - saved) + "%"
print Fore.CYAN + " <<<<< [3] measuring energy end <<<<< " + Fore.RESET
print


