#!/bin/bash
IDEMPIERE_HOME=$( dirname "$( readlink -f "${BASH_SOURCE[0]}")" )
if [ "$1" = "-h" ] || [ "$1" = "--help" ]
then
    echo "

Usage: ${BASH_SOURCE[0]} [db_name db_user db_password]

Called without parameters it tries to get the database connection information from the file $IDEMPIERE_HOME/idempiere.properties
This requires that install.app or install.console.app has been previously executed in eclipse.
It will then create a new iDempiere database and run the migration scripts on the database.

Migration will have to be done manually when the database information has been supplied through parameters. 

When called with parameter --empty or -e it creates an empty database based on the settings in idempiere.properties.

When called with parameter -h or --help prints this help message"
    exit 0
fi
if [ "$#" -eq 3 ]
then
    ADEMPIERE_DB_NAME="$1"
    ADEMPIERE_DB_USER="$2"
    PGPASSWORD="$3"
elif [ "$#" -le 1 ]
then
    if [ "$#" -eq 1 ]
    then
        if [ "$1" = "-e" ] || [ "$1" = "--empty" ]
        then
            CREATE_FULL=false
        else
            echo "

Wrong Parameter count! For Usage information run ${BASH_SOURCE[0]} -h"
            exit 1 
        fi
    else
        CREATE_FULL=true
    fi
    if [ -s "$IDEMPIERE_HOME/idempiere.properties" ]
    then
        PROPFILE="$IDEMPIERE_HOME/idempiere.properties"
    fi
    if [ -z "$PROPFILE" ]
    then
        echo "There is no idempiere.properties in folder $IDEMPIERE_HOME.
Please run first install.app or install.console.app within eclipse"
        exit 1
    fi
    cd "$IDEMPIERE_HOME" || exit
    CONN=$(grep "^Connection=.*type" "$PROPFILE")
    if [ -z "$CONN" ]
    then
        echo "There is no Connection definition in the properties file $PROPFILE, or Connection is encrypted"
        exit 1
    fi
    ADEMPIERE_DB_NAME="$(expr "$CONN" : ".*DBname.=\(.*\),BQ.=")"
    ADEMPIERE_DB_USER="$(expr "$CONN" : ".*UID.=\(.*\),PWD.=")"
    PGPASSWORD="$(expr "$CONN" : ".*PWD.=\(.*\)]")"
else
    echo "

Wrong Parameter count! For Usage information run ${BASH_SOURCE[0]} -h"
    exit 1    
fi
export PGPASSWORD
echo "Overwriting Database "$ADEMPIERE_DB_NAME" with a fresh seed." 
read -p "THIS WILL ERASE ALL DATA! Are you sure? (Y/N)" -n 1 -r
echo    # move to a new line
if [[ $REPLY =~ ^[Yy]$ ]]
then
    export PGPASSWORD

    dropdb --if-exists -U adempiere $ADEMPIERE_DB_NAME -h localhost &&
    createdb  --template=template0 -E UNICODE -O adempiere -U adempiere $ADEMPIERE_DB_NAME -h localhost &&
    psql -d $ADEMPIERE_DB_NAME -U adempiere -c "ALTER ROLE adempiere SET search_path TO adempiere, pg_catalog" -h localhost &&
    if [ "$CREATE_FULL" = true ]
    then    
        cd /tmp &&
        jar xvf $IDEMPIERE_HOME/org.adempiere.server-feature/data/seed/Adempiere_pg.jar &&
        psql -d idempiere_noflive -U adempiere -f Adempiere_pg.dmp -h localhost &&
        cd $IDEMPIERE_HOME &&
        if [ -z $1 ]
        then
            ./RUN_SyncDBDev.sh
        fi
    fi
else
    echo "Operation aborted."
fi
unset PGPASSWORD
