#!/usr/bin/python

import os, sys

extradir = os.environ['CHORD_MAIN'] + '/../extra'
benchdir = '/Users/jspark/projects/r2.large-benchmarks/boofcv/examples/'
benchpath = benchdir
cmd = 'ant -Dchord.work.dir=' + benchpath + ' -Dchord.scope.reuse=true -Dchord.methods.file=' \
		+ benchpath + 'chord_output/methods.txt -Dchord.reflect.file=' + benchpath \
		+ 'chord_output/reflect.txt -Dchord.check.exclude= -Dchord.run.analyses=cipa-0cfa-dlog,src-files-java run'
print('./countLOC.sh "' + cmd + '"')
os.system('./countLOC.sh "' + cmd + '"')
textpath = benchpath + 'chord_output/ouptut.txt'
os.system('cat ' + benchpath + 'chord_output/log.txt | grep FILE > ' + textpath)
os.system('java LOCCounter ' + textpath + ' ' + benchpath + '/src ../main/ip/src/')

