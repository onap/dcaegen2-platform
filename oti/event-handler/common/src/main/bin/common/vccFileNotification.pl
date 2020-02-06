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
#   vccFileNotification.pl - Send notification of a file to process to a VEC 
#                            daemon process; ported from PEC environment
#
#  AUTHOR(S)
#   1. Greg Howard; greg.howard@att.com; 607-257-1575 
#
#  SYNOPSIS
#   vccFileNotification.pl [-h|?] -p <port> -m <module> -f <filename> -d
#                                 -l <file_list> -x <extension>
#
#    ? Print help.
#
#   -h Same as '?'.
#
#   -p Specify port to connect to ...
#      Assumption for now is port/process will be on "this host"
#      Once config file is implemented you can JUST specify module name
#      and port will be looked up, but you can also explicitly specify
#      the port which will override the lookup
#
#   -f Specify name of file to be contained in notification message to 
#      the specified process(port), this must be a FULL path and file
#      specification
#   
#   -l Specify a LIST of full path and file specifications (space separated)
#   
#   -m Specify module abbreviation (trap, pp, etc.) 
#      Once config file is implemented you can JUST specify module name
#      and port will be looked up, but you can also explicitly specify
#      the port which will override the lookup
#
#   -x Specify the extension that you need to look for and remove before
#      adding the state suffix
#
#   -d Turns on debugging output which is printed to STDERR 
#      (no log is kept)
#
#   Example(s):
#   vccFileNotification.pl -p $TRAP_PORT -m trap -f /opt/pec/data/output/alcatel/15min/PM_20071101150000_IPNSEP_br7mo01ems_rlpd003.txt
#   vccFileNotification.pl -p 5555 -m pp -d -f /opt/pec/data/output/alcatel/15min/PM_20071101150000_IPNSEP_br7mo01ems_rlpd003.txt 
#   vccFileNotification.pl -p 4242 -m test -f ./abc.txt -x "txt"
#
#  DESCRIPTION
#    vccFileNotification.pl takes a port spec and full path/file spec and
#    connects to the port and sends one of the following:
#
#    FILE_NOTIFICATION:<filePathAndName>
#    FILE_LIST:<filePathAndName1>,<filePathAndName2>,<filePathAndName3> ...
#
#  RELATED COMMANDS
#
#    None
#
#  ENVIRONMENT VARIABLES USED
#  
#    None
#
#  CONFIGURATION FILE(S)
#  
#    $PEC_CONFIG_DIR/pec_portmapper.cfg
#
#    <processName>|<processPort>|<systemID>(hostname or IP address or localhost)
#     
#    pp|5555|localhost
#    trap|4444|localhost
#
#  NOTES
#   1. Config file isn't in use just yet... but will be shortly
#
#  REVISION HISTORY
#   mm/dd/yyyy -   added .... 
#   mm/dd/yyyy -   fixed .... 
#   mm/dd/yyyy - updated .... 
#   mm/dd/yyyy - removed .... 
#
#   11/28/2007 - created original incarnation of vccFileNotification.pl
#
#   11/29/2007 -   added check to ensure duplicate notifications are 
#                        not sent for specified file(s)
#
#   11/29/2007 -   added optional extension spec to indicate what extension
#                        to look for and remove if desired (otherwise the
#                        state "extension" will just be added to the end of
#                        the filename as it's received
#
#   04/03/2007 -   fixed connection logic; no connection should be opened
#                        unless it's certain that a file notification will
#                        take place
#
#   09/14/2015 -  ported to VEC environment
#
#   06/06/2017 -  ported to VCC environment
#
#  TODO LIST
#   1. Implement the use of a config file wrt mapping a given module that
#      is specified to a port using the config file described earlier
#   2. When config file is used, allow multiple notifications.  That would
#      mean that you could have -m <module_list> (e.g. -m delta,rtda,send2ddb);
#      I suppose that I could allow -p <port_list> and -m <module_list> but
#      then the user has to make sure they're paired up right... 
#

# Try to force some reasonable coding constraints
use strict;

# for command line options processing
use Getopt::Std;

# Base IO obj class, used here for autoflush
use IO::Handle;

# Duh, socket interface
use IO::Socket;

# for easy file OO open with file handle
use IO::File;

# for mkpath
use File::Path;

# for basename() and dirname()
use File::Basename;


# ----------------
# Global variables 
# ----------------

use IO::Socket;

my $socket;

# Globals for host this
my $this_hostname_maybe_domain;
my $this_hostname;
my $this_domain;

# Debug flag
my $debug = "off";

# Command Line Option variables
my $cloFileName;
my $cloFileList;
my $cloModule;
my $cloExtension = "none";
my $cloPort;

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
	print STDERR  "\n$_[0], printing usage and exiting\n";

	# Print standard usage message
	my $cmd = basename($0);
	print STDERR << "EOF";

usage: $cmd [-h|?] -p <port>|<process>  -f <file> -l <file_list> -d

-h       : this (help) message
 ?       : this (help) message
-p port  : port (or process abbrev) to connect to
-m mod   : module name (e.g. trap, pp, etc.)
-x ext   : extension to be stripped before appending state suffix 
	       (don't include the '.')
-f file  : file to include in notification
-l list  : list of files to include in notification (not yet implemented)
-d       : turn on debugging

example 1: $cmd -p \$TRAP_PORT -m trap -f /opt/pec/data/output/alcatel/15min/PM_20071101150000_IPNSEP_br7mo01ems_rlpd003.txt
example 2: $cmd -p 5555 -m pp -d -f /opt/pec/data/output/alcatel/15min/PM_20071101150000_IPNSEP_br7mo01ems_rlpd003.txt
example 3: $cmd -p 4242 -m test -f ./abc.txt -x "txt"

EOF
}

sub process_cmdline_opts
{
	# Args
	# None

	# Return value(s)
	#  0 = Processed all options successfully
	# !0 = Error or user just wanted help

	# vccFileNotification.pl [-h|?] -d -f <filename>|<file_list> -p <port>|<process>
	my $opt_string = "hf:p:l:m:x:d";

	# get options from command string
	getopts( "$opt_string", \my %opt ) or usage("Invalid command line option") and return 31;

	# if -h (help) option, print usage and exit
	usage("-h specified") and return 37 if $opt{h};

	# See if -f specified, either -f OR -l is required but you
	# can't specify both (if you do, the -l will take precedence)
	if ( $opt{d} ) 
	{ 
		$debug = "on";
		if ( "$debug" eq "on" ) { print STDERR "\nOption specified: -d\n"; }
	}

	# See if -f specified, either -f OR -l is required but you
	# can't specify both (if you do, the -l will take precedence)
	if ( $opt{f} ne "" ) 
	{ 
		if ( "$debug" eq "on" ) { print STDERR "\nOption specified: -f $opt{f}\n"; }
		$cloFileName = "$opt{f}";
	}

	# See if -x specified
	if ( $opt{x} ne "" ) 
	{ 
		if ( "$debug" eq "on" ) { print STDERR "\nOption specified: -x $opt{x}\n"; }
		$cloExtension = "$opt{x}";
	}

	# Ensure -p specified, it's REQUIRED (for now)
	if ( $opt{p} ne "" ) 
	{ 
		if ( "$debug" eq "on" ) { print STDERR "\nOption specified: -p $opt{p}\n"; }
		$cloPort = "$opt{p}";
	}
	else { usage("Required opt (-p) not specified"); return 37; }

	# See if -l specified, either -f OR -l is required
	# but you can't specify both
	if ( $opt{l} ne "" ) 
	{ 
		if ( "$debug" eq "on" ) { print STDERR "\nOption specified: -l $opt{l}\n"; }
		$cloFileList = "$opt{l}";
	}

	# See if -m specified, it's REQUIRED
	if ( $opt{m} ne "" ) 
	{ 
		if ( "$debug" eq "on" ) { print STDERR "\nOption specified: -m $opt{m}\n"; }
		$cloModule = "$opt{m}";
	}

	# Must have either a file or file list spec
	if ( $opt{f} eq "" && $opt{l} eq "" ) 
	{
		usage("Must specify -f or -l option"); 
		return 37;
	}

	# Only thing that should be leftover on command line might be '?"
	# Otherise ignore it
	my $cl_leftover = $ARGV[0];
	if ( "$cl_leftover" eq "?" ) { usage("? specified"); return 37; }
	if ( "$cl_leftover" ne "" ) { usage("Unknown argument: $cl_leftover"); return 37; }

	return 0;
}

sub get_this_hostname
{
	# Args
	# None

	# Return value(s)
	# None

	if ( "$debug" eq "on" ) { print STDERR "\nEntered: get_this_hostname()\n"; }

	# Find out what host this DM instance is running on
	$this_hostname_maybe_domain = `hostname -f`;

	# Get rid of newline  
	chomp($this_hostname_maybe_domain);

	if ( "$debug" eq "on" ) { print STDERR "Result of \`hostname -f\`: $this_hostname_maybe_domain\n"; }

	( $this_hostname, $this_domain ) = split(/\./, $this_hostname_maybe_domain, 2);

	if ( "$debug" eq "on" ) 
	{ 
		print STDERR "\nThe hostname portion: $this_hostname\n"; 
		print STDERR "The   domain portion: $this_domain\n"; 
	}

	# Success
	return 0;
}


# ----------------------------
# **********  MAIN  ********** 
# ----------------------------

# Do command line processing
# Sets:
# $cloFileName or $cloFileList
# $cloPort
# $debug
exit if process_cmdline_opts() ne "0";

# Get local host's name
# Sets:
# $this_hostname_maybe_domain
# $this_hostname
# $this_domain
get_this_hostname();

# Could do file validation here, but
# this is typically invoked by the 
# Delivery Manager that only get's the
# names from doing a glob() which means
# that the file definitely exists.
# However if I did wanted to do validation...
# write validate_file_existance();

# FIX: Connection really shouldn't happen until it's 
#      been decided that a message will definitely be sent


# Send appropriate message and then write state file
# indicating that message was successfully sent
my $stateFile;
if ( "$cloFileList" ne "" ) 
{
	my @listArray = ();
	my $notificationList = ();
	# This section for LIST OF FILES
	# IMPORTANT:
	# 1. Need to check each file in the list
	# 2. Could have some files that notifications were already sent for
	#    and some files for which no notifications were sent
	# 3. Need to build new list of files to include in the notification 
	#    based on previous notification files and of course write notification
	#    state files for just those files (although it doesn't hurt to 
	#    overwrite the state file)
	#
	# Make it work with either spaces separated 
	# or comma separated lists by changing spaces
	# to commas before the split; notification will
	# always have commas for list of files
	$cloFileList =~ s/ /\,/g;
	#
	( @listArray ) = split /,/, $cloFileList;
	foreach ( @listArray ) 
	{
		# Does the file even exist?
		# If not, don't do notification
		if ( ! -e "$_" ) 
		{
			if ( "$debug" eq "on" ) { print STDERR "\nFile in list doesn't exist ($_), ignoring it.\n"; }
			next;
		}
		$stateFile = $_;
		$stateFile =~ s/.$cloExtension$//;
		if ( "$debug" eq "on" )
		{ 
			print STDERR "\n\$stateFile=$stateFile\n"; 
			print STDERR "\n\${stateFile}.\${cloModule}_notified=${stateFile}.${cloModule}_notified\n"; 
		}
		if ( -e "${stateFile}.${cloModule}_notified" ) 
		{
			if ( "$debug" eq "on" ) { print STDERR "\nState file already exists, no notification will be done\n"; }
			next;
		}
		$notificationList .= "${_},";
	}
	# Get rid of trailing comma if any
	$notificationList =~ s/,$//;
	if ( "$debug" eq "on" ) { print STDERR "\nNotification list is: $notificationList\n"; }
	if ( "$notificationList" eq "" ) 
	{
		if ( "$debug" eq "on" ) { print STDERR "\nNotification list is null, exiting.\n"; }
		exit;
	}
	#
	# Connect to specified port on local(this) host
	if ( "$debug" eq "on" ) { print STDERR "\nAttempting connection to: $cloPort ...\n"; }
	#
	$socket = IO::Socket::INET->new(PeerAddr => "$this_hostname",
									   PeerPort => $cloPort,
									   Proto => "tcp",
									   Type => SOCK_STREAM) 
				 or die "\nCouldn't establish connection to: $cloPort";
	#
	if ( "$debug" eq "on" ) { print STDERR "\nSending msg: FILE_LIST:$notificationList\n"; }
	print $socket "FILE_LIST:$notificationList" or exit 37;
	( @listArray ) = split /,/, $notificationList;
	foreach ( @listArray ) 
	{
		$stateFile = $_;
		$stateFile =~ s/.$cloExtension$//;
		if ( "$debug" eq "on" ) { print STDERR "\nWriting state file: ${stateFile}.${cloModule}_notified\n"; }
		system("> ${stateFile}.${cloModule}_notified");
	}
}
elsif ( "$cloFileName" ne "" )
{
	# Does the file even exist?
	# If not, don't do notification
	if ( ! -e "$cloFileName" ) 
	{
		if ( "$debug" eq "on" ) { print STDERR "\nFile doesn't exist ($cloFileName), exiting without notification.\n"; }
		exit;
	}

	# This section for SINGLE FILE NOTIFICATION
	$stateFile = $cloFileName;
	$stateFile =~ s/.$cloExtension$//;
	if ( -e "${stateFile}.${cloModule}_notified" ) 
	{
		if ( "$debug" eq "on" ) { print STDERR "\nState file already exists, no notification will be done\n"; }
		exit;
	}
	else
	{
		# Connect to specified port on local(this) host
		if ( "$debug" eq "on" ) { print STDERR "\nAttempting connection to: $cloPort\n"; }
		#
		$socket = IO::Socket::INET->new(PeerAddr => "$this_hostname",
										   PeerPort => $cloPort,
										   Proto => "tcp",
										   Type => SOCK_STREAM) 
					 or die "\nCouldn't establish connection to: $cloPort";
		#
		print $socket "FILE_NOTIFICATION:$cloFileName" or exit 37;
		if ( "$debug" eq "on" ) 
		{ 
			print STDERR "\nMsg sent: FILE_NOTIFICATION:$cloFileName\n"; 
		}
		system("> ${stateFile}.${cloModule}_notified") or exit 37;
		if ( "$debug" eq "on" ) 
		{ 
			print STDERR "\nWrote state file: ${stateFile}.${cloModule}_notified\n"; 
		}
	}
}

close($socket);

exit 0;
