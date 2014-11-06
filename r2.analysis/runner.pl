#!/usr/bin/perl

use strict;
use Getopt::Long;
use feature "switch";

######################################################################################
# Configuration of environment variables, benchmarks, analyses, and options.
#
# All things that need to be configured are in this section. They include:
# 1. Programs - benchmarks on which you want to run analyses.
# 2. Analyses - analyses you want to run on benchmarks.
# 3. Options  - system properties you want to pass to Chord. There are four levels of options:
# Higest priority options: those passed on the command line of this script, using "-D key=val" syntax.
# Second priority options: those defined in bench_options_map below. They are options specific to an (analysis, benchmark) pair.
# Third  priority options: those defined in local_options_map below. They are options specific to an analysis (but independent of the benchmark).
# Lowest priority options: those defined in global_options below. They are options independent of both the analysis and benchmark.

my $chord_main_dir = &getenv("CHORD_MAIN");
my $r2_dir = &getenv("R2_ANALYSIS");
my $r2_bench_dir = &getenv("R2_BENCH");
#jspark
my $chord_max_heap = &getenv("CHORD_MAX_HEAP");
my $chord_bddbddb_max_heap = &getenv("CHORD_BDDBDDB_MAX_HEAP"); 
#krapsj

# Map from program name to program directory relative to $pjbench_dir
my %benchmarks = (
    "scimark2" => "scimark2",
		"fft" => "fft",
		"lu" => "lu",
		"smm" => "smm",
		"sor" => "sor",
		"mc" => "mc",
    "simple" => "simple",
		"zxing" => "zxing",
		"sobel" => "sobel",
		"jmeint" => "jmeint",
		"simpleRaytracer" => "simpleRaytracer",
		"boofcv" => "boofcv"
);
my @programs = keys %benchmarks;

my @analyses = ("r2");

# Lowest priority options, but still higher than $chord_main_dir/chord.properties
my @global_options = (
    "-Dchord.ext.java.analysis.path=$r2_dir/classes/",
    "-Dchord.ext.dlog.analysis.path=$r2_dir/src/",
    "-Dchord.max.heap=$chord_max_heap",
    "-Dchord.bddbddb.max.heap=$chord_bddbddb_max_heap"
);

# Medium priority options
my %local_options_map = (
    "r2" =>
        [
            "-Dchord.reflect.kind=dynamic",
            "-Dchord.ssa.kind=nophi",
            "-Dchord.rhs.merge=pjoin",
            "-Dchord.rhs.trace=shortest",
            "-Dchord.run.analyses=cipa-0cfa-dlog,r2-metaback-java",
						"-Dchord.scope.exclude=com.sun.,com.oracle.,sun.",
#				    "-Dchord.check.exclude=java.,com.,sun.,sunw.,javax.,launcher.,org.",				# set at runpl.sh 
#					  "-Dchord.check.exclude=java.,com.sun.,sun.,sunw.,javax.,launcher.,org.", 		# set at runpl.sh
        ],

);

# Higher priority options, but lower than @cmdline_options below, which are highest.
my %bench_options_map = (
    "thresc_metaback" =>
        {
            "r2" => [ ]
   	},

);

######################################################################################
# Process command line arguments

my $help = 0;
my $mode;
my $chosen_program;
my $chosen_analysis;
my $master_host;
my $master_port;
my $num_workers;
my @cmdline_options;

GetOptions(
    "mode=s" => \$mode,
    "program=s" => \$chosen_program,
    "analysis=s" => \$chosen_analysis,
    "host=s" => \$master_host,
    "port=i" => \$master_port,
    "workers=i" => \$num_workers,
    "D=s" => \@cmdline_options
);

my $error = 0;

my @modes = ("master", "worker", "parallel", "serial");
if (!grep {$_ eq $mode} @modes) {
    print "ERROR: expected mode=one of: @modes\n";
    $error = 1;
}

if (!grep {$_ eq $chosen_program} @programs) {
    #print "ERROR: expected program=one of: @programs\n";
	print "WARNING: not one of the default expected program: @programs\n";
    #$error = 1;
}

if (!grep {$_ eq $chosen_analysis} @analyses) {
    print "ERROR: expected analysis=one of: @analyses\n";
    $error = 1;
}

if ($mode eq "master" || $mode eq "worker" || $mode eq "parallel") {
    if (!$master_host) {
        $master_host = "localhost";
        print "WARN: 'host' undefined, setting it to $master_host\n";
    }
    if (!$master_port) {
        $master_port = 8888;
        print "WARN: 'port' undefined, setting it to $master_port\n";
    }
}

if ($mode eq "worker" || $mode eq "parallel") {
    if ($num_workers <= 0) {
        print "ERROR: expected workers=<NUM WORKERS>\n";
        $error = 1;
    }
}

if ($error) {
    print "Usage: $0 -mode=[@modes] -program=[@programs] -analysis=[@analyses] -D key1=val1 ... -D keyN=valN\n";
    exit 1;
}

@cmdline_options = map { "-D$_" } @cmdline_options;
print "INFO: Command line system properties: @cmdline_options\n";

######################################################################################

my $chord_jar_file = "$chord_main_dir/chord.jar";
my $local_options = $local_options_map{$chosen_analysis};
if (!$local_options) { @$local_options = (); }

my $bench_dir;

$bench_dir = "$r2_bench_dir/$benchmarks{$chosen_program}";

my $bench_options = $bench_options_map{$chosen_analysis}{$chosen_program};
if (!$bench_options) { @$bench_options = (); }
# NOTE: order of cmdline, bench, local, global options on following line is important
my @options = (@global_options, @$local_options, @$bench_options, @cmdline_options);
@options = map { s/\${chord.work.dir}/$bench_dir/; $_ } @options;
unshift (@options, "-Dchord.work.dir=$bench_dir");
given ($mode) {
    when("master") {
        &run_master(@options);
    }
    when("worker") {
        &run_worker(@options);
    }
    when("parallel") {
        &run_master(@options);
        &run_worker(@options);
    }
    when("serial") {
        &run_serial(@options);
    }
    default { die "Unknown mode: $mode\n"; }
}

######################################################################################

sub run_serial {
    my @final_options = ("-Dchord.out.dir=./chord_output_$chosen_analysis", @_);
    runcmd_in_background(@final_options);
}

sub run_master {
    my @final_options = (("-Dchord.parallel.mode=master", "-Dchord.parallel.host=$master_host", "-Dchord.parallel.port=$master_port",
        "-Dchord.out.dir=./chord_output_$chosen_analysis/Master"), @_);
    runcmd_in_background(@final_options);
}

sub run_worker {
    my @final_options = (("-Dchord.parallel.mode=worker", "-Dchord.parallel.host=$master_host", "-Dchord.parallel.port=$master_port"), @_);
    for (my $i = 1; $i <= $num_workers; $i++) {
        runcmd_in_background("-Dchord.out.dir=./chord_output_$chosen_analysis/Worker$i", @final_options);
    }
}

sub runcmd_in_background {
    my @cmd = getcmd(@_);
    my $cmd_str = join(" ", @cmd) . " &";
    print "INFO: Running command: $cmd_str\n";
    system($cmd_str);
}

sub getcmd {
    return ("nohup", "java", "-cp", $chord_jar_file, @_, "chord.project.Boot");
}

sub getenv {
    my $key = $_[0];
    my $val = $ENV{$key};
    if (!$val) {
        print "ERROR: Environment variable '$key' undefined.\n";
        exit 1;
    }
    return $val;
}

