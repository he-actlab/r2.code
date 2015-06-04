#!/usr/bin/python

import os, sys

parallelism = 30

specKinds = ['low','med','fd','high']

for spec in specKinds:
	for x in range(0, parallelism):
		specName = spec.strip('\n')
		#print "cp energy_chart_" + specName + ".py.template energy_chart_" + specName + str(x) + ".py"
		#print "sed -i'' -e \"s/num = -1/num = " + str(x) + "/g\" energy_chart_" + specName + str(x) + ".py"
		os.system("cp energy_chart_" + specName + ".py.template energy_chart_" + specName + str(x) + ".py")
		os.system("sed -i'' -e \"s/num = -1/num = " + str(x) + "/g\" energy_chart_" + specName + str(x) + ".py")
		#print "cp collect_" + specName + ".py.template collect_" + specName + str(x) + ".py"
		#print "sed -i'' -e \"s/num = -1/num = " + str(x) + "/g\" collect_" + specName + str(x) + ".py"
		os.system("cp collect_" + specName + ".py.template collect_" + specName + str(x) + ".py")
		os.system("sed -i'' -e \"s/num = -1/num = " + str(x) + "/g\" collect_" + specName + str(x) + ".py")
os.system('rm *-e')
