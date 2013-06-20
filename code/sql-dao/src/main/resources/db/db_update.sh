#!/bin/ksh

USER=testuser
DOMAIN=mysql.niidg.ru
DB=ordtest
PASSWORD=th8memAme5T4
CURRENT_VERSION=1

mysql -u $USER -h $DOMAIN -p$PASSWORD -D $DB -e "CREATE TABLE IF NOT EXISTS update_version (version INT NOT NULL)";
mysql -u $USER -h $DOMAIN -p$PASSWORD -D $DB -ss -n -q |&
print -p -- "SELECT max(version) FROM update_version;"
read -p DB_VER

echo "Последняя обновленная версия в бд $DB_VER"
let "NEW_VERSION=$DB_VER+1"

if [ $CURRENT_VERSION -lt $NEW_VERSION ]
    then
        echo "Версия sql уже есть в бд"
    return;
else
    while [ $NEW_VERSION -le $CURRENT_VERSION ]
    do
        cd $NEW_VERSION
        for FILE in *sql
        do
          mysql -u $USER -h $DOMAIN -p$PASSWORD -D $DB < $FILE
        done
        mysql -u $USER -h $DOMAIN -p$PASSWORD -D $DB -e "insert into update_version values ($NEW_VERSION)";
        let NEW_VERSION=$NEW_VERSION+1
        cd ../
    done
fi

