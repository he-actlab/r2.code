#!/usr/bin/python

import os, sys, time

def printUsage():
	print "USAGE: ./exp.py [bench] [serial/parallel]"

def main():

	argLen = len(sys.argv)
	if argLen != 3:
		printUsage()
		sys.exit()

	if sys.argv[2] != 'serial' and sys.argv[2] != 'parallel':
		printUsage()
		sys.exit()

	if sys.argv[1] != 'FFT' and sys.argv[1] != 'SOR' and sys.argv[1] != 'MonteCarlo' and sys.argv[1] != 'SMM' and sys.argv[1] != 'LU' and sys.argv[1] != 'zxing' and sys.argv[1] != 'jmeint' and sys.argv[1] != 'imagefill' and sys.argv[1] != 'simpleRaytracer':
		printUsage()
		sys.exit()

	sp = sys.argv[2]
	bench = sys.argv[1]

	if sp == 'serial':
		#serial execution 
		print "serial " + bench + " start"
		start = time.time()
		os.system("./genetic.py " + bench + " > " + sp + "_" + bench)
		end = time.time()
		os.system("echo " + sp + "_" + bench + " >> time_" + sp + "_" + bench)
		os.system("echo " + str(end - start) + " >> time_" + sp + "_" + bench)

	if sp == 'parallel':
		# parallel execution
		print "parallel " + bench + " start"
		start = time.time()
		os.system("./genetic_parallel.py " + bench + " > " + sp + "_" + bench)
		end = time.time()
		os.system("echo " + sp + "_" + bench + " >> time_" + sp + "_" + bench)
		os.system("echo " + str(end - start) + " >> time_" + sp + "_" + bench)

if __name__ == '__main__':
	main()
