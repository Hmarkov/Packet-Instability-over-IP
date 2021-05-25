#!/bin/bash
# Define an array with filenames as values
myArray=(www.qunar.com sohu.com www.nasa.gov aircanada.ca)
# Looping over for the two elements in myArray
for ((c=0; c<4; c++)); do
traceroute -n ${myArray[${c}]}
done
