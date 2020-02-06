#!/usr/bin/env ksh
#==========================================================#
# Set terminal options                                     #
#==========================================================#

test -t && stty erase "^h" echo echoe echok ixon -ixany ixoff tabs

#==========================================================#
# set vi editor options                                    #
#==========================================================#

set -o vi
TERM=vt100
EDITOR=vi; export EDITOR TERM
EXINIT="set showmode ai tabstop=2| map g 1G| map \ i ^[l"
export EXINIT

# for when no pec installed, set PEC_HOME for install script
export PEC_ADMIN=${HOME%%/admin}/admin
export PEC_HOME=$HOME

#==========================================================#
# set misc vars                                            #
#==========================================================#

TERMINFO=$PEC_HOME/terminfo; export TERMINFO
HOST=`uname -n`; export HOST
HISTFILE=$PEC_HOME/.ksh_history; export HISTFILE
HISTSIZE=4096; export HISTSIZE

#PS1='$HOST:$LOGNAME> '; export PS1
#PS1='$HOST':'[${PWD}]>>'; export PS1
PS1='$HOST':'${PWD}>'; export PS1
PS2="$(tput bold)more? :$(tput sgr0) "; export PS2

VISUAL=${EDITOR}; export VISUAL
FCEDIT=${EDITOR}; export FCEDIT
SHELL=/usr/bin/ksh; export SHELL
KSHRC=$PEC_HOME/.kshrc; export KSHRC
ENV=$KSHRC; export ENV

LS_COLORS='no=00:fi=00:di=01;34:ln=01;36:pi=40;33:so=01;35:do=01;35:bd=40;33;01:cd=40;33;01:or=40;31;01:ex=01;32:*.tar=01;31:*.tgz=01;31:*.arj=01;31:*.taz=01;31:*.lzh=01;31:*.zip=01;31:*.z=01;31:*.Z=01;31:*.gz=01;31:*.bz2=01;31:*.deb=01;31:*.rpm=01;31:*.jar=01;31:*.jpg=01;35:*.jpeg=01;35:*.gif=01;35:*.bmp=01;35:*.pbm=01;35:*.pgm=01;35:*.ppm=01;35:*.tga=01;35:*.xbm=01;35:*.xpm=01;35:*.tif=01;35:*.tiff=01;35:*.png=01;35:*.mov=01;35:*.mpg=01;35:*.mpeg=01;35:*.avi=01;35:*.fli=01;35:*.gl=01;35:*.dl=01;35:*.xcf=01;35:*.xwd=01;35:*.ogg=01;35:*.mp3=01;35:*.wav=01;35:'
export LS_COLORS

#==========================================================#
# Environment Variables should be defined in vcc_env       #
#==========================================================#

PATH=/bin:/usr/bin:/usr/local/bin:/usr/sbin
export PATH

. $HOME/bin/common/vcc_env

tty -s
stty sane
stty erase ^?
export GREP_OPTIONS='--color=auto'

alias rm='/bin/rm -i'

