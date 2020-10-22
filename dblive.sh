#!/bin/bash

# get current db selection state
read -r STATE < data/currentsetting

echo $STATE
#echo "Press any key to continue . . ."
#read -n 1

if [ "$STATE" == "dev" ] 
then
cd ~/dev-id &&
mv data data_dev && 
mv data_live data &&
cp data/idempiere.properties ./idempiere.properties &&
echo db switch to LIVE success ||
echo db switch to LIVE failed
else
echo dh switch to LIVE not required 
fi
