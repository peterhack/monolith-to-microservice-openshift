#!/bin/bash

RANDOM_NO_OF_CLIENTS=$(( ( RANDOM % 10 )  + 1 ))

for (( i=1; i<=$2; i++))
do

  if [ -z "$3" ]
  then	  
    let CLIENTS=RANDOM_NO_OF_CLIENTS
  else 
    let CLIENTS=$3
  fi

  let SLEEPTIME=60/CLIENTS
  let CLIENTS_STARTED=0;

  while [ $CLIENTS -ne 0 ]; 
  do
    let INTERNAL=CLIENTS_STARTED%10

    if  [ $(( $CLIENTS % 2 )) == 0 ]
    then
      casperjs /app/click_through.js $1 ff $INTERNAL &
    else
      casperjs /app/click_through.js $1 ch $INTERNAL &
    fi
    
    let CLIENTS_STARTED=CLIENTS_STARTED+1
    let CLIENTS=CLIENTS-1
    
    sleep $SLEEPTIME
  done

done

wait
echo "finished"