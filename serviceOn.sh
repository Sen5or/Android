#!/bin/bash
tvStatus=$(xset q | tail -n1)
set -- $tvStatus
user=$(whoami)
if [ "$3" = "Off" ] 
then
  xset dpms force on
  echo "Powered ON display."
fi
