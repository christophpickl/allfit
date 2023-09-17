#!/bin/bash

source "bin/common.sh"

buildArtifactWithVersion "0"
echo ""
packageDeployables
echo ""

# copy needs to be done manually as permission denied :-(
echo "Now manually copy $TARGET_LOCATION_APP to /Applications/AllFit.app"
open $TARGET_DIR
open /Applications

echo ""
echo "Success ✅"

echo ""
exit 0
