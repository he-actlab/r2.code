#!/usr/bin/python

import os,sys,time

sysspec = -1
if len(sys.argv) != 3:
	print "Usage: ./optimize.py [benchname] [0|1|2|3]"
	print "[0|1|2|3] are representing system specifications of low, medium, high, and aggresive respectively."
	sys.exit()
else:
	sysspec = int(sys.argv[2])
	if sysspec != 0 and sysspec != 1 and sysspec != 2 and sysspec != 3:
		print "Usage: ./optimize.py [benchname] [0|1|2|3]"
		print "[0|1|2|3] are representing system specifications of low, medium, high, and aggresive respectively."
		sys.exit()

bench = sys.argv[1]
benchpath = os.environ['R2_BENCH'] + '/' + bench
if os.path.exists(benchpath) == False:
	print "path[" + benchpath + "] doens't exist"
	sys.exit()

print
print " <<<<< stochastic optimization start <<<<< "
print
os.system('cd ../r2.optimization ; ./NewGeneticParallel.py ' + bench + ' ' + str(sysspec))
print
print " >>>>> stochastic optimization end >>>>> "
