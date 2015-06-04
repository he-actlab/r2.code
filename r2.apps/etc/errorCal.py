#!/usr/bin/python

import os,sys

#benchmarks = ['simpleRaytracer', 'FFT', 'LU', 'SOR', 'SMM', 'MonteCarlo', 'jmeint', 'sobel', 'zxing' ]
benchmarks = ['SOR']
levels = ['low', 'med', 'fd', 'high']

for bench in benchmarks:
	print 'Bench[' + bench + ']'
	for level in levels:
		print 'Level[' + level + ']'
		tmpErrSum = 0.0
		tmpEnerSum = 0.0
		for i in range(0, 10):
			if bench == 'simpleRaytracer':
				process = os.popen('./collect_' + level + '.py ' + bench + ' 3 2 30 10 | grep level | awk \'{print $3}\'')
			else:
				process = os.popen('./collect_' + level + '.py ' + bench + ' 19 | grep level | awk \'{print $3}\'')
			result = process.read()
			process.close()
			print result
			tmpErrSum = tmpErrSum + float(result.strip('\n'))
			if bench == 'jmeint':
				process = os.popen('./energy_chart_' + level + '.py ' + bench + '| grep jME | grep -v jME-B | awk \'{print $2}\'')
			if bench == 'sobel':
				print('./energy_chart_' + level + '.py ' + bench + '| grep Sobel | grep -v Sobel-B | awk \'{print $2}\'')
				process = os.popen('./energy_chart_' + level + '.py ' + bench + '| grep Sobel | grep -v Sobel-B')
			if bench == 'zxing':
				process = os.popen('./energy_chart_' + level + '.py ' + bench + '| grep ZXing | grep -v ZXing-B | awk \'{print $2}\'')
			if bench == 'simpleRaytracer':
				process = os.popen('./energy_chart_' + level + '.py ' + bench + '| grep Raytracer | grep -v Raytracer-B | awk \'{print $2}\'')
			else:
				process = os.popen('./energy_chart_' + level + '.py ' + bench + '| grep ' + bench + ' | grep -v ' + bench + '-B | awk \'{print $2}\'')
			result = process.read()
			process.close()
			print result
			tmpEnerSum = tmpEnerSum + float(result.strip('\n'))

		print 'bench: ' + bench + '  level: ' + level + '  error: ' + str(tmpErrSum / 10.0)
		print 'bench: ' + bench + '  level: ' + level + '  energy: ' + str(tmpEnerSum / 10.0)
	print 

