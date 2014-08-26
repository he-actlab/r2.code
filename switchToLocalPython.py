#!/usr/bin/python

import os, sys

directories = ['r2.optimization', 'r2.apps']

newPythonPath = os.environ['RESEARCH'] + '/r2.code/Python-2.7.5/python' 
_newPythonPath = ''
for ch in newPythonPath:
	if ch == '/':
		_newPythonPath += '\/'
	else:
		_newPythonPath += ch

for directory in directories:
	print("sed -i 's/\/usr\/bin\/python/" + _newPythonPath  + "/g' ./" + directory  +  "/*.py")
	print("sed -i 's/\/usr\/bin\/python/" + _newPythonPath  + "/g' ./" + directory  +  "/*.py.template")
	print("sed -i 's/\/usr\/local\/bin\/python2.7/" + _newPythonPath  + "/g' ./" + directory  +  "/*.py.template")
	os.system("sed -i 's/\/usr\/bin\/python/" + _newPythonPath  + "/g' ./" + directory  +  "/*.py")
	os.system("sed -i 's/\/usr\/bin\/python/" + _newPythonPath  + "/g' ./" + directory  +  "/*.py.template")
	os.system("sed -i 's/\/usr\/local\/bin\/python2.7/" + _newPythonPath  + "/g' ./" + directory  +  "/*.py.template")
