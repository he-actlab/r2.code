#!/usr/bin/python

import os,sys

if len(sys.argv) != 2:
	print 'USAGE: ./backup.py [username]'
	sys.exit()

username = sys.argv[1]

os.system('cp -r sor-enerj backup/sor-enerj-' + username)
os.system('cp -r sor-r2 backup/sor-r2-' + username)
os.system('cp -r smm-enerj backup/smm-enerj-' + username)
os.system('cp -r smm-r2 backup/smm-r2-' + username)
os.system('cp -r fft-enerj backup/fft-enerj-' + username)
os.system('cp -r fft-r2 backup/fft-r2-' + username)

