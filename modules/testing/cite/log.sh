if [ "$1" = "" ]; then
  echo "Usage: $0 <profile>"
  exit -1
fi

if [ -e target/logs/$1 ]; then
  sh engine/bin/viewlog.sh -logdir=target/logs -session=$1 $2
else
  echo "Error: profile '$1' does not exist."
  exit -1
fi
