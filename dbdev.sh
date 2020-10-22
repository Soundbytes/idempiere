#!/bin/bash

# get current db selection state
read -r STATE < data/currentsetting

echo $STATE 
#echo "Press any key to continue . . ."
#read -n 1

if [ "$STATE" == "live" ] 
then
cd ~/dev-id &&
mv data data_live &&
mv data_dev data &&
cp data/idempiere.properties ./idempiere.properties &&
echo db switch to DEV success ||
echo db switch to DEV failed
else
echo dh switch to DEV not required 
fi
