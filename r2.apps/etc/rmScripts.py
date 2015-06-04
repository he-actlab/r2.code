#!/usr/bin/python

import os, sys

parallelism = 30

specKinds = ['low','med','fd','high']

for spec in specKinds:
	for x in range(0, parallelism):
		#print("rm energy_chart_" + spec + str(x) + ".py")
		os.system("rm energy_chart_" + spec + str(x) + ".py")
		#print("rm collect_" + spec + str(x) + ".py")
		os.system("rm collect_" + spec + str(x) + ".py")
