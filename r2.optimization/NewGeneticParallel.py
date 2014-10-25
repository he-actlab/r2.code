#!/usr/bin/python

import sys
import os
import re
import shutil
import commands
import random
import math
import Inputs
from StatGuarantee import StatGuarantee 
from operator import itemgetter
from collections import namedtuple
from multiprocessing import Process, Queue

BASE_ADDR = os.environ['RESEARCH'] + '/r2.code'
APP_ADDR = BASE_ADDR + '/r2.apps/'
INSTR_ADDR = BASE_ADDR + '/r2.optimization'

# ***** Parameters *****
NUM_RUNS = 5
# NUM_RUNS = 1
PARALLEL_WIDTH = 2
# PARALLEL_WIDTH = 4
POPULATION_SIZE = 8
# POPULATION_SIZE = 2
MAX_RUNS = 5
# MAX_RUNS = 1
MAX_BINARY_SEARCH_DEPTH = 5
# MAX_BINARY_SEARCH_DEPTH = 1
ERR_EXPECTATION = 5
# ***** Parameters *****

JarInfo = namedtuple('AppInfo', 'prj_name jar_name src_dir dst_dir')
DictInfo = namedtuple('DictInfo', 'error energy')

# Genetic Algorithm Related Data

#SELECTED_SIZE = 100
MUTE_PROB = 0.02
CROSS_PROB = 0.8
# [bv, zeros, err, energy, score]
LGeneration = {}
MGeneration = {}
FGeneration = {}
HGeneration = {}
LBestGene = {}
MBestGene = {}
FBestGene = {}
HBestGene = {}
ERR_WEIGHT = 0.5
ENERGY_WEIGHT = 0.5
ERROR_BAR = 0.0
MAX_RETRY = 100
#
UniqueBitvector = set()
sg = StatGuarantee()
zxingBits =[-1,-1]

BENCHMARK = {
    'fft' : [
        JarInfo('fft', 'fft.jar', 'fft/', 'fft/'),
    ],
    'sor' : [
        JarInfo('sor', 'sor.jar', 'sor/', 'sor/'),
    ],
    'mc' : [
        JarInfo('mc', 'mc.jar', 'mc/', 'mc/'),
    ],
    'smm' : [
        JarInfo('smm', 'smm.jar', 'smm/', 'smm/'),
    ],
    'lu' : [
        JarInfo('lu', 'lu.jar', 'lu/', 'lu/')
    ],
    'zxing' : [
        JarInfo('zxing', 'core.jar', 'zxing/core/', 'zxing/core/'),
        JarInfo('zxing', 'javase.jar', 'zxing/javase/', 'zxing/javase/')
    ],
    'jmeint' : [
        JarInfo('jmeint', 'jmeint.jar', 'jmeint/', 'jmeint/'),
    ],
    'simpleRaytracer' : [
        JarInfo('simpleRaytracer', 'simpleRaytracer.jar', 'simpleRaytracer/', 'simpleRaytracer/'),
    ],
    'sobel' : [
        JarInfo('sobel', 'sobel.jar', 'sobel/', 'sobel/'),
    ]
}

INSTRUMENT = {
    'input_file' : INSTR_ADDR + '/input/old.jar',
    'output_file' : INSTR_ADDR + '/output/new.jar',
    'jar_file' : INSTR_ADDR + '/jars/R2Inst.jar'
}

def zeroBitNum(bitvector):
    num = 0
    for ch in bitvector:
        if ch == '0':
            num += 1
    return num

def generate(name, bit_size):
    LGeneration[name] = []
    MGeneration[name] = []
    FGeneration[name] = []
    HGeneration[name] = []
    for i in range(POPULATION_SIZE):
        unique = False
        while unique == False:
            bitvector = ''
            for i in range(bit_size):
                bitvector += str(random.randrange(0, 2))
            if bitvector not in UniqueBitvector:
                unique = True
                UniqueBitvector.add(bitvector)
#                 print "UniqueBitvector: " + bitvector
        LGeneration[name].append([bitvector, zeroBitNum(bitvector), 0, 0, 0])
        MGeneration[name].append([bitvector, zeroBitNum(bitvector), 0, 0, 0])
        FGeneration[name].append([bitvector, zeroBitNum(bitvector), 0, 0, 0])
        HGeneration[name].append([bitvector, zeroBitNum(bitvector), 0, 0, 0])

def init(name):
    jar_infos = BENCHMARK[name]
    if name == 'zxing':
        bit_size = -1
        index = 0
        for jar_info in jar_infos:
            os.chdir(INSTR_ADDR)
            fp = open('build.properties', 'w')
            fp.write('build.app.name=' + name + '\n')
            fp.write('build.bitvector=' + '\n')    
            fp.write('build.mode=count')
            fp.close()
            path = APP_ADDR + jar_info.src_dir + jar_info.jar_name
            shutil.copy(path,  INSTRUMENT['input_file'])
            cmd = 'ant run'
            (status, output) = commands.getstatusoutput(cmd)
            for line in output.split('\n'):
                if "[java] count=" in line:
                    bit_size = int(line.split('=')[1])
                    zxingBits[index] = bit_size
                    index += 1
                    break
            if bit_size == -1:
                print 'r2 instrumentation does not work properly'
        if bit_size != 0:
            generate(name, bit_size)
            print zxingBits
        else:
            print zxingBits
            sys.exit(1)
    else:
        bit_size = -1
        for jar_info in jar_infos:
            os.chdir(INSTR_ADDR)
            fp = open('build.properties', 'w')
            fp.write('build.app.name=' + name + '\n')
            fp.write('build.bitvector=' + '\n')    
            fp.write('build.mode=count')
            fp.close()
            path = APP_ADDR + jar_info.src_dir + jar_info.jar_name
            shutil.copy(path,  INSTRUMENT['input_file'])
            cmd = 'ant run'
            (status, output) = commands.getstatusoutput(cmd)
            for line in output.split('\n'):
                if "[java] count=" in line:
                    bit_size = int(line.split('=')[1])
                    break
            if bit_size == -1:
                print 'r2 instrumentation does not work properly'
            if bit_size != 0:
                generate(name, bit_size)
            else:
                sys.exit(1);
    sg.createInfo(bit_size)

# only for zxing app
def special_build(name, level, chunk, j):
    jar_infos = BENCHMARK[name]
    i = 0
    round = PARALLEL_WIDTH * chunk + j
    for jar_info in jar_infos:
        os.chdir(INSTR_ADDR)
        fp = open('build.properties', 'w')
        fp.write('build.app.name=' + name + '\n')
        fp.write('build.bitvector=')
        if i == 0:
            if level == 0:
                fp.write(LGeneration[name][round][0][:zxingBits[0]]) 
            elif level == 1:
                fp.write(MGeneration[name][round][0][:zxingBits[0]])
            elif level == 2:
                fp.write(FGeneration[name][round][0][:zxingBits[0]])
            else:
                fp.write(HGeneration[name][round][0][:zxingBits[0]])
        else:
            if level == 0:
                fp.write(LGeneration[name][round][0][zxingBits[0]:])
            elif level == 1:
                fp.write(MGeneration[name][round][0][zxingBits[0]:])
            elif level == 2:
                fp.write(FGeneration[name][round][0][zxingBits[0]:])
            else:
                fp.write(HGeneration[name][round][0][zxingBits[0]:])
        fp.write('\n')
        fp.write('build.mode=inst')
        fp.close()
        path = APP_ADDR + jar_info.src_dir + jar_info.jar_name
        shutil.copy(path,  INSTRUMENT['input_file'])
        cmd = 'ant run'
        (status, output) = commands.getstatusoutput(cmd)
        if status:
            sys.stderr.write(output)
            sys.exit(1)
        path = APP_ADDR + str(j) + jar_info.dst_dir + jar_info.jar_name 
        shutil.copy(INSTRUMENT['output_file'], path)

def build(name, level, chunk, j):
    jar_infos = BENCHMARK[name]
    round = PARALLEL_WIDTH * chunk + j
    for jar_info in jar_infos:
        os.chdir(INSTR_ADDR)
        fp = open('build.properties', 'w')
        fp.write('build.app.name=' + name + '\n')
        fp.write('build.bitvector=')
        if level == 0:
            fp.write(LGeneration[name][round][0])
        elif level == 1:
            fp.write(MGeneration[name][round][0])
        elif level == 2:
            fp.write(FGeneration[name][round][0])
        else:
            fp.write(HGeneration[name][round][0])
        fp.write('\n')
        fp.write('build.mode=inst')
        fp.close()
        path = APP_ADDR + jar_info.src_dir + jar_info.jar_name
        shutil.copy(path,  INSTRUMENT['input_file'])
        cmd = 'ant run'
        (status, output) = commands.getstatusoutput(cmd)
        if status:
            sys.stderr.write(output)
            sys.exit(1)
        path = APP_ADDR + str(j) + jar_info.dst_dir + 'new.jar'
        shutil.copy(INSTRUMENT['output_file'], path) 

def check(output, level):
    is_failed = False
    index = output.find('level ' + str(level))
    if output[index+8] == 'f':
        is_failed = True
    return is_failed

def run(name, level, result, chunk, pid, trainInputs, logQueue):
    os.chdir(APP_ADDR)
    errorSumSum = 0.0
    energySumSum = 0.0
    failed_num = 0
    log = []
    
    if level == 0:
        lStr = 'low'
        findStr = '-1'
    elif level == 1:
        lStr = 'med'
        findStr = '-2'
    elif level == 2:
        lStr = 'fd'
        findStr = '-3'
    else:
        lStr = 'high'
        findStr = '-4'
        
    for trainInput in trainInputs:
        
        errorSum = 0.0
        energySum = 0.0
        error = 0.0
        energy = 0.0
        maxError = 0.0
        minError = 100.0
        maxmin = 2
        
        argument = ""
        
        if name == 'simpleRaytracer':
            for arg in trainInput:
                argument = argument + str(arg) + ' '
        else:
            argument = trainInput
        
        err_cmd = './collect_' + lStr + str(pid) + '.py ' + name  + ' ' + str(argument)
        energy_cmd = './energy_chart_' + lStr + str(pid) + '.py ' + name
        for i in range(NUM_RUNS):
#                 print 'enerJ iteration %d...' % (i+1)
            (status, output) = commands.getstatusoutput(err_cmd)
            if status:
                sys.stderr.write(output)
                sys.exit(1)
            if check(output, level):
                failed_num += 1
                continue
            #print output
            match = re.findall(r'\s[0-1]?.[0-9]+', output)
            #print match[0][1:]
            error = float(match[0][1:]) * 100
            (status, output) = commands.getstatusoutput(energy_cmd)
            if status:
                sys.stderr.write(output)
                sys.exit(1)
            index = output.find(findStr)
            #print output[index+3 : index+10]
            energy = float(output[index+3 : index+10]) * 100
            
            if maxError < error:
                maxError = error
            if minError > error:
                minError = error
            
            errorSum += error
            energySum += energy
            
#             print "[gene " + str(PARALLEL_WIDTH * chunk + pid + 1) + "] try[" + str(i+1) + "] trainInput[" + str(trainInput) + "] error: " + str(error) + " energy: " + str(energy)
            
        error = round((errorSum - maxError - minError)/(NUM_RUNS-failed_num-maxmin), 6)
        energy = round(energySum/(NUM_RUNS-failed_num), 6)
        
        log.append("     [gene " + str(PARALLEL_WIDTH * chunk + pid + 1) + "] trainInput[" + str(trainInput) + "] error: " + str(error) + " energy: " + str(energy) + "\n")
        
        errorSumSum += error
        energySumSum += energy
    
    errorAvg = errorSumSum / float(len(trainInputs))
    energyAvg = energySumSum / float(len(trainInputs))
    
    log.append('\n')
    log.append("          [gene " + str(PARALLEL_WIDTH * chunk + pid + 1) + "] error: " + str(errorAvg) + " energy: " + str(energyAvg) + "\n")
    log.append('\n')
    
    result.put([PARALLEL_WIDTH * chunk + pid, errorAvg, energyAvg])
    logQueue.put(log)

def selection(name, level):
    #diff = POPULATION_SIZE - SELECTED_SIZE
    if level == 0:
        LGeneration[name] = sorted(LGeneration[name], key=itemgetter(4))
        if name in LBestGene:
            if LGeneration[name][0][4] < LBestGene[name][4]:
                LBestGene[name] = LGeneration[name][0]
        else:
            LBestGene[name] = LGeneration[name][0]
        #del LGeneration[name][-diff:]
        #print '[level 0] best gene:', LBestGene[name]
        logfile.write("\n")
        logfile.write('[level 0]:\n')
        for i in range(POPULATION_SIZE):
            logfile.write(str(i+1) + " " + str(LGeneration[name][i]) + "\n")
        logfile.write("\n")
    elif level == 1:
        MGeneration[name] = sorted(MGeneration[name], key=itemgetter(4))
        if name in MBestGene:
            if MGeneration[name][0][4] < MBestGene[name][4]:
                MBestGene[name] = MGeneration[name][0]
        else:
            MBestGene[name] = MGeneration[name][0]
        #del MGeneration[name][-diff:]
        #print '[level 1] best gene:', MBestGene[name]
        logfile.write("\n")
        logfile.write('[level 1]:\n')
        for i in range(POPULATION_SIZE):
            logfile.write(str(i+1) + " " + str(MGeneration[name][i]) + "\n")
        logfile.write("\n")
    elif level == 2:
        FGeneration[name] = sorted(FGeneration[name], key=itemgetter(4))
        if name in FBestGene:
            if FGeneration[name][0][4] < FBestGene[name][4]:
                FBestGene[name] = FGeneration[name][0]
        else:
            FBestGene[name] = FGeneration[name][0]
        #del FGeneration[name][-diff:]
        #print '[level 2] best gene:', FBestGene[name]
        logfile.write("\n")
        logfile.write('[level 2]:\n')
        for i in range(POPULATION_SIZE):
            logfile.write(str(i+1) + " " + str(FGeneration[name][i]) + '\n')
        logfile.write("\n")
    else:
        HGeneration[name] = sorted(HGeneration[name], key=itemgetter(4))
        if name in HBestGene:
            if HGeneration[name][0][4] < HBestGene[name][4]:
                HBestGene[name] = HGeneration[name][0]
        else:
            HBestGene[name] = HGeneration[name][0]
        #del HGeneration[name][-diff:]
        #print '[level 2] best gene:', HBestGene[name]
        logfile.write("\n")
        logfile.write('[level 3]:\n')
        for i in range(POPULATION_SIZE):
            logfile.write(str(i+1) + str(HGeneration[name][i]) + "\n")
        logfile.write("\n")

def cal_prob(list):
    # get the total scores
    total = sum(1/math.sqrt(i) for i in range(1, len(list)+1))
    prob_list = []
    for i in range(1, len(list)+1):
        prob_list.append(1/math.sqrt(i)/total)
    return prob_list

# roulette-wheel selection
#def random_pick(prob_list):
    #for i in range(len(prob_list)):
        #prob_list[i] = int(round(prob_list[i], 2) * 100)
    #select_pool = [data for index, prob in zip(range(len(prob_list)), prob_list) for data in [index] * prob]
    #return random.choice(select_pool)

def random_pick(prob_list):
    prob = random.random()
    for i in range(len(prob_list)):
        if prob_list[i] >= prob:
            return i
        else:
            prob -= prob_list[i]

def mutation(gene):
    bv = gene[0]
    unique = False
    retry = MAX_RETRY
    while unique == False and retry != 0:
        new_bv = ''
        for bit in bv:
            if random.random() <= MUTE_PROB:
                if bit == '0':
                    new_bv += '1'
                else:
                    new_bv += '0'
            else:
                new_bv += bit 
        if new_bv not in UniqueBitvector:
            unique = True
            UniqueBitvector.add(new_bv)
#             print "UniqueBitvector: " + new_bv
        else:
            retry -= 1
    return [new_bv, zeroBitNum(new_bv), 0, 0, 0]


def crossover(f_gene, m_gene):
    pos = random.randrange(0, len(f_gene[0]))
    bv1 = f_gene[0][:pos] + m_gene[0][pos:]
    bv2 = m_gene[0][:pos] + f_gene[0][pos:]
    offSpring1 = [bv1, zeroBitNum(bv1), 0, 0, 0]
    offSpring2 = [bv2, zeroBitNum(bv2), 0, 0, 0]
    return [offSpring1, offSpring2]

def new_generate(name, level):
    new_list = []
    count = POPULATION_SIZE - 1
    if level == 0:
        # always keep best genes
        new_list.append(LGeneration[name][0])
        prob_list = cal_prob(LGeneration[name])
        while count != 0:
            father_index = random_pick(prob_list)
            mother_index = random_pick(prob_list)
            # crossover
            if random.random() <= CROSS_PROB:
                if father_index == mother_index:
                    new_gene = mutation(LGeneration[name][father_index])
                    new_list.append(new_gene)
                    count -= 1
                else:
                    [first_offspring, second_offspring] = crossover(LGeneration[name][father_index], LGeneration[name][mother_index])
                    new_gene = mutation(first_offspring)
                    new_list.append(new_gene)
                    count -= 1
                    if count == 0: break
                    else:
                        new_gene = mutation(second_offspring)
                        new_list.append(new_gene)
                        count -= 1
            else:
                if father_index == mother_index:
                    new_gene = mutation(LGeneration[name][father_index])
                    new_list.append(new_gene)
                    count -= 1
                else:
                    new_gene = mutation(LGeneration[name][father_index])
                    new_list.append(new_gene)
                    count -= 1
                    if count == 0: break
                    else:
                        new_gene = mutation(LGeneration[name][mother_index])
                        new_list.append(new_gene)
                        count -= 1
        del LGeneration[name]
        LGeneration[name] = new_list
    elif level == 1:
        new_list.append(MGeneration[name][0])
        prob_list = cal_prob(MGeneration[name])
        while count != 0:
            father_index = random_pick(prob_list)
            mother_index = random_pick(prob_list)
            if random.random() <= CROSS_PROB:
                if father_index == mother_index:
                    new_gene = mutation(MGeneration[name][father_index])
                    new_list.append(new_gene)
                    count -= 1
                else:
                    [first_offspring, second_offspring] = crossover(MGeneration[name][father_index], MGeneration[name][mother_index])
                    new_gene = mutation(first_offspring)
                    new_list.append(new_gene)
                    count -= 1
                    if count == 0: break
                    else:
                        new_gene = mutation(second_offspring)
                        new_list.append(new_gene)
                        count -= 1
            else:
                if father_index == mother_index:
                    new_gene = mutation(MGeneration[name][father_index])
                    new_list.append(new_gene)
                    count -= 1
                else:
                    new_gene = mutation(MGeneration[name][father_index])
                    new_list.append(new_gene)
                    count -= 1
                    if count == 0: break
                    else:
                        new_gene = mutation(MGeneration[name][mother_index])
                        new_list.append(new_gene)
                        count -= 1
        del MGeneration[name]
        MGeneration[name] = new_list
    elif level == 2:
        new_list.append(FGeneration[name][0])
        prob_list = cal_prob(FGeneration[name])
        while count != 0:
            father_index = random_pick(prob_list)
            mother_index = random_pick(prob_list)
            if random.random() <= CROSS_PROB:
                if father_index == mother_index:
                    new_gene = mutation(FGeneration[name][father_index])
                    new_list.append(new_gene)
                    count -= 1
                else:
                    [first_offspring, second_offspring] = crossover(FGeneration[name][father_index], FGeneration[name][mother_index])
                    new_gene = mutation(first_offspring)
                    new_list.append(new_gene)
                    count -= 1
                    if count == 0: break
                    else:
                        new_gene = mutation(second_offspring)
                        new_list.append(new_gene)
                        count -= 1
            else:
                if father_index == mother_index:
                    new_gene = mutation(FGeneration[name][father_index])
                    new_list.append(new_gene)
                    count -= 1
                else:
                    new_gene = mutation(FGeneration[name][father_index])
                    new_list.append(new_gene)
                    count -= 1
                    if count == 0: break
                    else:
                        new_gene = mutation(FGeneration[name][mother_index])
                        new_list.append(new_gene)
                        count -= 1
        del FGeneration[name]
        FGeneration[name] = new_list
    else:
        new_list.append(HGeneration[name][0])
        prob_list = cal_prob(HGeneration[name])
        while count != 0:
            father_index = random_pick(prob_list)
            mother_index = random_pick(prob_list)
            if random.random() <= CROSS_PROB:
                if father_index == mother_index:
                    new_gene = mutation(HGeneration[name][father_index])
                    new_list.append(new_gene)
                    count -= 1
                else:
                    [first_offspring, second_offspring] = crossover(HGeneration[name][father_index], HGeneration[name][mother_index])
                    new_gene = mutation(first_offspring)
                    new_list.append(new_gene)
                    count -= 1
                    if count == 0: break
                    else:
                        new_gene = mutation(second_offspring)
                        new_list.append(new_gene)
                        count -= 1
            else:
                if father_index == mother_index:
                    new_gene = mutation(HGeneration[name][father_index])
                    new_list.append(new_gene)
                    count -= 1
                else:
                    new_gene = mutation(HGeneration[name][father_index])
                    new_list.append(new_gene)
                    count -= 1
                    if count == 0: break
                    else:
                        new_gene = mutation(HGeneration[name][mother_index])
                        new_list.append(new_gene)
                        count -= 1
        del HGeneration[name]
        HGeneration[name] = new_list

def geneticAlgorithm(name, level, testInputs):
    logfile.write('error level %d...\n' % level)
    print('error level %d...' % level)
    for i in range(MAX_RUNS):
        logfile.write('genetic algo iteration %d...\n' % (i+1))
        print('genetic algo iteration %d...' % (i+1))
        for chunk in range(0, (POPULATION_SIZE / PARALLEL_WIDTH) + 1):
            logfile.write('chunk %d start ...\n' % (chunk+1))    
            result = Queue()
            logQueue = Queue()
            pool = [Process(target=run, args=(name, level, result, chunk, k, testInputs, logQueue)) for k in range(min(PARALLEL_WIDTH * (chunk+1), POPULATION_SIZE) - PARALLEL_WIDTH * chunk)]
            for j in range(min(PARALLEL_WIDTH * (chunk+1), POPULATION_SIZE) - PARALLEL_WIDTH * chunk):
                logfile.write('candidate gene %d...\n' % (PARALLEL_WIDTH * chunk + j))
                if name == 'zxing':
                    special_build(name, level, chunk, j)
                else:
                    build(name, level, chunk, j)
                pool[j].start()
            for p in pool:
                p.join()
            for j in range(min(PARALLEL_WIDTH * (chunk+1), POPULATION_SIZE) - PARALLEL_WIDTH * chunk):
                log = logQueue.get()
                for line in log:
                    logfile.write(line)
                ret = result.get()
                pid = ret[0]
                error = ret[1]
                energy = ret[2]
                if level == 0:
                    zeroBitNum = LGeneration[name][pid][1]
                    totalLen = len(LGeneration[name][pid][0])
                    normalZero = 100.0 * zeroBitNum / totalLen
                    sg.updateInfo(LGeneration[name][pid][0], error / 100.0, energy / 100.0)
                    LGeneration[name][pid][2] = error
                    LGeneration[name][pid][3] = energy
                    if error <= ERROR_BAR:
                        LGeneration[name][pid][4] = energy * ENERGY_WEIGHT
                    else:
                        LGeneration[name][pid][4] = error * ERR_WEIGHT + energy * ENERGY_WEIGHT
                    #print LGeneration[name]
                elif level == 1:
                    zeroBitNum = MGeneration[name][pid][1]
                    totalLen = len(MGeneration[name][pid][0])
                    normalZero = 100.0 * zeroBitNum / totalLen
                    sg.updateInfo(MGeneration[name][pid][0], error / 100.0, energy / 100.0)
                    MGeneration[name][pid][2] = error
                    MGeneration[name][pid][3] = energy
                    if error <= ERROR_BAR:
                        MGeneration[name][pid][4] = energy * ENERGY_WEIGHT
                    else:
                        MGeneration[name][pid][4] = error * ERR_WEIGHT + energy * ENERGY_WEIGHT 
                    #print MGeneration[name]
                elif level == 2:
                    zeroBitNum = FGeneration[name][pid][1]
                    totalLen = len(FGeneration[name][pid][0])
                    normalZero = 100.0 * zeroBitNum / totalLen
                    sg.updateInfo(FGeneration[name][pid][0], error / 100.0, energy / 100.0)
                    FGeneration[name][pid][2] = error
                    FGeneration[name][pid][3] = energy
                    if error <= ERROR_BAR:
                        FGeneration[name][pid][4] = energy * ENERGY_WEIGHT
                    else:
                        FGeneration[name][pid][4] = error * ERR_WEIGHT + energy * ENERGY_WEIGHT
                    #print FGeneration[name]
                else:
                    zeroBitNum = HGeneration[name][pid][1]
                    totalLen = len(HGeneration[name][pid][0])
                    normalZero = 100.0 * zeroBitNum / totalLen
                    sg.updateInfo(HGeneration[name][pid][0], error / 100.0, energy / 100.0)
                    HGeneration[name][pid][2] = error
                    HGeneration[name][pid][3] = energy
                    if error <= ERROR_BAR:
                        HGeneration[name][pid][4] = energy * ENERGY_WEIGHT 
                    else:
                        HGeneration[name][pid][4] = error * ERR_WEIGHT + energy * ENERGY_WEIGHT 
                    #print HGeneration[name]
        selection(name, level)
        if i == MAX_RUNS - 1: break
        new_generate(name, level) 

def logging(name, level):
    print '%s: ' % name
    if level == 0:
        print 'error_low:', LBestGene[name]
    elif level == 1:
        print 'error_med:', MBestGene[name]
    elif level == 2:
        print 'error_fd', FBestGene[name]
    else:
        print 'error_high:', HBestGene[name]

def printUsage():
    print "Usage: ./NewGeneticParallel.py [bench] [level]"

def zxingGeneBuild(name, gene):
    jar_infos = BENCHMARK[name]
    i = 0
    for jar_info in jar_infos:
        os.chdir(INSTR_ADDR)
        fp = open('build.properties', 'w')
        fp.write('build.app.name=' + name + '\n')
        fp.write('build.bitvector=')
        if i == 0:
            fp.write(gene[0][:zxingBits[0]])
        else:
            fp.write(gene[0][zxingBits[0]:])
        fp.write('\n')
        fp.write('build.mode=inst')
        fp.close()
        path = APP_ADDR + jar_info.src_dir + jar_info.jar_name
        shutil.copy(path,  INSTRUMENT['input_file'])
        cmd = 'ant run'
        (status, output) = commands.getstatusoutput(cmd)
        if status:
            sys.stderr.write(output)
            sys.exit(1)
        path = APP_ADDR + jar_info.dst_dir + jar_info.jar_name 
        shutil.copy(INSTRUMENT['output_file'], path)
        i += 1

def geneBuild(name, gene):
    jar_infos = BENCHMARK[name]
    for jar_info in jar_infos:
        os.chdir(INSTR_ADDR)
        fp = open('build.properties', 'w')
        fp.write('build.app.name=' + name + '\n')
        fp.write('build.bitvector=')
        fp.write(gene[0] + '\n')
        fp.write('build.mode=inst')
        fp.close()
        path = APP_ADDR + jar_info.src_dir + jar_info.jar_name
        shutil.copy(path,  INSTRUMENT['input_file'])
        cmd = 'ant run'
        (status, output) = commands.getstatusoutput(cmd)
        if status:
            sys.stderr.write(output)
            sys.exit(1)
        path = APP_ADDR + jar_info.dst_dir + 'new.jar'
        shutil.copy(INSTRUMENT['output_file'], path) 


def oneRun(level, findStr, result, name, lStr, k, trainInput):
    
    argument = ''
    
    if name == 'simpleRaytracer':
        for arg in trainInput:
            argument = argument + str(arg) + ' '
    else:
        argument = trainInput
    
    err_cmd = './collect_' + lStr + str(k) + '.py ' + name  + ' ' + str(argument)
    energy_cmd = './energy_chart_' + lStr + str(k) + '.py ' + name
#   print 'enerJ iteration %d...' % (i+1)
    fail = False
    (status, output) = commands.getstatusoutput(err_cmd)
    if status:
        sys.stderr.write(output)
        sys.exit(1)
    if check(output, level):
        fail = True 
    #print output
    match = re.findall(r'\s[0-1]?.[0-9]+', output)
    #print match[0][1:]
    error = float(match[0][1:]) * 100
    (status, output) = commands.getstatusoutput(energy_cmd)
    if status:
        sys.stderr.write(output)
        sys.exit(1)
    #print output
    index = output.find(findStr)
    #print output[index+3 : index+10]
    energy = float(output[index+3 : index+10]) * 100
    
    result.put([error, energy, fail])


def runGene(name, gene, level, trainInput):
    os.chdir(APP_ADDR)
    failed_num = 0
    
    if level == 0:
        lStr = 'low'
        findStr = '-1'
    elif level == 1:
        lStr = 'med'
        findStr = '-2'
    elif level == 2:
        lStr = 'fd'
        findStr = '-3'
    else:
        lStr = 'high'
        findStr = '-4'
        
    error = 0.0
    energy = 0.0
    errorSum = 0.0 
    energySum = 0.0
    maxError = 0.0
    minError = 100.0
    maxmin = 2
    
    for chunk in range(0, (NUM_RUNS / PARALLEL_WIDTH) + 1):
        logfile.write('chunk %d start ...\n' % (chunk+1))   
        result = Queue()
        pool = [Process(target=oneRun, args=(level, findStr, result, name, lStr, k, trainInput)) for k in range(min(PARALLEL_WIDTH * (chunk+1), NUM_RUNS) - PARALLEL_WIDTH * chunk)]
        for j in range(min(PARALLEL_WIDTH * (chunk+1), NUM_RUNS) - PARALLEL_WIDTH * chunk):
            if name == 'zxing':
                jar_infos = BENCHMARK[name]
                for jar_info in jar_infos:
#                     print 'cp ' + APP_ADDR + jar_info.dst_dir + jar_info.jar_name + ' ' + APP_ADDR + str(j) + jar_info.dst_dir + jar_info.jar_name
                    os.system('cp ' + APP_ADDR + jar_info.dst_dir + jar_info.jar_name + ' ' + APP_ADDR + str(j) + jar_info.dst_dir + jar_info.jar_name)
            else:
                jar_infos = BENCHMARK[name]
                for jar_info in jar_infos:
#                     print 'cp ' + APP_ADDR + jar_info.dst_dir + 'new.jar' + ' ' + APP_ADDR + str(j) + jar_info.dst_dir + 'new.jar'
                    os.system('cp ' + APP_ADDR + jar_info.dst_dir + 'new.jar' + ' ' + APP_ADDR + str(j) + jar_info.dst_dir + 'new.jar')
            pool[j].start()
        for p in pool:
            p.join()
        for j in range(min(PARALLEL_WIDTH * (chunk+1), NUM_RUNS) - PARALLEL_WIDTH * chunk):
            ret = result.get()
            error = ret[0]
            energy = ret[1]
            fail = ret[2] 
            
            if maxError < error:
                maxError = error
            if minError > error:
                minError = error
                
            if fail == True:
                failed_num += 1
            
            errorSum += error
            energySum += energy
        
#             print "gene[" + gene[0] + "] try[" + str(PARALLEL_WIDTH * chunk + j) + "] trainInput[" + str(trainInput) + "] error: " + str(error) + " energy: " + str(energy)
        
    error = round((errorSum - maxError - minError)/(NUM_RUNS-failed_num-maxmin), 6)
    energy = round(energySum/(NUM_RUNS-failed_num), 6)
    
    logfile.write("     gene[" + gene[0] + "] trainInput[" + str(trainInput) + "] error: " + str(error) + " energy: " + str(energy) + "\n")
    
    return [error, energy]

Results = namedtuple('Results', 'error energy satisfyConstraints')
UniqueResults = {}

def binarySearch(name, gene, level, trainInputs, oneThird, depth):

    if UniqueResults.has_key(gene[0]):    
        gene[2] = UniqueResults.get(gene[0]).error
        gene[3] = UniqueResults.get(gene[0]).energy
        satisfyConstraints = UniqueResults.get(gene[0]).satisfyConstraints
    else:
        if name == 'zxing':
            zxingGeneBuild(name, gene)
        else:
            geneBuild(name, gene)
        
        satisfyConstraints = True
        errorSum = 0.0
        errors = []
        energySum = 0.0
        logfile.write('\n')
        logfile.write('Binary Search for gene[' + str(gene[0]) + ', ' + str(gene[1]) + ']\n')
#         print 'Binary Search for gene[' + str(gene[0]) + ', ' + str(gene[1]) + ']\n'
        for trainInput in trainInputs:
            [error, energy] = runGene(name, gene, level, trainInput)
            if error > ERR_EXPECTATION:         # at least one test input does not satisfy the constraints, this gene fails
                logfile.write('  fail - trainInput[' + str(trainInput) + ']\n')
                satisfyConstraints = False
            else:
                logfile.write('  pass - trainInput[' + str(trainInput) + ']\n')
            errorSum += error
            errors.append(error)
            energySum += energy
        gene[2] = errorSum / float(len(trainInputs))
        gene[3] = energySum / float(len(trainInputs))
        UniqueResults[gene[0]] = Results(gene[2], gene[3], satisfyConstraints)
        print 
    
    logfile.write('\n')
    logfile.write("  Result: " + str(gene) + "\n")
    logfile.write("    oneThird: " + str(oneThird) + "\n")
    logfile.write("    depth: " + str(depth) + "\n")
    logfile.write("    satisfyConstraints: " + str(satisfyConstraints) + "\n")
    logfile.write('\n')
    
    if depth == MAX_BINARY_SEARCH_DEPTH:
        return gene
        
    # if error reduction is not enough, increase more precise ops 
    if satisfyConstraints == False:         
        newGene = sg.getUpdatedGene(gene, oneThird, True, logfile)
        newGene = binarySearch(name, newGene, level, trainInputs, oneThird, depth + 1)
        bestgene = newGene
    # if error reduction is too much, decrease precise ops
    else:
        newGene = sg.getUpdatedGene(gene, oneThird, False, logfile)
        newGene = binarySearch(name, newGene, level, trainInputs, oneThird, depth + 1)
        if newGene[2] > ERR_EXPECTATION:    # when increased (aggressive) gene does not satisfy constraints
            bestgene = gene                 # select the safe original gene
        else:                               # newGene also satisfy the constraints
            if gene[3] > newGene[3]:         # compare energy saving, and pick the better one
                bestgene = newGene
            else:
                bestgene = gene
    
    return bestgene

def findStatGuaranteeGene(bench, level, trainInputs, testInputs):
    
    logfile.write('\n')
    logfile.write('\n')
    logfile.write('****** Statistically Gurantee Start ******\n')
    logfile.write('\n')
    logfile.write('\n')
    
    # train and find a gene guaranteeing statistically
    sg.sortOps()
    
    if level == 0:
        bestgene = LBestGene[bench]
    elif level == 1:
        bestgene = MBestGene[bench]
    elif level == 2:
        bestgene = FBestGene[bench]
    else:
        bestgene = HBestGene[bench]
    
    # bestgene test
    logfile.write('Test bestgene from genetic algorithm\n')
    logfile.write('\n')
    logfile.write(str(bestgene) + "\n")
    logfile.write('\n')
    if bench == 'zxing':
        zxingGeneBuild(bench, bestgene)
    else:
        geneBuild(bench, bestgene)

    satisfyConstraints = True
    for testInput in testInputs:
        [error, energy] = runGene(bench, bestgene, level, testInput)
        if error > ERR_EXPECTATION:        
            logfile.write("fail - trainInput[" + str(testInput) + ']\n')   
            satisfyConstraints = False
        else:
            logfile.write("pass - trainInput[" + str(testInput) + ']\n')
            
    if satisfyConstraints == False:
        logfile.write('\n')
        logfile.write('  ->  Bestgene from genetic algorithm failed to pass the test!\n')
        logfile.write('\n')
        logfile.write('[High Error Bits Removed Gene start ...[\n')
        print('high error bits removed gene ...')
        highErrRmvdBestGene = sg.getOneThirdGene(bestgene, 'high')     # high: 2
        UniqueResults.clear()
        highBestGene = binarySearch(bench, highErrRmvdBestGene, level, trainInputs, 'high', 0)
        logfile.write('\n')
        logfile.write("highBestGene: " + str(highBestGene) + "\n")
        logfile.write('\n')
        logfile.write('[High Error Bits Removed Gene end ...]\n')
        logfile.write('\n')
        logfile.write('[Middle Error Bits Removed Gene start ...]\n')
        print('middle error bits removed gene ...')
        midErrRmvdBestGene = sg.getOneThirdGene(bestgene, 'mid')      # middle: 1
        UniqueResults.clear()
        midBestGene = binarySearch(bench, midErrRmvdBestGene, level, trainInputs, 'mid', 0)
        logfile.write('\n')
        logfile.write("midBestGene: " + str(midBestGene) + "\n")
        logfile.write('\n')
        logfile.write('[Middle Error Bits Removed Gene end ...]\n')
        logfile.write('\n')
        logfile.write('[Low Error Bits Removed Gene start ...]\n')
        print('low error bits removed gene ...')
        lowErrRmvdBestGene = sg.getOneThirdGene(bestgene, 'low')      # low: 0
        UniqueResults.clear()
        lowBestGene = binarySearch(bench, lowErrRmvdBestGene, level, trainInputs, 'low', 0)
        logfile.write('\n')
        logfile.write("lowBestGene: " + str(lowBestGene) + "\n")
        logfile.write('\n')
        logfile.write('[Low Error Bits Removed Gene end ...]\n')
        logfile.write('\n')
        
        minEnergyConsume = highBestGene[3]
        bestgene = highBestGene
        if midBestGene[3] < minEnergyConsume:
            minEnergyConsume = midBestGene[3]
            bestgene = midBestGene
        if lowBestGene[3] < minEnergyConsume:
            minEnergyConsume = lowBestGene[3]
            bestgene = lowBestGene
    else:
        logfile.write('\n')
        logfile.write('  ->  Bestgene from genetic algorithm passed the test!\n')
        logfile.write('\n')
        
    # test
    if bench == 'zxing':
        zxingGeneBuild(bench, bestgene)
    else:
        geneBuild(bench, bestgene)

    satisfyConstraints = True
    logfile.write('\n')
    logfile.write('****** Final Test start ******\n')
    print '****** Final Test start ******'
    logfile.write('\n')
    for testInput in testInputs:
        [error, energy] = runGene(bench, bestgene, level, testInput)
        if error > ERR_EXPECTATION:  
            logfile.write("fail - testInput[" + str(testInput) + ']\n')
            print("fail - testInput[" + str(testInput) + ']\n')
            satisfyConstraints = False
        else:
            logfile.write("pass - testInput[" + str(testInput) + ']\n')
            print("pass - testInput[" + str(testInput) + ']')
    logfile.write('\n')
    logfile.write('****** Final Test end ******\n')
    print('****** Final Test end ******')
    logfile.write('\n')        
    
    if level == 0:
        LBestGene[bench] = bestgene
    elif level == 1:
        MBestGene[bench] = bestgene
    elif level == 2:
        FBestGene[bench] = bestgene
    else:
        HBestGene[bench] = bestgene
        
    logfile.write("Bestgene: " + str(bestgene) + "\n")

def main():
    
    if len(sys.argv) < 3:
        printUsage()
        sys.exit(0)
    bench = sys.argv[1]
    level = sys.argv[2]
    global logfile 
    logfile =  open(bench + '_' + level + "_" + str(ERR_EXPECTATION) + '.out', 'w')
    logfile.write('starting %s level %s...\n' % (bench, level)) # level 0[low], 1[medium], 2[high], 3[aggressive]
    init(bench)
    os.system('cd ../r2.apps ; ./cpDir.py ' + bench)
    geneticAlgorithm(bench, int(level), Inputs.BenchInputs[bench].trainInputs)
    print "genetic algorithm finished"
    print "statistic guarantee start"
    findStatGuaranteeGene(bench, int(level), Inputs.BenchInputs[bench].trainInputs, Inputs.BenchInputs[bench].testInputs)
    print "statistic guarantee end"
    logging(bench, int(level))
    logfile.close()
    os.system('cd ../r2.apps ; ./rmDir.py ' + bench)

if __name__ == '__main__':
    main()
