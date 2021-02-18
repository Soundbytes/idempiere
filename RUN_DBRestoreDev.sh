#!/bin/bash
#IDEMPIERE_HOME=$( dirname "$( readlink -f "${BASH_SOURCE[0]}")" )

if [ "$1" = "-h" ] || [ "$1" = "--help" ]
then
    echo "
Usage: $0 [property_file]

Called without the property_file parameter it tries to get the database connection information 
from the file $IDEMPIERE_HOME/idempiereEnv.properties
This requires that install.app or install.console.app has been previously executed in eclipse.

If the first parameter is a file, then the script tries to obtain the connection information from such properties file.

Note: This script requires that a database backup has already been created using the RUN_DBExportDev.sh script.

When called with parameter -h or --help prints this help message"
    exit 0   
fi

IDEMPIERE_HOME=$( dirname "$( readlink -f "$0")" )
cd "$IDEMPIERE_HOME" #| echo "
#iDempiere home directory "$IDEMPIERE_HOME" not found."; exit 901

if [ ! -f "./data/ExpDat.dmp" ] 
then
    echo "
Database Backup not found. Make sure to run RUN_DBExportDev.sh at least once before attempting to restore." 
    exit 902
fi

if [ -s "$2" -a "skipsync" != "$2" ]
then
	PROPFILE="$(dirname "$2")/$(basename "$2")"
fi

if [ -z "$PROPFILE" ]
then
    if [ -s "$IDEMPIERE_HOME/idempiereEnv.properties" ]
    then
        PROPFILE="$IDEMPIERE_HOME/idempiereEnv.properties"
    fi
fi
if [ -z "$PROPFILE" ]
then
    echo "There is no idempiereEnv.properties in folder $IDEMPIERE_HOME.
Please run first install.app or install.console.app within eclipse"
    exit 904
fi

sudo service postgresql restart

source "$PROPFILE"
ADEMPIERE_DB_PATH=`echo "$ADEMPIERE_DB_PATH" | tr '[:upper:]' '[:lower:]'`

export IDEMPIERE_HOME
export ADEMPIERE_DB_NAME
export ADEMPIERE_DB_SERVER
export ADEMPIERE_DB_PORT

#echo Restore idempiere Database from Export- $IDEMPIERE_HOME \($ADEMPIERE_DB_NAME\)
#
#echo Re-Create idempiere User and import $IDEMPIERE_HOME/data/ExpDat.dmp
#echo == The import will show warnings. This is OK ==
#ls -lsa $IDEMPIERE_HOME/data/ExpDat.dmp
#echo Press enter to continue ...
#read in

# Parameter: <systemAccount> <adempiereID> <adempierePwd>
# globalqss - cruiz - 2007-10-09 - added fourth parameter for postgres(ignored in oracle)
sh "org.adempiere.server-feature/utils.unix/$ADEMPIERE_DB_PATH/DBRestore.sh" "system/$1" "$ADEMPIERE_DB_USER" "$ADEMPIERE_DB_PASSWORD" "$ADEMPIERE_DB_SYSTEM"
if [ -s "$2" -a "nosync" != "$2" ]
then
    ./RUN_SyncDBDev.sh
fi
