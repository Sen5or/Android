#!/bin/bash
#while true; do { echo -e 'HTTP/1.1 200 OK\r\n'; sh service.sh; } | nc -l 80; done
#while true; do { cat /off; sh serviceOff.sh; } | nc -l 80; done
while true; do cat ./off | nc -l 8080 | sh serviceOff.sh; done
