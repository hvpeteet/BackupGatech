#!/bin/bash
TESTS=tests/*.tgr


echo ""
echo "Running interpreter"
echo ""

for f in $TESTS
do
  echo -n "--$f: "
  java -jar compiler.jar --runil $f < ${f%.tgr}_in > ${f%.tgr}_res
done


echo ""
echo "Done interpreting... evaluating tests"
echo ""

let passes=0
let fails=0

for f in $TESTS
do
  echo -n "--$f: "
  diff -iw ${f%.tgr}_out ${f%.tgr}_res > ${f%.tgr}.diff
  if [ -s ${f%.tgr}.diff ]
  then
    echo "failed"
    let fails++
  else
    echo "passed"
    let passes++
  fi
done

echo ""
echo "passed: $passes"
echo "failed: $fails"
echo ""
