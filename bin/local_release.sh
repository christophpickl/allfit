#!/bin/bash

source "bin/common.sh"

buildArtifactWithVersion "0"
echo ""
packageDeployables
echo ""

# copy needs to be done manually as permission denied :-(
echo "Now manually copy $TARGET_LOCATION to /Applications/AllFit.app"
open ./build
open /Applications

echo ""
echo "Success ✅"

echo ""
exit 0
