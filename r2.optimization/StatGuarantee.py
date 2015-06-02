#!/usr/bin/python

import sys
import os
import re
import shutil
import commands
import random
import math
from collections import namedtuple
from operator import itemgetter

ERR_WEIGHT = 0.5
ENR_WEIGHT = 0.5

ScoreInfo = namedtuple('ScoreInfo', 'score index')

class Status:
    PRECISE = 1
    APPROX = 2
    CHANGED_PRECISE = 3

class StatGuarantee(object):

    def __init__(self):
        self.nOps = 0
        self.error = []
        self.energy = []
        self.cnt = []
        self.score = []
        self.approx = {}
        self.approx['high'] = []
        self.approx['mid'] = []
        self.approx['low'] = []
        self.approxCount = {}
        self.approxCount['high'] = 0
        self.approxCount['mid'] = 0
        self.approxCount['low'] = 0
        self.prevChange = {}
        self.prevChange['high'] = []
        self.prevChange['mid'] = []
        self.prevChange['low'] = []
        self.prevChangeCount = {}
        self.prevChangeCount['high'] = 0
        self.prevChangeCount['mid'] = 0
        self.prevChangeCount['low'] = 0
        
    def createInfo(self, nOps=0):
        self.nOps = nOps
        for i in range(0, nOps):
            self.error.append(0.0)
            self.energy.append(0.0)
            self.cnt.append(0)
            self.approx.get('high').append(Status.PRECISE)
            self.approx.get('mid').append(Status.PRECISE)
            self.approx.get('low').append(Status.PRECISE)
            self.prevChange.get('high').append(True)
            self.prevChange.get('mid').append(True)
            self.prevChange.get('low').append(True)
        self.approxCount['high'] = 0
        self.approxCount['mid'] = 0
        self.approxCount['low'] = 0
        
            
    def updateInfo(self, bitvector, error, energy):
        
        totalOnes = 0
        for ch in bitvector:
            if ch == '1':
                totalOnes += 1
                
        index = 0
        for ch in bitvector:
            if ch == '1':
                self.cnt[index] += 1
                self.error[index] += error / float(totalOnes) # error rate
                self.energy[index] += energy / float(totalOnes) # energy gain
            index += 1
    
    def sortOps(self):
        #for i in range(0, self.nOps):
        #    if self.cnt[i] != 0:
        #        self.error[i] = self.error[i] / self.cnt[i]
        #        self.energy[i] = self.energy[i] / self.cnt[i]
        #        self.score.append(ScoreInfo(self.error[i] * ERR_WEIGHT + self.energy[i] * ENR_WEIGHT, i))       # the more score has, the higher risk to approixmate  
        #    else:
        #        self.score.append(ScoreInfo(0.0, i)) 

        # Just put random number into the score vector
        for i in range(0, self.nOps):
            self.score.append(ScoreInfo(random.random(),i))
        self.score = sorted(self.score, key=itemgetter(0), reverse=True)
      
    def zeroBitNum(self, bitvector):
        num = 0
        for ch in bitvector:
            if ch == '0':
                num += 1
        return num
      
    def initPrevChange(self, level):
        for i in range(0, self.nOps):
            self.prevChange.get(level)[i] = True
        self.prevChangeCount[level] = 0
      
    def getOneThirdGene(self, gene, level):
        
        newGeneArr = []
        for bit in gene[0]:
            newGeneArr.append(bit)        

        indexSet = []
        self.approxCount[level] = 0
        for i in range(0, self.nOps):
            if newGeneArr[self.score[i].index] == '1': 
                indexSet.append(i)
                self.approx.get(level)[i] = Status.APPROX
                self.approxCount[level] += 1
    
        subset = []
        if level == 'high':
            for i in range(0, int(math.ceil(float(len(indexSet)) * (1.0/3.0)))):
                subset.append(indexSet[i])
        elif level == 'mid':
            for i in range(int(math.ceil(float(len(indexSet)) * (1.0/3.0))), int(math.ceil(float(len(indexSet)) * (2.0/3.0)))):
                subset.append(indexSet[i])
        else:
            for i in range(int(math.ceil(float(len(indexSet)) * (2.0/3.0))), len(indexSet)):
                subset.append(indexSet[i])
                
        self.initPrevChange(level)
        for i in subset:
            index = self.score[i].index
            newGeneArr[index] = '0'
            self.approx.get(level)[i] = Status.CHANGED_PRECISE
            self.prevChange.get(level)[i] = False
            self.approxCount[level] = self.approxCount.get(level) - 1
            self.prevChangeCount[level] = self.prevChangeCount.get(level) + 1
        
        newGene = ''
        for bit in newGeneArr:
            newGene += bit

        return [newGene, self.zeroBitNum(newGene), 0, 0, 0]
    
    # TODO update increase/decrease
    def getUpdatedGene(self, gene, level, increase, logfile):
        
        newGeneArr = []
        for bit in gene[0]:
            newGeneArr.append(bit)
        
        if increase == True:
            self.initPrevChange(level)
            nChange = int(math.ceil(float(self.approxCount.get(level)) / 2.0))
            logfile.write("[i] nChange: " + str(nChange))
            for i in range(0, self.nOps):
                if self.approx.get(level)[i] == Status.APPROX:
                    index = self.score[i].index
                    if newGeneArr[index] == '1':
                        newGeneArr[index] = '0'
                    self.approx.get(level)[i]  = Status.CHANGED_PRECISE
                    self.prevChange.get(level)[i] = False
                    self.approxCount[level] = self.approxCount.get(level) - 1
                    self.prevChangeCount[level] = self.prevChangeCount.get(level) + 1
                    nChange -= 1
                    if nChange == 0:
                        break
        else:
            nChange = int(math.ceil(float(self.prevChangeCount.get(level)) / 2.0))
            logfile.write("[d] nChange: " + str(nChange))
            for i in range(self.nOps - 1, -1, -1):
                if self.prevChange.get(level)[i] == False:
                    index = self.score[i].index
                    if newGeneArr[index] == '0':
                        newGeneArr[index] = '1'
                    self.approx.get(level)[i] = Status.APPROX
                    self.prevChange.get(level)[i] = True
                    self.approxCount[level] = self.approxCount.get(level) + 1
                    self.prevChangeCount[level] = self.prevChangeCount.get(level) - 1
                    nChange -= 1
                    if nChange == 0:
                        break
                
        newGene = ''
        for bit in newGeneArr:
            newGene += bit
        
        return [newGene, self.zeroBitNum(newGene), 0, 0, 0]
        
        
        
        
        