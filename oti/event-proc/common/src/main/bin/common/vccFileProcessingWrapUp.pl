#!/usr/bin/env perl
# ============LICENSE_START=======================================================
# Copyright (c) 2020 AT&T Intellectual Property. All rights reserved.
# ================================================================================
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ============LICENSE_END=========================================================

#############################################################################
#
# IMPORTANT: If you change this file ... 
#
#  1. Add your name to the AUTHOR(S) list with contact info!
#  2. Update the SYNOPSIS section if you change command line options
#  3. Update the DESCRIPTION section 
#  4. Update the REVISION HISTORY section with the date you began
#     your changes and a brief description of what they were.
#  5. If you were really nice you'd comment your changes and add your
#     initials so it's easy to find what got modified.
#
#############################################################################

#  NAME
#   vccFileProcessingWrapUp.pl - File housekeeping after all VCC file 
#                                processing is finished
#
#  AUTHOR(S)
#   1. Greg Howard; greg.howard@att.com; 607-257-1575 (current implementation)
#
#  SYNOPSIS
#   vccFileProcessingWrapUp.pl [-h|?] -f <file> ext=<extension> [-l <level>]
#
#    ? Print help.
#
#   -h Same as '?'.
#
#   -f Specify the name of a file for which all VCC processing is finished 
#
#   -l Set logging to specified level, which should be one of:
#      debug
#      info
#      warn
#      error
#      fatal
#   
#      NOTE: The logging feature will not be fully implemented until
#            log4perl is installed and made available. Initially there
#            will only be a debug level and then a default "always log"
#            message level.
#
#   ext=<extension>
#
#      Specifies the extension portion of <file> (if any) that should be
#      ignored when looking for associated state files. For example, if
#      the original file was named "abc.txt" and you had state files named
#      "abc.state1", "abc.state2", etc. then you'd want to specify
#      ext=txt to ignore the ".txt" portion of the original file name and 
#      look for all files named "abc.*"
#
#   Example(s):
#   vccFileProcessingWrapUp.pl -f ./test.txt ext="txt"
#   vccFileProcessingWrapUp.pl -f ./test.txt ext="txt" -l debug
#
#  DESCRIPTION
#    Update later. 
#
#  RELATED COMMANDS
#
#    None
#
#  ENVIRONMENT VARIABLES USED
#  
#    FPWU_LOG_DIR
#
#  NOTES
#   1. Log file will be written to $FPWU_LOG_DIR and have a naming
#      convention of: fpwu.log
#   2. Log file will be APPENDED to so that you don't have a ton
#      of small log files (depending on how often the file processing
#      finished script runs.. which will be very frequently)
#   3. Log file will be rolled over on a hourly or daily basis with
#      the rolled over log files having the naming convention of:
#      fpwu_YYYYMMDDhhmmss.log
#
#  TODO LIST
#   1. ...
#
#  REVISION HISTORY
#   mm/dd/yyyy -   added .... 
#   mm/dd/yyyy -   fixed .... 
#   mm/dd/yyyy - updated .... 
#   mm/dd/yyyy - removed .... 
#
#   11/25/2007 - created original incarnation of pecFileProcessingWrapUp.pl
#
#   06/06/2017 -  ported to VCC environment as vccFileProcessingWrapUp.pl
#

# Try to force some reasonable coding constraints
use strict;

# for command line options processing
use Getopt::Std;

# Base IO obj class, used here for autoflush
use IO::Handle;

# for easy file OO open with file handle
use IO::File;

# for mkpath
use File::Path;

# for basename() and dirname()
use File::Basename;


# ----------------
# Global variables 
# ----------------

# Logging globals
my $default_log_path = $ENV{'VCC_HOME'} . "/logs/DCAE/fpwu"; 
my $env_log_path = $ENV{'FPWU_LOG_DIR'};
my $log_path_used = "";
my $log_level = "info";      # default log level, cmdline arg may change this

# Command Line Option Globals
my $cloFile;
my $cloExtension;

# -------------------------
# locally defined functions
# -------------------------

sub usage
{
	# This subroutine logs a message passed to it by calling
	# statement and then prints a standard usage message; 
	# A call to this subroutine is always followed by a call
	# to the common_exit() routine very shortly afterwards

	# Args:
	# $_[0] is a message to log before exiting

	# Return values:
	# None, we're going to exit anyway

	# Log message as to why we got here
	print LOGFILE "\n$_[0], printing usage and exiting\n";
	print STDERR  "\n$_[0], printing usage and exiting\n";

	# Print standard usage message
	my $cmd = basename($0);
	print STDERR << "EOF";

usage: $cmd [-h|?] -f <file> ext=<extension> [-l <level>]

-h       : this (help) message
 ?       : this (help) message
-f file  : file for which all VCC processing is finished
-l level : log level ["debug" only so far]

ext=<extension>

example 1: $cmd -f ./test.txt ext="txt"
example 1: $cmd -f ./test.txt ext="txt" -l debug

EOF
}

sub setup_logging
{
	# Args
	# None

	# Return value(s)
	# None

	if ( "$log_level" eq "debug" ) { print LOGFILE "\nEntered: setup_logging()\n"; }

	# Work to do in future ...
	# Add logging functinality using log4perl

	# Use log name convention of:
	# fpwu.log

	# It's intended that the log file get rolled over 
	# by some external mechanism (i.e. logRollover.pl)
	# Suggested rollover naming convention:
	# dmon-YYYYMMDDhhmmss.log

	# Establish logging PATH to be used
	if ("$env_log_path" ne "") { $log_path_used = $env_log_path; }
	else { $log_path_used = $default_log_path; }

	# If log directory is not there, create it
	if ( !(-e "$log_path_used") ) 
	{
		eval { mkpath($log_path_used) };
		if ($@) 
		{
			print STDERR "Couldn't create: $log_path_used: $@\n"; 
			return -1;
		}
	}

	# Ensure log path directory is writable
	if ( -w $log_path_used ) 
	{
		# Open log file in APPEND mode
		# (creates it if necessary)
		#my $logname = "fpwu.log";
		my $now = time();
		my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime($now);
		my $ss = sprintf("%02d", $sec);
		my $mm = sprintf("%02d", $min);
		my $hh = sprintf("%02d", $hour);
		my $DD = sprintf("%02d", $mday);
		my $MM = sprintf("%02d", ($mon + 1));
		my $YYYY = $year + 1900;

		# Create and open log file
		my $logname = "fpwu_"."$YYYY$MM$DD"."_$hh".".log";
		open(LOGFILE, ">> $log_path_used/$logname");
		LOGFILE->autoflush(1);
	}
	else { print STDERR "\nCan't write to: $log_path_used\n"; return -1; }

	# Add log inititializtion message
	my $start_time = localtime(time());
	print LOGFILE "\n---\n\n$start_time File Processing Wrap-Up STARTED\n";

	# Success
	return 0;
}

sub common_exit
{
	# Args
	# $_[0] is exit value to use
	# $_[1] is message to log before exiting
	my $exit_value = $_[0];
	my $message = $_[1];

	# Return value(s)
	# None

	if ( "$log_level" eq "debug" ) { print LOGFILE "\nEntered: common_exit()\n"; }

	if ( "$message" ne "" ) 
	{
		print LOGFILE "\ncommon_exit(): $message\n";
	}

	# Add log Termination message
	my $end_time = localtime(time());
	print LOGFILE "\n$end_time File Processing Wrap-Up ENDED\n";

	# Close log file
	close(LOGFILE);

	exit("$exit_value");
}

sub process_cmdline_opts()
{
	# Args
	# None

	# Return value(s)
	#  0 = Processed all options successfully
	# !0 = Error or user just wanted help

	# vccFileProcessingWrapUp.pl [-h|?] -f <file> ext=<extension> [-l <level>]
	my $opt_string = "f:l:h";

	# get options from command string
	getopts( "$opt_string", \my %opt ) or usage("Invalid command line option") and return 31;

	# if -h (help) option, print usage and exit
	usage("-h specified") and return 33 if $opt{h};

	# Was -l was specified? [optional]
	if ( $opt{l} ne "" ) 
	{
		$log_level = $opt{l};
		if ( "$log_level" eq "debug" ) { print LOGFILE "\nOption specified: -l $opt{l}\n"; }
	}

	# Ensure -f specified, it's REQUIRED
	if ( $opt{f} ne "" ) 
	{ 
		if ( "$log_level" eq "debug" ) { print LOGFILE "\nOption specified: -f $opt{f}\n"; }
		$cloFile = "$opt{f}";

		# make sure specified file exists
		if ( ! -f "$cloFile"  ) 
		{
			usage("File specified with -f option not valid");
			return 51;
		}
	}
	else { usage("Required opt (-f) not specified"); return 51; }

	# Following getopt style parameters could be:
	# 1. "?" for help
	# 2. ext=<extension> 

	my $cl_arg = $ARGV[0];

	if ( "$cl_arg" eq "?" ) { usage("? specified"); return -1; }
	# no error, just forces common exit point from main

	if ( "$cl_arg" =~ m/^ext=/i ) 
	{
		$cl_arg =~ s/ext=//;
		$cloExtension = "$cl_arg";
	}

	# anything else on the command line will just be ignored
	# could log error and force exit, but why if everything else is kosher?

	return 0;
}

# ----------------------------
# **********  MAIN  ********** 
# ----------------------------

# Determine log name and open/create log file
# Init log fle with log created time
exit if setup_logging() < 0;

##! Any exit beyond this point should use common_exit()
##! to ensure that you log the termination time and properly
##! close the log file

# Do command line processing
# Sets delay for DM execution if desired, sets logging level, etc.
# If help options (-h|?) specified, will return with a -1
# which is the same as error return since both have same behavior.
my $clo_status = process_cmdline_opts();
if ( "$clo_status" ne "0" )
{
	common_exit($clo_status, "Exiting due to command line option errors.") 
}

# Archive the specified file

if ( system("archiver.pl -f $cloFile") ne 0 ) 
{
	print LOGFILE "\nError executing: archiver.pl $cloFile";
}

# Look for all remaining state files to be deleted

my $file_wo_extension = $cloFile;
$file_wo_extension =~ s/.$cloExtension//;

if ( "$log_level" eq "debug" ) 
{ 
	print LOGFILE "\n\$file_wo_extension: $file_wo_extension\n"; 
	print LOGFILE "\nState files:\n";
}

my @dir_list = glob("$file_wo_extension.*");
foreach ( @dir_list ) 
{
	if ( "$log_level" eq "debug" ) 
	{ 
		print LOGFILE "$_\n"; 
	}
	unlink("$_");
}

##
## Reached end of script, normal termination ... 
##

common_exit(0, "End of script");

# End of Main
