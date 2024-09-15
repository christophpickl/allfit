#!/bin/bash

source "bin/common.sh"

buildArtifactWithVersion "0"
echo ""
packageDeployables
echo ""
open $TARGET_DIR

echo ""
echo "Success ✅"

echo ""
exit 0
