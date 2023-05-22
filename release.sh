#!/bin/bash

echo "Building package ..."
./gradlew clean shadowJar || exit 1

rm -rf /Applications/AllFit.jar || exit 1
mv build/libs/allfit-all.jar /Applications/AllFit.jar || exit 1

echo "Success ✅  See file located at: /Applications/AllFit.jar"
open /Applications
