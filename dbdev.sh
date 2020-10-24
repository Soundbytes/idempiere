#!/bin/bash

# get current db selection state
read -r STATE < data/currentsetting

echo $STATE 
#echo "Press any key to continue . . ."
#read -n 1

if [ "$STATE" != "dev" ] 
then
cd ~/dev-id &&
ln -sfn ./data_dev data &&
ln -sf ./data_dev/idempiere.properties idempiere.properties &&
echo db switch to DEV success ||
echo db switch to DEV failed; exit 1
else
echo db switch to DEV not required 
fi
