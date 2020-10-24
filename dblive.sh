#!/bin/bash

# get current db selection state
read -r STATE < data/currentsetting

echo $STATE
#echo "Press any key to continue . . ."
#read -n 1

if [ "$STATE" != "live" ] 
then
cd ~/dev-id &&
ln -sfn ./data_live data &&
ln -sf ./data_live/idempiere.properties idempiere.properties &&
echo db switch to LIVE success ||
echo db switch to LIVE failed; exit 1
else
echo dh switch to LIVE not required 
fi
