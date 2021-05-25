#!/bin/bash
# The files size are around the reqested in the brief so 100k 2m 100m +-5k/5m
myArray=(http://ftp.am.debian.org/debian/doc/FAQ/debian-faq.en.html.tar.gz http://ftp.au.debian.org/debian/indices/Uploaders.gz http://ftp.am.debian.org/raspbian/raspbian/pool/firmware/r/raspberrypi-firmware-nokernel/raspberrypi-firmware-nokernel_1.20180328-1~nokernel1.tar.gz)
filenames=(debian-faq.en.html.tar.gz Uploaders.gz raspberrypi-firmware-nokernel_1.20180328-1~nokernel1.tar.gz)
# Looping over for the two elements in myArray
for ((c=0; c<3; c++));
do
	for ((i=0; i<10; i++));
	do
		wget -o ->> 3Alogfile.log ${myArray[${c}]}
		rm ${filenames[${c}]}
	done
done
