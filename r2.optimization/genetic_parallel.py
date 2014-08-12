#!/usr/bin/python

import sys
import os
import re
import shutil
import commands
import random
import math
from operator import itemgetter
from collections import namedtuple
from multiprocessing import Process, Queue

BASE_ADDR = os.environ['RESEARCH'] + '/expax/'
APP_ADDR = BASE_ADDR + 'apps/'
INSTR_ADDR = BASE_ADDR + 'ExpaxInst/'
NUM_RUNS = 1
#NUM_RUNS = 1

JarInfo = namedtuple('AppInfo', 'prj_name jar_name src_dir dst_dir approx_inst_num')

# Genetic Algorithm Related Data
POPULATION_SIZE = 10
#POPULATION_SIZE = 2
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
MAX_RUNS = 5
#MAX_RUNS = 1
ERROR_BAR = 0
#

BENCHMARK = {
    'FFT' : [
        JarInfo('FFT', 'scimark2.jar', 'scimark2/', 'scimark2/', 123),
    ],
    'SOR' : [
        JarInfo('SOR', 'scimark2.jar', 'scimark2/', 'scimark2/', 23),
    ],
    'MonteCarlo' : [
        JarInfo('MonteCarlo', 'scimark2.jar', 'scimark2/', 'scimark2/', 11),
    ],
    'SMM' : [
        JarInfo('SparseCompRow', 'scimark2.jar', 'scimark2/', 'scimark2/', 7),
    ],
    'LU' : [
        JarInfo('LU', 'scimark2.jar', 'scimark2/', 'scimark2/', 46)
    ],
    'zxing' : [
        JarInfo('ZXing', 'core.jar', 'zxing/', 'zxing/core/', 720),
        JarInfo('ZXing', 'javase.jar', 'zxing/', 'zxing/javase/', 39)
    ],
    'jmeint' : [
        JarInfo('jME', 'jmeint.jar', 'jmeint/', 'jmeint/', 1046),
    ],
    'imagefill' : [
        JarInfo('ImageJ', 'imagefill.jar', 'imagefill/', 'imagefill/', 130),
    ],
    'simpleRaytracer' : [
        JarInfo('Plane', 'simpleRaytracer.jar', 'simpleRaytracer/', 'simpleRaytracer/', 199),
    ]
}

INSTRUMENT = {
    'input_file' : INSTR_ADDR + 'input/old.jar',
    'output_file' : INSTR_ADDR + 'output/new.jar',
    'jar_file' : INSTR_ADDR + 'jars/enerjInst.jar'
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
        bitvector = ''
        for i in range(bit_size):
            bitvector += str(random.randrange(0, 2))
        LGeneration[name].append([bitvector, zeroBitNum(bitvector), 0, 0, 0])
        MGeneration[name].append([bitvector, zeroBitNum(bitvector), 0, 0, 0])
        FGeneration[name].append([bitvector, zeroBitNum(bitvector), 0, 0, 0])
        HGeneration[name].append([bitvector, zeroBitNum(bitvector), 0, 0, 0])

def init(name):
    jar_infos = BENCHMARK[name]
    if name == 'zxing':
        bit_size = 0
        for jar_info in jar_infos:
            bit_size += jar_info.approx_inst_num
        generate(name, bit_size)
    else:
        for jar_info in jar_infos:
            bit_size = jar_info.approx_inst_num
            generate(name, bit_size)

# only for zxing app
def special_build(name, level, round):
    jar_infos = BENCHMARK[name]
    i = 0
    for jar_info in jar_infos:
        os.chdir(INSTR_ADDR)
        fp = open('build.properties', 'w')
        fp.write('build.app.name=' + name + '\n')
        fp.write('build.bitvector=')
        if i == 0:
            if level == 0:
                fp.write(LGeneration[name][round][0][:720])
            elif level == 1:
                fp.write(MGeneration[name][round][0][:720])
            elif level == 2:
                fp.write(FGeneration[name][round][0][:720])
            else:
                fp.write(HGeneration[name][round][0][:720])
        else:
            if level == 0:
                fp.write(LGeneration[name][round][0][720:])
            elif level == 1:
                fp.write(MGeneration[name][round][0][720:])
            elif level == 2:
                fp.write(FGeneration[name][round][0][720:])
            else:
                fp.write(HGeneration[name][round][0][720:])
        fp.close()
        path = APP_ADDR + jar_info.src_dir + jar_info.jar_name
        shutil.copy(path,  INSTRUMENT['input_file'])
        cmd = 'ant run'
        (status, output) = commands.getstatusoutput(cmd)
        if status:
            sys.stderr.write(output)
            sys.exit(1)
        path = APP_ADDR + str(round) + jar_info.dst_dir + jar_info.jar_name 
        shutil.copy(INSTRUMENT['output_file'], path)

def build(name, level, round):
    jar_infos = BENCHMARK[name]
    for jar_info in jar_infos:
        os.chdir(INSTR_ADDR)
        fp = open('build.properties', 'w')
        if name == 'SMM':
            fp.write('build.app.name=SparseCompRow' + '\n')
        else:
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
        fp.close()
        path = APP_ADDR + jar_info.src_dir + jar_info.jar_name
        shutil.copy(path,  INSTRUMENT['input_file'])
        cmd = 'ant run'
        (status, output) = commands.getstatusoutput(cmd)
        if status:
            sys.stderr.write(output)
            sys.exit(1)
        path = APP_ADDR + str(round) + jar_info.dst_dir + 'new.jar'
        shutil.copy(INSTRUMENT['output_file'], path) 

def check(output, level):
    is_failed = False
    index = output.find('level ' + str(level))
    if output[index+8] == 'f':
        is_failed = True
    return is_failed

def run(name, level, result, pid):
    os.chdir(APP_ADDR)
    error = 0
    energy = 0
    failed_num = 0
    if level == 0:
        err_cmd = './collect_low' + str(pid) + '.py ' + name
        energy_cmd = './energy_chart_low' + str(pid) + '.py ' + name
        for i in range(NUM_RUNS):
            print 'enerJ iteration %d...' % (i+1)
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
            error += float(match[0][1:]) * 100
            (status, output) = commands.getstatusoutput(energy_cmd)
            if status:
                sys.stderr.write(output)
                sys.exit(1)
            #print output
            index = output.find('-1')
            #print output[index+3 : index+10]
            energy += float(output[index+3 : index+10]) * 100
        error = round(error/(NUM_RUNS-failed_num), 6)
        energy = round(energy/(NUM_RUNS-failed_num), 6)
        result.put([pid, error, energy])
    elif level == 1:
        err_cmd = './collect_med' + str(pid) + '.py ' + name
        energy_cmd = './energy_chart_med' + str(pid) +'.py ' + name
        for i in range(NUM_RUNS):
            print 'enerJ iteration %d...' % (i+1)
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
            error += float(match[0][1:]) * 100
            (status, output) = commands.getstatusoutput(energy_cmd)
            if status:
                sys.stderr.write(output)
                sys.exit(1)
            #print output
            index = output.find('-2')
            #print output[index+3 : index+10]
            energy += float(output[index+3 : index+10]) * 100
        error = round(error/(NUM_RUNS-failed_num), 6)
        energy = round(energy/(NUM_RUNS-failed_num), 6)
        result.put([pid, error, energy])
    elif level == 2:
        err_cmd = './collect_fd' + str(pid) + '.py ' + name
        energy_cmd = './energy_chart_fd' + str(pid) + '.py ' + name
        for i in range(NUM_RUNS):
            print 'enerJ iteration %d...' % (i+1)
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
            error += float(match[0][1:]) * 100
            (status, output) = commands.getstatusoutput(energy_cmd)
            if status:
                sys.stderr.write(output)
                sys.exit(1)
            #print output
            index = output.find('-3')
            #print output[index+3 : index+10]
            energy += float(output[index+3 : index+10]) * 100
        error = round(error/(NUM_RUNS-failed_num), 6)
        energy = round(energy/(NUM_RUNS-failed_num), 6)
        result.put([pid, error, energy])
    else:
        err_cmd = './collect_high' + str(pid) + '.py ' + name
        energy_cmd = './energy_chart_high' + str(pid) + '.py ' + name
        for i in range(NUM_RUNS):
            print 'enerJ iteration %d...' % (i+1)
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
            error += float(match[0][1:]) * 100
            (status, output) = commands.getstatusoutput(energy_cmd)
            if status:
                sys.stderr.write(output)
                sys.exit(1)
            #print output
            index = output.find('-4')
            #print output[index+3 : index+10]
            energy += float(output[index+3 : index+10]) * 100
        error = round(error/(NUM_RUNS-failed_num), 6)
        energy = round(energy/(NUM_RUNS-failed_num), 6)
        result.put([pid, error, energy])

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
        print
        print '[level 0]:'
        for i in range(POPULATION_SIZE):
            print (i+1), LGeneration[name][i]
        print
    elif level == 1:
        MGeneration[name] = sorted(MGeneration[name], key=itemgetter(4))
        if name in MBestGene:
            if MGeneration[name][0][4] < MBestGene[name][4]:
                MBestGene[name] = MGeneration[name][0]
        else:
            MBestGene[name] = MGeneration[name][0]
        #del MGeneration[name][-diff:]
        #print '[level 1] best gene:', MBestGene[name]
        print
        print '[level 1]:'
        for i in range(POPULATION_SIZE):
            print (i+1), MGeneration[name][i]
        print
    elif level == 2:
        FGeneration[name] = sorted(FGeneration[name], key=itemgetter(4))
        if name in FBestGene:
            if FGeneration[name][0][4] < FBestGene[name][4]:
                FBestGene[name] = FGeneration[name][0]
        else:
            FBestGene[name] = FGeneration[name][0]
        #del FGeneration[name][-diff:]
        #print '[level 2] best gene:', FBestGene[name]
        print
        print '[level 2]:'
        for i in range(POPULATION_SIZE):
            print (i+1), FGeneration[name][i]
        print
    else:
        HGeneration[name] = sorted(HGeneration[name], key=itemgetter(4))
        if name in HBestGene:
            if HGeneration[name][0][4] < HBestGene[name][4]:
                HBestGene[name] = HGeneration[name][0]
        else:
            HBestGene[name] = HGeneration[name][0]
        #del HGeneration[name][-diff:]
        #print '[level 2] best gene:', HBestGene[name]
        print
        print '[level 3]:'
        for i in range(POPULATION_SIZE):
            print (i+1), HGeneration[name][i]
        print

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
    new_bv = ''
    for bit in bv:
        if random.random() <= MUTE_PROB:
            if bit == '0':
                new_bv += '1'
            else:
                new_bv += '0'
        else:
            new_bv += bit 
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

def runAlgorithm(name, level):
    print 'error level %d...' % level
    for i in range(MAX_RUNS):
        print 'genetic algo iteration %d...' % (i+1)
        result = Queue()
        pool = [Process(target=run, args=(name, level, result, k)) for k in range(POPULATION_SIZE)]
        for j in range(POPULATION_SIZE):
            print 'candidate gene %d...' % (j+1)
            if name == 'zxing':
                special_build(name, level, j)
            else:
                build(name, level, j)
            pool[j].start()
        for p in pool:
            p.join()
        n = POPULATION_SIZE
        while n:
            n -= 1
            ret = result.get()
            pid = ret[0]
            error = ret[1]
            energy = ret[2]
            if level == 0:
                zeroBitNum = LGeneration[name][pid][1]
                totalLen = len(LGeneration[name][pid][0])
                normalZero = 100.0 * zeroBitNum / totalLen
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

def logging(name):
    if name == 'zxing':
        jar_infos = BENCHMARK[name]
        for jar_info in jar_infos:
            print 'zxing:%s: ' % jar_info.jar_name
            print 'error_low:', LBestGene[jar_info.jar_name], 'error_med:', MBestGene[jar_info.jar_name], \
            'error_fd', FBestGene[jar_info.jar_name], 'error_high:', HBestGene[jar_info.jar_name]
    else:
        print '%s: ' % name
        print 'error_low:', LBestGene[name], 'error_med:', MBestGene[name], 'error_fd', FBestGene[name], 'error_high:', HBestGene[name]

def main():
    args = sys.argv[1:]
    if not args:
        for key in BENCHMARK:
            print 'starting %s...' % key
            init(key)
            for level in range(0, 4):
                runAlgorithm(key, level)
            logging(key)
    else:
        for arg in args:
            print 'starting %s...' % arg
            init(arg)
            for level in range(0, 4):
                runAlgorithm(arg, level)    
            logging(arg)

if __name__ == '__main__':
    main()
