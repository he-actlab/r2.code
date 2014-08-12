#!/usr/bin/python

import os, sys

if len(sys.argv) != 2:
	print 'Usage: ./countLOC.py [bench]'
	sys.exit(0)

extradir = os.environ['CHORD_MAIN'] + '/../extra'
benchdir = os.environ['EXPAX_BENCH'] + '/'
bench = sys.argv[1]
benchpath = benchdir + bench + '/'
cmd = 'ant -Dchord.work.dir=' + benchpath + ' -Dchord.scope.reuse=true -Dchord.methods.file=' \
		+ benchpath + 'chord_output/methods.txt -Dchord.reflect.file=' + benchpath \
		+ 'chord_output/reflect.txt -Dchord.check.exclude= -Dchord.run.analyses=cipa-0cfa-dlog,src-files-java run'
print('./countLOC.sh "' + cmd + '"')
os.system('./countLOC.sh "' + cmd + '"')
textpath = benchpath + 'chord_output/' + bench + '.txt'
os.system('cat ' + benchpath + 'chord_output/log.txt | grep FILE > ' + textpath)
if bench != 'zxing':
	os.system('cd ' + benchdir + '; java LOCCounter ' + textpath + ' ' + benchpath + '/src')
else:
	os.system('cd ' + benchdir + '; java LOCCounter ' + textpath + ' ' + benchpath + 'core/src' + ' ' + benchpath + 'javase/src')