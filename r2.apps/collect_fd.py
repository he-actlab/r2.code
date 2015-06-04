#!/usr/bin/python

from __future__ import division
import os
import subprocess
from collections import namedtuple
import logging
import math
import json
import sys
import re

Bmark = namedtuple('Benchmark', 'name runargs output regex')
BMARKS = {
    #'scimark2': [
        #Bmark('SciMark2: FFT',        'fft', 'absolute', None),
        #Bmark('SciMark2: SOR',        'sor', 'absolute', None),
        #Bmark('SciMark2: MonteCarlo', 'mc',  'proportion', None),
        #Bmark('SciMark2: SMM',        'smm', 'proportion', None),
        #Bmark('SciMark2: LU',         'lu',  'absolute', None),
    #],
     'zxing': [
        Bmark('zxing', '', 'string', r'Parsed result:\n(\S+)'),
    ],
    'zxing-enerj': [
        Bmark('zxing', '', 'string', r'Parsed result:\n(\S+)'),
    ],
    'jmeint': [
        Bmark('jmeint', '', 'boolean', None),
    ],
    'jmeint-enerj': [
        Bmark('jmeint', '', 'boolean', None),
    ],
    'simpleRaytracer': [
        Bmark('simpleRaytracer', '' , 255, None)
    ],
    'simpleRaytracer-enerj': [
        Bmark('simpleRaytracer', '' , 255, None)
    ],
    'sobel': [
        Bmark('sobel', '' , 255, None)
    ],
    'sobel-enerj': [
        Bmark('sobel', '' , 255, None)
    ],
    'fft': [
        Bmark('fft', '', 'absolute', None)
    ],
    'fft-enerj': [
        Bmark('fft', '', 'absolute', None)
    ],
    'sor': [
        Bmark('sor', '', 'absolute', None)
    ],
    'sor-enerj': [
        Bmark('sor', '', 'absolute', None)
    ],
    'lu': [
        Bmark('lu', '', 'absolute', None)
    ],
    'lu-enerj': [
        Bmark('lu', '', 'absolute', None)
    ],
    'mc': [
        Bmark('mc', '', 'proportion', None)
    ],
    'mc-enerj': [
        Bmark('mc', '', 'proportion', None)
    ],
    'smm': [
        Bmark('smm', '', 'proportion', None)
    ],
    'smm-enerj': [
        Bmark('smm', '', 'proportion', None)
    ]
}
COMMANDS = {
    'run_precise':   './run.sh -nonoise %s',
    'build_sim':     './build.sh',
    'run_approx':    './run.sh %s',
}
REPLICATIONS = 1
STATS_FILENAME = 'enerjstats.json'
NOISE = {
    'INVPROB_SRAM_WRITE_FAILURE': (int(10**5.59), int(10**4.94), int(10**4.15), 10**3),
    'INVPROB_SRAM_READ_UPSET': (int(10**16.7), int(10**7.4), int(10**5.8), 10**5),
    'MB_FLOAT_APPROX': (16, 8, 6, 4),
    'MB_DOUBLE_APPROX': (32, 16, 12, 8),
    'INVPROB_DRAM_FLIP_PER_SECOND': (10**9, 10**5, 10**4, 10**3),
    'TIMING_ERROR_PROB_PERCENT': (0.0001, 0.01, 0.1, 1.0),
}
MODES_NOISE_KEY = 'TIMING_ERROR_PROB_PERCENT'
MODE_KEY = 'TIMING_ERROR_MODE'
MODES = 1, 2, 3
MODE_DEFAULT = 2
NOISE_FILE = 'enerjnoiseconsts.json'
FD = 2
DISABLED = 0
JSON_OUT = 'results.json'

class Result(object):
    def __init__(self, bmark):
        self.bmark = bmark
        self.precise_output = None
        self.output_individual = {}
        self.output_collective = [None, None, None, None]
        self.stats = None
    
    def _level_error(self, level, outputs):
        if not outputs:
            return 'level %i failed!' % level
        error = calc_error(self.bmark, self.precise_output, outputs)
        return 'level %i: %f' % (level, error)
    
    def _frac(self, a, b):
        total = a + b
        if total == 0.0:
            return 0.0
        else:
            return a / total
    
    def __str__(self):
        out = []
        out.append('%s\n' % self.bmark.name)
        
        if self.output_collective != [None, None, None, None]:
            out.append('  mean error for collective noise variation:\n')
            level = FD    
            outputs = self.output_collective[level]
            out.append('    %s\n' % self._level_error(level, outputs))
        
        #if self.output_individual:
            #out.append('  mean error for individual noise variation:\n')
            #for const, level_outputs in self.output_individual.iteritems():
                #out.append('    %s:\n' % const)
                #for level in (LOW, MED, FD, HIGH):
                    #outputs = level_outputs[level]
                    #out.append('      %s\n' % self._level_error(level, outputs))
            
        out.append('  approximateness:\n')
        for stat, (precise, approx) in self.stats.iteritems():
            out.append('    %s: %.1f%% (%i/%i)\n' % \
                (stat, self._frac(approx, precise)*100, approx, approx+precise))
        return ''.join(out)
    
    def flatten(self):
        obj = {}
        
        if self.output_collective != [None, None, None, None]:
            obj['collective'] = [None, None, None, None]
            level = FD
            obj['collective'][level] = calc_error(self.bmark,
                       self.precise_output, self.output_collective[level])
        
        if self.output_individual:
            obj['individual'] = {}
            for const, level_outputs in self.output_individual.iteritems():
                out = [None, None, None, None]
                obj['individual'][const] = out
                level = FD
                out[level] = calc_error(self.bmark,
                           self.precise_output, level_outputs[level])
        
        obj['approximateness'] = self.stats
        
        return obj

class CommandError(Exception):
    def __init__(self, command, code, stderr):
        self.command = command
        self.code = code
        self.stderr = stderr
    def __str__(self):
        return 'command %s exited wth code %i:\n%s' % \
               (self.command, self.code, self.stderr)
def shell(line):
    logging.debug('+ ' + line);
    proc = subprocess.Popen(line, shell=True,
                            stdout=subprocess.PIPE,
                            stderr=subprocess.PIPE)
    stdout, stderr = proc.communicate()
    
    if proc.returncode != 0:
        raise CommandError(line, proc.returncode, stderr)
    
    return stdout

def run(bmark, noise, benchArg):
    #logging.info('running "%s" %s' % (bmark.name, 'approx' if noise else 'precise'))
                 
    # Write out noise dictionary.
    if noise is not None:
        logging.debug('writing noise dictionary: %s' % noise)
        with open(NOISE_FILE, 'w') as f:
            json.dump(noise, f)
    
    # Get the stdout from running the benchmark.
    if noise is not None:
        cmd = COMMANDS['run_approx']
    else:
        cmd = COMMANDS['run_precise']
    newCmd = cmd % bmark.runargs
    for arg in benchArg:
        newCmd += " "
        newCmd += arg
    out = shell(newCmd)
    out = re.sub(r'Loading Precision.+', '', out)
    out = out.strip()
    
    # Extract output.
    if bmark.regex:
        match = re.search(bmark.regex, out)
        if match:
            out = match.group(1)
        else:
            out = ''
    
    if bmark.output != 'string':
        if ':' in out:
            tag, out = out.split(': ', 1)
        else:
            out = out
    
    # Parse output.
    if bmark.output in ('absolute', 'proportion', 'boolean'):
        return [float(value.strip()) for value in out.split()]
    if isinstance(bmark.output, int):
        return [int(value.strip()) for value in out.split()]
    elif bmark.output == 'string':
        return out
    else:
        assert False

def replicated_run(bmark, noise, benchArg):
    out = []
    for i in xrange(REPLICATIONS):
        try:
            res = run(bmark, noise, benchArg)
            logging.debug('output: ' + repr(res))
            out.append(res)
        except CommandError:
            logging.warn('execution failed!')
            # TODO! Log number of failed executions.
    return out

def collect_outputs(path, bmarks, apronly=False, colonly=False, benchArg=[]):
    curdir = os.getcwd()
    os.chdir(path)

    # Now rebuild with simulation.
    #logging.info('building %s with simulation' % path)
    #shell(COMMANDS['build_sim'])
    
    # Run each benchmark precisely and gather precise outputs.
    results = {}
    for bmark in bmarks:
        result = Result(bmark)
        result.precise_output = run(bmark, None, benchArg)
        logging.debug('output: ' + repr(result.precise_output))
        results[bmark] = result
        
        # Load statistics from precise run.
        results[bmark].stats = summarize_stats(json.load(open(STATS_FILENAME)))
    
    # Abort if we're just getting the approximateness.
    if apronly:
        os.chdir(curdir)
        return results
    
    # Get approximate outputs.
    approx_outputs = {}
    stats = {}
    for bmark in bmarks:
        
        # Collective variation.
        level = FD
        #logging.info('collective noise level: %i' % level)
        noise = {}
        for name, vals in NOISE.iteritems():
            noise[name] = vals[level]
        noise[MODE_KEY] = MODE_DEFAULT
        results[bmark].output_collective[level] = \
            replicated_run(bmark, noise, benchArg)
        
        if colonly:
            # Skip individual variation.
            continue
        
        # Individual variation.
        for const in NOISE:
            if const == MODES_NOISE_KEY:
                continue
            #logging.info('individual variation for %s' % const)
            outputs = [None, None, None, None]
            results[bmark].output_individual[const] = outputs
            level = FD
            #logging.info('indiv. noise level for %s: %i' % (const, level))
            noise = {}
            for g_const, g_values in NOISE.iteritems():
                if g_const == const:
                    noise[g_const] = g_values[level]
                else:
                    noise[g_const] = DISABLED
            noise[MODE_KEY] = DISABLED
            outputs[level] = replicated_run(bmark, noise, benchArg)

        # Individual variation for timing error modes.
        #FIXME Terrible copy-and-paste!
        for mode in MODES:
            #logging.info('individual variation for %s mode %i' % (MODES_NOISE_KEY, mode))
            name = '%s-%i' % (MODES_NOISE_KEY, mode)
            outputs = [None, None, None, None]
            results[bmark].output_individual[name] = outputs
            level = FD
            #logging.info('mode noise level for %s: %i' % (name, level))
            noise = {}
            for g_const, g_values in NOISE.iteritems():
                if g_const == MODES_NOISE_KEY:
                    noise[g_const] = g_values[level]
                else:
                    noise[g_const] = DISABLED
            noise[MODE_KEY] = mode
            outputs[level] = replicated_run(bmark, noise, benchArg)
    
    os.chdir(curdir)
    
    return results

def mean(vals):
    return sum(vals) / len(vals)

def err(x, y, proportion, norm=None):
    errs = []
    for a, b in zip(x, y):
        if math.isnan(a):
            e = 0.0 if math.isnan(b) else 1.0
        elif math.isnan(b):
            e = 1.0
        else:
            e = abs(a - b)
            if proportion:
                e = e / a
            e = min(e, norm if norm else 1.0) # Bound the error at unity.
        errs.append(e)
    avg = mean(errs)
    if norm:
        avg = min(avg / norm, 1.0)
    return avg

def calc_error(bmark, precise_output, approx_outputs):
    errors = []
    for approx_output in approx_outputs:
        if bmark.output == 'string':
            # String matching on output.
            if approx_output == precise_output:
                errors.append(0.0)
            else:
                errors.append(1.0)
        
        elif bmark.output in ('absolute', 'boolean', 'proportion'):
            # Output is a list of floats.
            errors.append(err(
                precise_output,
                approx_output,
                bmark.output == 'proportion',
                0.5 if bmark.output == 'boolean' else None
            ))
        
        elif isinstance(bmark.output, int):
            # Output is a list of integers. The value is the normalization.
            norm = float(bmark.output)
            errors.append(err(precise_output, approx_output, False, norm))
            
        else:
            assert false
    
    if not errors:
        return float('nan')
    else:
        return mean(errors)

def summarize_stats(stats):
    out = {
        'fpu': [0, 0],
        'alu': [0, 0],
        'heap': [0, 0],
        'stack': [0, 0],
        'loads': [0, 0],
        'stores': [0, 0],
    }
    
    for name, (precise, approx) in stats['operations'].iteritems():
        if name.startswith('FLOAT') or name.startswith('DOUBLE'):
            out['fpu'][0] += precise
            out['fpu'][1] += approx
        elif name.startswith('INT') or name.startswith('LONG') or \
                name.startswith('SHORT'):
            out['alu'][0] += precise
            out['alu'][1] += approx
        elif name.startswith('load'):    
            out['loads'][0] += precise
            out['loads'][1] += approx
        elif name.startswith('store'):    
            out['stores'][0] += precise
            out['stores'][1] += approx
    
    for name, (precise, approx) in stats['footprint'].iteritems():
        section, kind = name.split('-')
        if kind != 'bytes':
            continue
        out[section][0] += precise
        out[section][1] += approx
    
    return out

def dump_json(results, path):
    obj = {}
    for bmark, result in results.iteritems():
        obj[bmark.name] = result.flatten()
    
    with open(path, 'w') as f:
        json.dump(obj, f)

if __name__ == '__main__':    
    logging.getLogger('').setLevel(logging.INFO)
    
    path = sys.argv[1]
    benchArg = []
    for i in range(2, len(sys.argv)):
        benchArg.append(sys.argv[i])
    apronly = False
    colonly = False
    #if args and args[0] == '-d':
        #logging.getLogger('').setLevel(logging.DEBUG)
        #args.pop(0)
    #elif args and args[0] == '-a':
        #apronly = True
        #args.pop(0)
    #elif args and args[0] == '-c':
        #colonly = True
        #args.pop(0)
    
    total = {}
    if path == 'FFT' or path == 'SOR' or path == 'MonteCarlo' or path == 'SMM' or path == 'LU':
        results = collect_outputs('scimark2', BMARKS[path], apronly, colonly, benchArg)
    else:
        results = collect_outputs(path, BMARKS[path], apronly, colonly, benchArg)
    print '\n'.join(str(res) for res in results.itervalues())
    total.update(results)
    if path == 'FFT' or path == 'SOR' or path == 'MonteCarlo' or path == 'SMM' or path == 'LU':
        dump_json(total, 'scimark2/' + JSON_OUT)
    else:
        dump_json(total, path + '/' + JSON_OUT)
