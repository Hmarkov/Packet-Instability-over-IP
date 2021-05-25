#!/bin/bash
# The files size are around the reqested in the brief so 100k 2m 100m +-15k/5mb
myArray=(http://ftp.am.debian.org/raspbian/raspbian/pool/firmware/r/raspberrypi-firmware-nokernel/raspberrypi-firmware-nokernel_1.20180328-1~nokernel1.tar.gz)
filenames=(raspberrypi-firmware-nokernel_1.20180328-1~nokernel1.tar.gz)
host=(ftp.am.debian.org)
# Looping over for the two elements in myArray
for ((c=0; c<1; c++));
do
	for ((i=0; i<5; i++));
	do
		wget ${myArray[${c}]} &
		ping -c 60 >> 3Dlogfile.log ${host[${c}]} &
		wait
		rm ${filenames[${c}]}
	done
done
