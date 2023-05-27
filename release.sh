#!/bin/bash

echo ""
echo ">> gradlew clean shadowJar"

./gradlew clean shadowJar || exit 1

# Permission denied ... :'-(
#rm -rf /Applications/AllFit.jar || exit 1
#mv build/libs/allfit-all.jar /Applications/AllFit.jar || exit 1
mv build/libs/allfit-all.jar AllFit.jar || exit 1

echo ""
echo "Success ✅"
echo "Now copy the AllFit.jar to /Applications/AllFit.jar"
echo ""
open .
open /Applications

exit 0
