#!/bin/bash
var=$(tvservice -s)
user=$(whoami)
if [ "$var" = "state 0x120002 [TV is off]" ] 
then
  #For RPI3 
  tvservice -p && fbset -depth 8 && fbset -depth 16 && sudo chvt 6 && sudo chvt 7 
  #For Linux 
  #xset dpms force on 
  echo "Powered ON display."
#else
  #For RPI3 
  #tvservice -o
  #For Linux 
  #xset dpms force off 
fi
