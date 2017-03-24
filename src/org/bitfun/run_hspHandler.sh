#!/usr/bin/env bash

INPUT=$1
HSP_HANDLER_PATH=~/git/hspHandler/out/artifacts/hspHandler_jar

echo "Checking and formatting input file: " $INPUT
awk -v OFS="\t" '{if($8=="plus"){$8="+"}else{$8="-"} print}' $INPUT | sort -k1,1 -k5,5 -k8,8 -k3,3n -k4,4n > $INPUT.clean
echo "Finished..."

# Filter best HSP
java -jar ${HSP_HANDLER_PATH}/hspHandler.jar -filter -e 20 -i $INPUT.clean > $INPUT.filter

java -jar ${HSP_HANDLER_PATH}/hspHandler.jar -chain -e 20 -i $INPUT.filter > $INPUT.chain

# Calculate Error Statistics
java -jar ${HSP_HANDLER_PATH}/hspHandler.jar -stats -i $INPUT.chain > $INPUT.stats

