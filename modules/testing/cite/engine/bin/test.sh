#!/bin/sh
base=`dirname $0`
if [ -r $base/setenv.sh ]
then
  . $base/setenv.sh
fi
cp=$base/../components
for x in $base/../components/*.jar
do
  cp=$cp:$x
done
for x in $base/../scripts/*
do
  cp=$cp:$x/resources
done
java=$JAVA_HOME/bin/java
if [ ! -x $java ]
then
  java=java
fi
$java -cp $cp $JAVA_OPTS com.occamlab.te.Test -cmd=$0 $*

