#!/bin/bash
tvStatus=$(xset q | tail -n1)
set -- $tvStatus
user=$(whoami)
if [ "$3" = "On" ] 
then
  xset dpms force off
  echo "Powered OFF display."
fi
