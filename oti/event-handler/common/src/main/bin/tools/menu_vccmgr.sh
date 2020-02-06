cat <<EOM

  10 - Start VCC parser Ericsson daemon  20 - Stop VCC parser Ericsson daemon
  30 - Start VCC parser ALU daemon       40 - Stop VCC parser ALU daemon
  50 - Start VCC parser Mobility daemon  60 - Stop VCC parser Mobility daemon
  70 - Start Post Process daemon         80 - Stop Post Process daemon

  1 - show daemon status
  0 - get out of this menu

EOM
echo "Enter choice:"
read choice
case "$choice" in
[0-1]|[1-8][0-9])
	exit $choice
	;;
*)
	exit -1
	;;
esac 
