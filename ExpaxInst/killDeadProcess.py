#!/usr/bin/python

import time
import os, sys, subprocess
from collections import namedtuple

ProcessInfo = namedtuple('ProcessInfo', 'pid age')
prev = set()
live = set()
age = {}

command = "ps -ef | grep java | grep jsr308 | grep -v grep | awk '{print $2}'"
# command = "ps -ef | grep Google | awk '{print $2}'"

stop = False
while stop == False:
    process = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=None, shell=True)
    outputs = process.communicate()
    outputs = outputs[0].split('\n')
    live.clear()
    for pid in outputs:
        if pid != '':
            live.add(pid)
            if age.get(pid) == None:
                age[pid] = 0
            else:
                age[pid] += 1
                if (age[pid] == 500):
                    os.system('kill -9 ' + str(pid))
                    live.remove(pid)
    dead = prev.difference(live)
    for d in dead:
        age.pop(d)
    prev = live.copy()
    print age
    time.sleep(1)
