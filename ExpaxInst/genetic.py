#!/usr/bin/python

import sys
import os
import re
import shutil
import commands
import random
from operator import itemgetter
from collections import namedtuple

BASE_ADDR = os.environ['RESEARCH'] + '/expax/'
APP_ADDR = BASE_ADDR + 'apps/'
INSTR_ADDR = BASE_ADDR + 'ExpaxInst/'
NUM_RUNS = 1
# NUM_RUNS = 1

JarInfo = namedtuple('AppInfo', 'prj_name jar_name src_dir dst_dir approx_inst_num')

# Genetic Algorithm Related Data
POPULATION_SIZE = 3
# POPULATION_SIZE = 2
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
MAX_RUNS = 3
# MAX_RUNS = 1
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
        JarInfo('LU', 'scimark2.jar', 'scimark2/', 'scimark2/', 51)
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
    'jar_file' : INSTR_ADDR + 'jars/ExpaxInst.jar'
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
    for jar_info in jar_infos:
        bit_size = jar_info.approx_inst_num
        if name == 'zxing':
            generate(jar_info.jar_name, bit_size)
        else:
            generate(name, bit_size)

# only for zxing app
def special_build(name, jar_name, level, round):
    jar_infos = BENCHMARK[name]
    for jar_info in jar_infos:
        if jar_info.jar_name == jar_name:
            os.chdir(INSTR_ADDR)
            fp = open('build.properties', 'w')
            fp.write('build.app.name=' + name + '\n')
            fp.write('build.bitvector=')
            if level == 0:
                fp.write(LGeneration[jar_info.jar_name][round][0])
            elif level == 1:
                fp.write(MGeneration[jar_info.jar_name][round][0])
            elif level == 2:
                fp.write(FGeneration[jar_info.jar_name][round][0])
            else:
                fp.write(HGeneration[jar_info.jar_name][round][0])
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
        else:
            os.chdir(INSTR_ADDR)
            fp = open('build.properties', 'w')
            fp.write('build.app.name=' + name + '\n')
            fp.write('build.bitvector=')
            for i in range(jar_info.approx_inst_num):
                fp.write('0')
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
        path = APP_ADDR + jar_info.dst_dir + 'new.jar'
        shutil.copy(INSTRUMENT['output_file'], path) 

def check(output, level):
    is_failed = False
    index = output.find('level ' + str(level))
    if output[index+8] == 'f':
        is_failed = True
    return is_failed

def run(name, level):
    os.chdir(APP_ADDR)
    error = 0
    energy = 0
    failed_num = 0
    if level == 0:
        err_cmd = './collect_low.py ' + name
        energy_cmd = './energy_chart_low.py'
        for i in range(NUM_RUNS):
            print 'enerJ iteration %d...' % i
            (status, output) = commands.getstatusoutput(err_cmd)
            if status:
                sys.stderr.write(output)
                sys.exit(1)
            if check(output, level):
                failed_num += 1
                continue
            print output
            match = re.findall(r'\s[0-1]?.[0-9]+', output)
            error += float(match[0][1:])
            (status, output) = commands.getstatusoutput(energy_cmd)
            if status:
                sys.stderr.write(output)
                sys.exit(1)
            print output
            index = output.find('-1')
            energy += float(output[index+3 : index+10])
        error = round(error/(NUM_RUNS-failed_num), 4)
        energy = round(energy/(NUM_RUNS-failed_num), 4)
        return [error, energy]
    elif level == 1:
        err_cmd = './collect_med.py ' + name
        energy_cmd = './energy_chart_med.py'
        for i in range(NUM_RUNS):
            print 'enerJ iteration %d...' % i
            (status, output) = commands.getstatusoutput(err_cmd)
            if status:
                sys.stderr.write(output)
                sys.exit(1)
            if check(output, level):
                failed_num += 1
                continue
            print output
            match = re.findall(r'\s[0-1]?.[0-9]+', output)
            error += float(match[0][1:])
            (status, output) = commands.getstatusoutput(energy_cmd)
            if status:
                sys.stderr.write(output)
                sys.exit(1)
            print output
            index = output.find('-2')
            energy += float(output[index+3 : index+10])
        error = round(error/(NUM_RUNS-failed_num), 4)
        energy = round(energy/(NUM_RUNS-failed_num), 4)
        return [error, energy]
    elif level == 2:
        err_cmd = './collect_fd.py ' + name
        energy_cmd = './energy_chart_fd.py'
        for i in range(NUM_RUNS):
            print 'enerJ iteration %d...' % i
            (status, output) = commands.getstatusoutput(err_cmd)
            if status:
                sys.stderr.write(output)
                sys.exit(1)
            if check(output, level):
                failed_num += 1
                continue
            print output
            match = re.findall(r'\s[0-1]?.[0-9]+', output)
            error += float(match[0][1:]) 
            (status, output) = commands.getstatusoutput(energy_cmd)
            if status:
                sys.stderr.write(output)
                sys.exit(1)
            print output
            index = output.find('-3')
            energy += float(output[index+3 : index+10])
        error = round(error/(NUM_RUNS-failed_num), 4)
        energy = round(energy/(NUM_RUNS-failed_num), 4)
        return [error, energy]
    else:
        err_cmd = './collect_high.py ' + name
        energy_cmd = './energy_chart_high.py'
        for i in range(NUM_RUNS):
            print 'enerJ iteration %d...' % i
            (status, output) = commands.getstatusoutput(err_cmd)
            if status:
                sys.stderr.write(output)
                sys.exit(1)
            if check(output, level):
                failed_num += 1
                continue
            print output
            match = re.findall(r'\s[0-1]?.[0-9]+', output)
            error += float(match[0][1:])
            (status, output) = commands.getstatusoutput(energy_cmd)
            if status:
                sys.stderr.write(output)
                sys.exit(1)
            print output
            index = output.find('-4')
            energy += float(output[index+3 : index+10])
        error = round(error/(NUM_RUNS-failed_num), 4)
        energy = round(energy/(NUM_RUNS-failed_num), 4)
        return [error, energy]

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
        print '[level 0] best gene:', LBestGene[name]
    elif level == 1:
        MGeneration[name] = sorted(MGeneration[name], key=itemgetter(4))
        if name in MBestGene:
            if MGeneration[name][0][4] < MBestGene[name][4]:
                MBestGene[name] = MGeneration[name][0]
        else:
            MBestGene[name] = MGeneration[name][0]
        #del MGeneration[name][-diff:]
        print '[level 1] best gene:', MBestGene[name]
    elif level == 2:
        FGeneration[name] = sorted(FGeneration[name], key=itemgetter(4))
        if name in FBestGene:
            if FGeneration[name][0][4] < FBestGene[name][4]:
                FBestGene[name] = FGeneration[name][0]
        else:
            FBestGene[name] = FGeneration[name][0]
        #del FGeneration[name][-diff:]
        print '[level 2] best gene:', FBestGene[name]
    else:
        HGeneration[name] = sorted(HGeneration[name], key=itemgetter(4))
        if name in HBestGene:
            if HGeneration[name][0][4] < HBestGene[name][4]:
                HBestGene[name] = HGeneration[name][0]
        else:
            HBestGene[name] = HGeneration[name][0]
        #del HGeneration[name][-diff:]
        print '[level 3] best gene:', HBestGene[name]

def cal_prob(list):
    # get the total scores
    total = sum(l[4] for l in list)
    prob_list = []
    if len(list) == 1:
        prob_list.append(1)
    else:
        for l in list:
            prob_list.append(1-l[4]/total)
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
    zeroBitNum = gene[1]
    new_bv = ''
    for bit in bv:
        if random.random() <= MUTE_PROB:
            if bit == '0':
                new_bv += '1'
                zeroBitNum -= 1
            else:
                new_bv += '0'
                zeroBitNum += 1
        else:
            new_bv += bit 
    return [new_bv, zeroBitNum, 0, 0, 0]

def crossover(f_gene, m_gene):
    pos = random.randrange(0, len(f_gene))
    return [f_gene[:pos] + m_gene[pos:], m_gene[:pos] + f_gene[pos:]]

def new_generate(name, level):
    new_list = []
    count = POPULATION_SIZE
    if level == 0:
        # always keep best genes
        #best_score = LGeneration[name][0][4]
        #for gene in LGeneration[name]:
            #if gene[4] == best_score:
                #new_list.append(gene)
                #count -= 1
            #else: break
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
        #best_score = MGeneration[name][0][4]
        #for gene in MGeneration[name]:
            #if gene[4] == best_score:
                #new_list.append(gene)
                #count -= 1
            #else: break
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
        #best_score = HGeneration[name][0][4]
        #for gene in HGeneration[name]:
            #if gene[4] == best_score:
                #new_list.append(gene)
                #count -= 1
            #else: break
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
    if name == 'zxing':
        jar_infos = BENCHMARK[name]
        for jar_info in jar_infos:
            print 'error level %d...' % level
            # total iterations
            for i in range(MAX_RUNS):
                print 'genetic algo iteration %d...' % i
                # total configurations in each generation
                for j in range(POPULATION_SIZE):
                    print 'candidate gene %d...' % j
                    special_build(name, jar_info.jar_name, level, j)
                    # update score
                    if level == 0:
                        zeroBitNum = LGeneration[jar_info.jar_name][j][1]
                        totalLen = len(LGeneration[jar_info.jar_name][j][0])
                        normalZero = 1.0 * zeroBitNum / totalLen
                        [error_low, energy_low] = run(name, level)
                        LGeneration[jar_info.jar_name][j][2] = error_low
                        LGeneration[jar_info.jar_name][j][3] = energy_low
                        LGeneration[jar_info.jar_name][j][4] = error_low * ERR_WEIGHT + energy_low * ENERGY_WEIGHT
                    elif level == 1:
                        zeroBitNum = MGeneration[jar_info.jar_name][j][1]
                        totalLen = len(MGeneration[jar_info.jar_name][j][0])
                        normalZero = 1.0 * zeroBitNum / totalLen
                        [error_med, energy_med] = run(name, level)
                        MGeneration[jar_info.jar_name][j][2] = error_med
                        MGeneration[jar_info.jar_name][j][3] = energy_med
                        MGeneration[jar_info.jar_name][j][4] = error_med * ERR_WEIGHT + energy_med * ENERGY_WEIGHT
                    elif level == 2:
                        zeroBitNum = FGeneration[jar_info.jar_name][j][1]
                        totalLen = len(FGeneration[jar_info.jar_name][j][0])
                        normalZero = 1.0 * zeroBitNum / totalLen
                        [error_fd, energy_fd] = run(name, level)
                        FGeneration[jar_info.jar_name][j][2] = error_fd
                        FGeneration[jar_info.jar_name][j][3] = energy_fd
                        FGeneration[jar_info.jar_name][j][4] = error_fd * ERR_WEIGHT + energy_fd * ENERGY_WEIGHT
                    else:
                        zeroBitNum = HGeneration[jar_info.jar_name][j][1]
                        totalLen = len(HGeneration[jar_info.jar_name][j][0])
                        normalZero = 1.0 * zeroBitNum / totalLen
                        [error_high, energy_high] = run(name, level)
                        HGeneration[jar_info.jar_name][j][2] = error_high
                        HGeneration[jar_info.jar_name][j][3] = energy_high
                        HGeneration[jar_info.jar_name][j][4] = error_high * ERR_WEIGHT + energy_high * ENERGY_WEIGHT
                selection(jar_info.jar_name, level)
                if i == MAX_RUNS - 1: break
                new_generate(jar_info.jar_name, level)
    else:
        print 'error level %d...' % level
        for i in range(MAX_RUNS):
            print 'genetic algo iteration %d...' % i
            for j in range(POPULATION_SIZE):
                print 'candidate gene %d...' % j
                build(name, level, j)
                if level == 0:
                    zeroBitNum = LGeneration[name][j][1]
                    totalLen = len(LGeneration[name][j][0])
                    normalZero = 1.0 * zeroBitNum / totalLen
                    [error_low, energy_low] = run(name, level)
                    LGeneration[name][j][2] = error_low
                    LGeneration[name][j][3] = energy_low
                    LGeneration[name][j][4] = error_low * ERR_WEIGHT + energy_low * ENERGY_WEIGHT
                    print LGeneration[name]
                elif level == 1:
                    zeroBitNum = MGeneration[name][j][1]
                    totalLen = len(MGeneration[name][j][0])
                    normalZero = 1.0 * zeroBitNum / totalLen
                    [error_med, energy_med] = run(name, level)
                    MGeneration[name][j][2] = error_med
                    MGeneration[name][j][3] = energy_med
                    MGeneration[name][j][4] = error_med * ERR_WEIGHT + energy_med * ENERGY_WEIGHT
                    print MGeneration[name]
                elif level == 2:
                    zeroBitNum = FGeneration[name][j][1]
                    totalLen = len(FGeneration[name][j][0])
                    normalZero = 1.0 * zeroBitNum / totalLen
                    [error_fd, energy_fd] = run(name, level)
                    FGeneration[name][j][2] = error_fd
                    FGeneration[name][j][3] = energy_fd
                    FGeneration[name][j][4] = error_fd * ERR_WEIGHT + energy_fd * ENERGY_WEIGHT
                else:
                    zeroBitNum = HGeneration[name][j][1]
                    totalLen = len(HGeneration[name][j][0])
                    normalZero = 1.0 * zeroBitNum / totalLen
                    [error_high, energy_high] = run(name, level)
                    HGeneration[name][j][2] = error_high
                    HGeneration[name][j][3] = energy_high
                    HGeneration[name][j][4] = error_high * ERR_WEIGHT + energy_high * ENERGY_WEIGHT
                    print HGeneration[name]
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
            # error_low error_med error_fd error_high
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
