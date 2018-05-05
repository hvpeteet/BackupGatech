#!/bin/bash
TESTS=cases/*.tgr

# 2>filename
#   # Redirect and append stderr to file "filename."
# &>filename
#   # Redirect both stdout and stderr to file "filename."

echo "Running IL tests"

# First check *_ok.tgr files for false positives
for f in $TESTS
do
  # echo "Testing case ${f%.*}"
  INFILE="${f%.*}_in"
  OUTFILE="${f%.*}_out"
  OURS="${f%.*}_ours"
  DIFF="${f%.*}_diff"
  java -jar compiler.jar --runil $f < $INFILE > $OURS
  diff $OURS $OUTFILE > $DIFF
    if [ -s $DIFF ]
    then
        echo "Test case ${f} failed"
    else
        echo "Test case ${f} passed"
    fi
done