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
#   vccFileStateEvaluator.pl - Monitor daemon processes and restart as necessary
#
#  AUTHOR(S)
#   1. Greg Howard; greg.howard@att.com; 607-257-1575 (current implementation)
#	2. Vinodh Pemmasani; vp663p@att.com; 973 396 2607 (added log rollover)
#
#  SYNOPSIS
#   vccFileStateEvaluator.pl -f <file>         [-h|?] [-l <level>]
#                            -s <state_list>
#                            -n <new_state>
#                            -r <extension>
#                            -a
#                            -e <application>
#
#    ? Print help.
#
#   -h Same as '?'.
#
#   -f Specify the name of a file for which you'd like to look for 
#      state files
#
#   -s Specify a list of states (i.e. file extensions) that will be
#      looked for wrt the named file
#
#   -n Specify a new state (i.e. file extension) that you'd like to
#      be use'd in creating a new state file for <file> (from -f)
#
#   -r Specify a string representing an extension that would be at
#      the end of <file> which you want to REPLACE with <new_state>
#   
#   -a If present, this option says you just want to append <new_state>
#      to <file> (i.e. you aren't concerned with replacing any exisiting
#      extension
#
#   -e Specify the name of a script or application to be executed via
#      system(); Make sure you include the full path of the script if
#      you're not sure if PATH is set to find it
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
#   Example(s):
#   vccFileStateEvaluator.pl ... update later
#   vccFileStateEvaluator.pl ... update later
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
#    FSE_LOG_DIR
#
#  NOTES
#   1. Log file will be written to $FSE_LOG_DIR and have a naming
#      convention of: fse.log
#   2. Log file will be APPENDED to so that you don't have a ton
#      of small log files (depending on how often the File State
#      Evaluator is called).
#   3. Log file will be rolled over on a daily or weekly basis with
#      the rolled over log files having the naming convention of:
#      fse_YYYYMMDDhhmmss.log
#
#  TODO LIST
#   1. ...
#
#  REVISION HISTORY
#   mm/dd/yyyy (attuid) -   added .... 
#   mm/dd/yyyy (attuid) -   fixed .... 
#   mm/dd/yyyy (attuid) - updated .... 
#   mm/dd/yyyy (attuid) - removed .... 
#
#   11/25/2007 (gh1363) - created original incarnation of fileStateEvaluator.pl
#   05/26/2009 (gh1363) - Fixed/Updated The log file was never rolled over so added the code to rollover the log
#
#   09/17/2015 (gh1363) -  ported original version to VEC platform
#
#   09/17/2015 (gh1363) -  ported VEC version to VCC platform
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
my $default_log_path = $ENV{'VCC_HOME'} . "/logs/DCAE/fse"; 
my $env_log_path = $ENV{'FSE_LOG_DIR'};
my $log_path_used = "";
my $log_level = "info";      # default log level, cmdline arg may change this

# Command Line Option Globals
my $cloFile;
my $cloStateList;
my $cloNewState;
my $cloExtensionToReplace;
my $cloAction;

# Extension Behavior Flag
# "append" means just append <new_state> to <file>
# "replace" means replace <extension> at end of <file> with <new_state>
my $extensionBehavior;

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

usage: $cmd [-h|?] -f <file> -s <state_list> -n <new_state> -r <extension> -a -e <application> [-l <level>]

-h       : this (help) message
 ?       : this (help) message
-f file  : file to perform state evaluation on
-s list  : list of file states that must exist to perform action
-n state : new state to create when all states in list exist
-r ext   : extension to be replaced (if any)
-a       : indicates <new_state> should be appended to <file>
-e app   : application to execute when all states in list exist
-l level : log level ["debug" only so far]

example 1: $cmd -f ./test.txt -s "notified,completed" -r "txt" -n "done" -e "echo \\"changed state\\""

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
	# fse.log

	# It's intended that the log file get rolled over 
	# by some external mechanism (i.e. logRollover.pl)
	# Suggested rollover naming convention:
	# fse-YYYYMMDDhhmmss.log

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
		#my $logname = "fse.log";
		#VINODH Adding log rollover
		my $now = time();
		my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime($now);
		my $ss = sprintf("%02d", $sec);
		my $mm = sprintf("%02d", $min);
		my $hh = sprintf("%02d", $hour);
		my $DD = sprintf("%02d", $mday);
		my $MM = sprintf("%02d", ($mon + 1));
		my $YYYY = $year + 1900;

		# Create and open log file
		my $logname = "fse_"."$YYYY$MM$DD"."_$hh".".log";
		open(LOGFILE, ">> $log_path_used/$logname");
		LOGFILE->autoflush(1);
	}
	else { print STDERR "\nCan't write to: $log_path_used\n"; return -1; }

	# Add log inititializtion message
	my $start_time = localtime(time());
	print LOGFILE "\n---\n\n$start_time File State Evaluator STARTED\n";

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
	print LOGFILE "\n$end_time File State Evaluator ENDED\n";

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

	if ( "$log_level" eq "debug" ) { print LOGFILE "\nEntered: process_cmdline_opts()\n"; }

	# vccFileStateEvaluator.pl -f <file>         [-h|?] [-l <level>]
	#                          -s <state_list>
	#                          -n <new_state>
	#                          -r <extension>
	#                          -a
	#                          -e <application>
	my $opt_string = "f:s:n:r:e:l:ha";

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

	# Ensure -s specified, it's REQUIRED
	if ( $opt{s} ne "" ) 
	{ 
		if ( "$log_level" eq "debug" ) { print LOGFILE "\nOption specified: -s $opt{s}\n"; }
		$cloStateList = "$opt{s}";
	}
	else { usage("Required opt (-s) not specified"); return 52; }

	# Ensure -n specified, it's REQUIRED
	if ( $opt{n} ne "" ) 
	{ 
		if ( "$log_level" eq "debug" ) { print LOGFILE "\nOption specified: -n $opt{n}\n"; }
		$cloNewState = "$opt{n}";
	}
	else 
	{ 
		if ( $opt{e} eq "" ) 
		{
			usage("Must specify -n <new_state> and/or -e <action>"); return 53; 
		}
	}

	# Was -r specified?
	if ( $opt{r} ne "" ) 
	{ 
		if ( "$log_level" eq "debug" ) { print LOGFILE "\nOption specified: -r $opt{r}\n"; }
		$cloExtensionToReplace = "$opt{r}";
		$extensionBehavior = "replace";
		
		# -r was specified, so -a can't be specified
		if ( $opt{a} ) 
		{
			usage("Can only specify -r <ext> OR -a"); return 54; 
		}
	}
	else 
	{ 
		# -r was NOT specified, so -a MUST be specified
		if ( !$opt{a} ) 
		{
			usage("Must specify -r <ext> or -a"); return 55; 
		}
	}

	# Was -a specified?
	if ( $opt{a} ) 
	{ 
		if ( "$log_level" eq "debug" ) { print LOGFILE "\nOption specified: -a\n"; }
		$extensionBehavior = "append";
		
		# -a was specified, so -r can't be specified
		if ( $opt{r} ne "" ) 
		{
			usage("Can only specify -a OR -r <ext>"); return 56; 
		}
	}
	else 
	{ 
		# -a was NOT specified, so -r MUST be specified
		if ( $opt{r} eq "" ) 
		{
			usage("Must specify -a or -r <ext>"); return 57; 
		}
	}

	# Ensure -e specified, it's REQUIRED
	if ( $opt{e} ne "" ) 
	{ 
		if ( "$log_level" eq "debug" ) { print LOGFILE "\nOption specified: -e $opt{e}\n"; }
		$cloAction = "$opt{e}";
	}
	else 
	{ 
		if ( $opt{n} eq "" ) 
		{
			usage("Must specify -e <action> or -n <new_state>"); return 58; 
		}
	}

	# Only thing that should be leftover on command line might be '?"
	# Otherise ignore it
	my $cl_leftover = $ARGV[0];
	if ( "$cl_leftover" eq "?" ) { usage("? specified"); return 33; }
	if ( "$cl_leftover" ne "" ) { usage("Unknown argument: $cl_leftover"); return 37; }

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


# Build array of state files to be looked for
my ( @state_files_to_match ) = split /,/, $cloStateList;
if ( "$log_level" eq "debug" ) 
{
	print LOGFILE "\nState files to look for:\n";
	foreach ( @state_files_to_match ) 
	{
		print LOGFILE "$_\n";
	} 
}

# Look for each of the state files in @state_files_to_match
# using:
# 1. $cloFile.$state_files_to_match[$index]
# 2. ($cloFile w/o $extensionToReplace).$state_files_to_match[$index]

my $file_wo_extensionToReplace = $cloFile;
$file_wo_extensionToReplace =~ s/.$cloExtensionToReplace//;
if ( "$log_level" eq "debug" ) 
{ 
	print LOGFILE "\n\$file_wo_extensionToReplace: $file_wo_extensionToReplace\n"; 
}

my $matched_all_states = "false";
my $state_files_to_match_size = scalar @state_files_to_match;

print LOGFILE "\nFile: $cloFile\n";
print LOGFILE "States to find: $cloStateList\n";

for ( my $i=0; $i < $state_files_to_match_size; $i++ ) 
{
	if ( -e "$file_wo_extensionToReplace.$state_files_to_match[$i]" ) 
	{
		if ( "$log_level" eq "debug" ) { print LOGFILE "Found: $file_wo_extensionToReplace.$state_files_to_match[$i]\n"; }
		$matched_all_states = "true";
		next;
	}
	elsif ( -e "$cloFile.$state_files_to_match[$i]" ) 
	{
		if ( "$log_level" eq "debug" ) { print LOGFILE "Found: $cloFile.$state_files_to_match[$i]\n"; }
		$matched_all_states = "true";
		next;
	}
	else
	{
		if ( "$log_level" eq "debug" ) 
		{ 
			print LOGFILE "Could not find: $file_wo_extensionToReplace.$state_files_to_match[$i]\n"; 
			print LOGFILE "Could not find: $cloFile.$state_files_to_match[$i]\n"; 
		}
		$matched_all_states = "false";
		last;
	}
} 

if ( "$matched_all_states" eq "false" ) 
{
	common_exit("255", "Not all specified states were found.");
}

# If we've reached this far, all of the specified state files
# associated with the specified file were found... create any
# new state file requested and/or perform specfied action

print LOGFILE "All of the specified states were found.\n";

if ( "$cloNewState" ne "" ) 
{
	if ( "$extensionBehavior" eq "replace" ) 
	{
		print LOGFILE "Creating: $file_wo_extensionToReplace.$cloNewState\n";
		system("> $file_wo_extensionToReplace.$cloNewState");
	}
	elsif ( "$extensionBehavior" eq "append" )
	{
		print LOGFILE "Creating: $cloFile.$cloNewState\n";
		system("> $cloFile.$cloNewState");
	}
}

if ( "$cloAction" ne "" ) 
{
	print LOGFILE "Executing: $cloAction\n";
	system("$cloAction");
}

##
## Reached end of script, normal termination ... 
##

common_exit(0, "End of script");

# End of Main
