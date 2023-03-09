#!/bin/bash

echo "Building package ..."
./gradlew clean shadowJar

mv build/libs/allfit-all.jar allfit.jar

echo "Success! See file located at: ./allfit.jar"
