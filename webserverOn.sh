#!/bin/bash
#while true; do { echo -e 'HTTP/1.1 200 OK\r\n'; sh service.sh; } | nc -l 80; done
#while true; do { cat /landing; sh serviceOn.sh; } | nc -l 80; done
while true; do cat ./on | nc -l 80 | sh serviceOn.sh; done

